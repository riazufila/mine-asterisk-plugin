package net.mineasterisk.mc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.util.PluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccessRepository extends Repository {
  public AccessRepository(final @NotNull Connection connection) {
    super(connection);
  }

  public @NotNull CompletableFuture<@NotNull HashMap<@NotNull UUID, @NotNull Set<String>>>
      getAllPlayersAccesses() {
    return CompletableFuture.supplyAsync(
        () -> {
          final HashMap<UUID, Set<String>> playersAccesses = new HashMap<>();
          final StringJoiner sql = new StringJoiner(" ");

          sql.add("SELECT player.uuid, permission.value");
          sql.add("FROM player");
          sql.add("INNER JOIN access ON player.id = access.player_id");
          sql.add("INNER JOIN permission ON access.permission_id = permission.id");

          try {
            try (final PreparedStatement statement =
                this.getConnection().prepareStatement(sql.toString())) {
              try (final ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                  UUID uuid = UUID.fromString(result.getString("uuid"));
                  String permission = result.getString("value");

                  playersAccesses.computeIfAbsent(uuid, key -> new HashSet<>()).add(permission);
                }
              }
            }
          } catch (SQLException exception) {
            PluginUtil.getLogger()
                .severe(
                    String.format(
                        "Encountered error while getting all Players' accesses: %s", exception));
          }

          return playersAccesses;
        });
  }

  public @NotNull CompletableFuture<@Nullable Void> updatePlayersAccesses(
      final @NotNull HashMap<@NotNull UUID, @NotNull Set<String>> playersAccesses) {
    return CompletableFuture.runAsync(
        () -> {
          if (playersAccesses.isEmpty()) {
            return;
          }

          final StringJoiner deleteSql = new StringJoiner(" ");
          final StringJoiner insertSql = new StringJoiner(" ");

          deleteSql.add("DELETE access");
          deleteSql.add("FROM access");
          deleteSql.add("INNER JOIN player ON access.player_id = player.id");
          deleteSql.add("INNER JOIN permission ON access.permission_id = permission.id");
          deleteSql.add("WHERE player.uuid = ?");

          insertSql.add("INSERT INTO access (player_id, permission_id)");
          insertSql.add("SELECT player.id, permission.id");
          insertSql.add("FROM player");
          insertSql.add("INNER JOIN permission ON permission.value = ?");
          insertSql.add("WHERE player.uuid = ?");

          try {
            @SuppressWarnings("resource")
            final PreparedStatement deleteStatement =
                this.getConnection().prepareStatement(deleteSql.toString());

            @SuppressWarnings("resource")
            final PreparedStatement insertStatement =
                this.getConnection().prepareStatement(insertSql.toString());

            for (final Map.Entry<UUID, Set<String>> entry : playersAccesses.entrySet()) {
              UUID uuid = entry.getKey();

              deleteStatement.setString(1, String.valueOf(uuid));
              deleteStatement.addBatch();

              for (final String permission : entry.getValue()) {
                insertStatement.setString(1, permission);
                insertStatement.setString(2, String.valueOf(uuid));
                insertStatement.addBatch();
              }
            }

            deleteStatement.executeBatch();
            insertStatement.executeBatch();
          } catch (SQLException exception) {
            PluginUtil.getLogger()
                .severe(
                    String.format(
                        "Encountered error while updating Players' accesses: %s", exception));
          }
        });
  }
}
