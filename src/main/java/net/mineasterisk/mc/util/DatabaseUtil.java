package net.mineasterisk.mc.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import net.mineasterisk.mc.MineAsterisk;
import org.jetbrains.annotations.NotNull;

public class DatabaseUtil {
  private static HikariDataSource dataSource;

  public static void initialize() {
    DatabaseUtil.dataSource = new HikariDataSource(DatabaseUtil.configure());
  }

  public static @NotNull Connection getConnection() {
    if (DatabaseUtil.dataSource == null) {
      DatabaseUtil.initialize();

      PluginUtil.getLogger().info("Initialized data source");
    }

    try {
      return DatabaseUtil.dataSource.getConnection();
    } catch (SQLException exception) {
      PluginUtil.getLogger()
          .severe(String.format("Encountered error while getting connection: %s", exception));

      throw new RuntimeException(exception);
    }
  }

  private static @NotNull HikariConfig configure() {
    final String HIKARICP_PROPERTIES = "hikaricp.properties";
    Properties properties = new Properties();

    try {
      try (final InputStream input =
          MineAsterisk.class.getClassLoader().getResourceAsStream(HIKARICP_PROPERTIES)) {
        if (input == null) {
          throw new IllegalStateException(String.format("Missing %s file", HIKARICP_PROPERTIES));
        }

        properties.load(input);

        return new HikariConfig(properties);
      }
    } catch (IOException exception) {
      PluginUtil.getLogger()
          .severe(String.format("Encountered error while configuring HikariCP: %s", exception));

      throw new RuntimeException(exception);
    }
  }
}
