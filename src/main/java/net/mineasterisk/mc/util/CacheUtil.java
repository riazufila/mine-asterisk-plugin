package net.mineasterisk.mc.util;

import io.papermc.paper.util.Tick;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;
import net.mineasterisk.mc.cache.access.Access;
import net.mineasterisk.mc.cache.access.AccessCache;
import net.mineasterisk.mc.cache.access.AccessCacheRunnable;
import net.mineasterisk.mc.repository.AccessRepository;

public class CacheUtil {
  protected static void load() {
    try {
      try (final Connection connection = DatabaseUtil.getConnection()) {
        final AccessRepository accessRepository = new AccessRepository(connection);
        final AccessCache accessCache = new AccessCache();
        final AccessCacheRunnable accessCacheRunnable = new AccessCacheRunnable();

        accessCache.putAll(accessRepository.getAllPlayersAccesses().join());
        accessCacheRunnable.runTaskTimerAsynchronously(
            PluginUtil.get(),
            Tick.tick().fromDuration(Duration.ofHours(1)),
            Tick.tick().fromDuration(Duration.ofHours(1)));
      }

      PluginUtil.getLogger().info("Loaded persistent data into cache if any");
    } catch (SQLException exception) {
      PluginUtil.getLogger()
          .severe(String.format("Encountered error while loading cache: %s", exception));

      throw new RuntimeException(exception);
    }
  }

  public static void persist(final boolean isClearDirty) {
    final Connection connection = DatabaseUtil.getConnection();
    final AccessCache accessCache = new AccessCache();
    HashMap<UUID, Access> accessesDirtyEntries = new HashMap<>();

    try {
      final AccessRepository accessRepository = new AccessRepository(connection);
      accessesDirtyEntries =
          accessRepository.updatePlayersAccesses(accessCache.getAllDirty()).join();

      if (isClearDirty && !accessesDirtyEntries.isEmpty()) {
        accessCache.setDirty(false, accessesDirtyEntries);
      }

      connection.commit();

      PluginUtil.getLogger().info("Persisted cache into database if any");
    } catch (SQLException exception) {
      try {
        if (isClearDirty && !accessesDirtyEntries.isEmpty()) {
          accessCache.setDirty(true, accessesDirtyEntries);
        }

        connection.rollback();
      } catch (SQLException innerException) {
        PluginUtil.getLogger()
            .severe(
                String.format(
                    "Encountered error while rolling back transaction during persisting cache: %s",
                    exception));

        throw new RuntimeException(innerException);
      }

      PluginUtil.getLogger()
          .severe(String.format("Encountered error while persisting cache: %s", exception));

      throw new RuntimeException(exception);
    } finally {
      try {
        connection.close();
      } catch (SQLException exception) {
        PluginUtil.getLogger()
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
