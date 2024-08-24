package net.mineasterisk.mc.util;

import java.sql.Connection;
import java.sql.SQLException;
import net.mineasterisk.mc.cache.access.AccessCache;
import net.mineasterisk.mc.repository.AccessRepository;

class CacheUtil {
  protected static void load() {
    try {
      try (final Connection connection = DatabaseUtil.getConnection()) {
        final AccessRepository accessRepository = new AccessRepository(connection);
        final AccessCache accessCache = new AccessCache();

        accessCache.putAll(accessRepository.getAllPlayersAccesses().join());
      }

      PluginUtil.getLogger().info("Loaded persistent data into cache if any");
    } catch (SQLException exception) {
      PluginUtil.getLogger()
          .severe(String.format("Encountered error while loading cache: %s", exception));

      throw new RuntimeException(exception);
    }
  }

  protected static void persist() {
    final Connection connection = DatabaseUtil.getConnection();
    try {

      final AccessRepository accessRepository = new AccessRepository(connection);
      final AccessCache accessCache = new AccessCache();

      accessRepository.updatePlayersAccesses(accessCache.getAllDirty()).join();
      connection.commit();

      PluginUtil.getLogger().info("Persisted cache into database if any");
    } catch (SQLException exception) {
      try {
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
