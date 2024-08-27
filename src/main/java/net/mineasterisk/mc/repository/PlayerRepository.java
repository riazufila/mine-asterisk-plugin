package net.mineasterisk.mc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.util.PluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerRepository extends Repository {
  public PlayerRepository(final @NotNull Connection connection) {
    super(connection);
  }

  public @NotNull CompletableFuture<@NotNull Boolean> isPlayerExist(final @NotNull UUID uuid) {
    return CompletableFuture.supplyAsync(
        () -> {
          final String SQL = "SELECT 1 FROM player WHERE uuid = ?";

          try {
            try (final PreparedStatement statement = this.getConnection().prepareStatement(SQL)) {
              statement.setString(1, String.valueOf(uuid));

              try (final ResultSet result = statement.executeQuery()) {
                return result.next();
              }
            }
          } catch (SQLException exception) {
            PluginUtil.getLogger()
                .severe(
                    String.format(
                        "Encountered error while checking if Player exist: %s", exception));

            throw new RuntimeException(exception);
          }
        });
  }

  public @NotNull CompletableFuture<@Nullable Void> insert(final @NotNull UUID uuid) {
    return CompletableFuture.runAsync(
        () -> {
          final String SQL = "INSERT INTO player (uuid) VALUES (?)";

          try {
            try (final PreparedStatement statement = this.getConnection().prepareStatement(SQL)) {
              statement.setString(1, String.valueOf(uuid));
              statement.executeUpdate();
            }
          } catch (SQLException exception) {
            PluginUtil.getLogger()
                .severe(String.format("Encountered error while inserting Player: %s", exception));

            throw new RuntimeException(exception);
          }
        });
  }
}
