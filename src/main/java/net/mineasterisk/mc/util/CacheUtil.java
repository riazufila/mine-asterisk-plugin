package net.mineasterisk.mc.util;

import io.papermc.paper.util.Tick;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.cache.access.AccessCache;
import net.mineasterisk.mc.cache.access.AccessCacheRunnable;
import net.mineasterisk.mc.repository.AccessRepository;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public class CacheUtil {
  private static final Set<@NotNull Integer> SYNCERS = new HashSet<>();

  protected static void load() {
    try {
      try (final Connection connection = DatabaseUtil.getConnection()) {
        final AccessRepository accessRepository = new AccessRepository(connection);
        final AccessCache accessCache = new AccessCache();
        final AccessCacheRunnable accessCacheRunnable = new AccessCacheRunnable();
        final int accessEntriesCount =
            accessCache.putAll(accessRepository.getAllPlayersAccesses().join());

        if (accessEntriesCount > 0) {
          MineAsterisk.getInstance()
              .getLogger()
              .info(String.format("Loaded %s access entries into cache", accessEntriesCount));
        } else {
          MineAsterisk.getInstance().getLogger().info("No access entries to load into cache");
        }

        final int accessCacheRunnableTaskId =
            accessCacheRunnable
                .runTaskTimerAsynchronously(
                    MineAsterisk.getInstance(),
                    Tick.tick().fromDuration(Duration.ofHours(1)),
                    Tick.tick().fromDuration(Duration.ofHours(1)))
                .getTaskId();

        MineAsterisk.getInstance()
            .getLogger()
            .info(
                String.format(
                    "Running access cache syncer with task ID %d", accessCacheRunnableTaskId));

        CacheUtil.SYNCERS.add(accessCacheRunnableTaskId);
      }
    } catch (final SQLException exception) {
      MineAsterisk.getInstance()
          .getLogger()
          .severe(String.format("Encountered error while loading cache: %s", exception));

      throw new RuntimeException(exception);
    }
  }

  protected static void finishAllSyncer() {
    final BukkitScheduler scheduler = MineAsterisk.getInstance().getServer().getScheduler();
    boolean isAnyTaskCurrentlyRunning = true;

    while (isAnyTaskCurrentlyRunning) {
      for (final Integer taskId : CacheUtil.SYNCERS) {
        final boolean isTaskCurrentlyRunning = scheduler.isCurrentlyRunning(taskId);

        if (!isTaskCurrentlyRunning) {
          scheduler.cancelTask(taskId);
        } else {
          MineAsterisk.getInstance()
              .getLogger()
              .info(String.format("Syncer with task ID %d is currently running", taskId));
        }
      }

      isAnyTaskCurrentlyRunning =
          CacheUtil.SYNCERS.stream().anyMatch(scheduler::isCurrentlyRunning);

      if (isAnyTaskCurrentlyRunning) {
        try {
          MineAsterisk.getInstance()
              .getLogger()
              .info("Sleeping thread for a minute, as at least one syncer is still running");
          Thread.sleep(Duration.ofMinutes(1));
        } catch (final InterruptedException exception) {
          MineAsterisk.getInstance()
              .getLogger()
              .severe("Encountered error while making thread sleep on finishing all cache syncer");
        }
      }
    }
  }

  public static void persist(final boolean isClearDirty) {
    final Connection connection = DatabaseUtil.getConnection();
    final AccessCache accessCache = new AccessCache();
    List<UUID> dirtyAccesses = new ArrayList<>();

    try {
      final AccessRepository accessRepository = new AccessRepository(connection);
      dirtyAccesses = accessRepository.updatePlayersAccesses(accessCache.getAllDirty()).join();

      if (!dirtyAccesses.isEmpty()) {
        MineAsterisk.getInstance()
            .getLogger()
            .info(String.format("Persisted %s dirty access cache", dirtyAccesses.size()));
      } else {
        MineAsterisk.getInstance().getLogger().info("No dirty access cache to persist");
      }

      if (isClearDirty && !dirtyAccesses.isEmpty()) {
        accessCache.setDirty(false, dirtyAccesses);

        MineAsterisk.getInstance()
            .getLogger()
            .info(
                String.format(
                    "Clearing dirty state for %s persisted access cache", dirtyAccesses.size()));
      }

      connection.commit();
    } catch (final SQLException exception) {
      try {
        if (isClearDirty && !dirtyAccesses.isEmpty()) {
          accessCache.setDirty(true, dirtyAccesses);
        }

        connection.rollback();
      } catch (final SQLException innerException) {
        MineAsterisk.getInstance()
            .getLogger()
            .severe(
                String.format(
                    "Encountered error while rolling back transaction during persisting cache: %s",
                    exception));

        throw new RuntimeException(innerException);
      }

      MineAsterisk.getInstance()
          .getLogger()
          .severe(String.format("Encountered error while persisting cache: %s", exception));

      throw new RuntimeException(exception);
    } finally {
      try {
        connection.close();
      } catch (final SQLException exception) {
        MineAsterisk.getInstance()
            .getLogger()
            .severe(
                String.format(
                    "Encountered error while closing connection during persisting cache: %s",
                    exception));

        //noinspection ThrowFromFinallyBlock
        throw new RuntimeException(exception);
      }
    }
  }
}
