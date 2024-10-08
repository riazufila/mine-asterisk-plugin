package net.mineasterisk.mc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.cache.access.Access;
import org.jetbrains.annotations.NotNull;

public class AccessRepository extends Repository {
  public AccessRepository(final @NotNull Connection connection) {
    super(connection);
  }

  public @NotNull CompletableFuture<@NotNull HashMap<@NotNull UUID, @NotNull Access>>
      getAllPlayersAccesses() {
    return CompletableFuture.supplyAsync(
        () -> {
          final HashMap<UUID, Access> playersAccesses = new HashMap<>();
          final StringJoiner sql = new StringJoiner(" ");

          sql.add("SELECT player.uuid, permission.name");
          sql.add("FROM player");
          sql.add("INNER JOIN access ON player.id = access.player_id");
          sql.add("INNER JOIN permission ON access.permission_id = permission.id");

          try {
            try (final PreparedStatement statement =
                this.getConnection().prepareStatement(sql.toString())) {
              try (final ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                  UUID uuid = UUID.fromString(result.getString("uuid"));
                  String permission = result.getString("name");

                  playersAccesses
                      .computeIfAbsent(uuid, key -> new Access(new HashSet<>()))
                      .getAccesses()
                      .add(permission);
                }
              }
            }

            return playersAccesses;
          } catch (final SQLException exception) {
            MineAsterisk.getInstance()
                .getLogger()
                .severe(
                    String.format(
                        "Encountered error while getting all Players' accesses: %s", exception));

            throw new RuntimeException(exception);
          }
        });
  }

  public @NotNull CompletableFuture<@NotNull List<@NotNull UUID>> updatePlayersAccesses(
      final @NotNull HashMap<@NotNull UUID, @NotNull Access> playersAccesses) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (playersAccesses.isEmpty()) {
            return new ArrayList<>();
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
          insertSql.add("INNER JOIN permission ON permission.name = ?");
          insertSql.add("WHERE player.uuid = ?");

          try {
            @SuppressWarnings("resource")
            final PreparedStatement deleteStatement =
                this.getConnection().prepareStatement(deleteSql.toString());

            @SuppressWarnings("resource")
            final PreparedStatement insertStatement =
                this.getConnection().prepareStatement(insertSql.toString());

            for (final Map.Entry<UUID, Access> entry : playersAccesses.entrySet()) {
              UUID uuid = entry.getKey();

              deleteStatement.setString(1, String.valueOf(uuid));
              deleteStatement.addBatch();

              for (final String permission : entry.getValue().getAccesses()) {
                insertStatement.setString(1, permission);
                insertStatement.setString(2, String.valueOf(uuid));
                insertStatement.addBatch();
              }
            }

            final int[] deleteCounts = deleteStatement.executeBatch();
            final int[] insertCounts = insertStatement.executeBatch();

            for (int i = 0; i < deleteCounts.length; i++) {
              MineAsterisk.getInstance()
                  .getLogger()
                  .info(String.format("Delete count for batch %d: %d", i + 1, deleteCounts[i]));
            }

            for (int i = 0; i < insertCounts.length; i++) {
              MineAsterisk.getInstance()
                  .getLogger()
                  .info(String.format("Insert count for batch %d: %d", i + 1, insertCounts[i]));
            }

            return playersAccesses.keySet().stream().toList();
          } catch (final SQLException exception) {
            MineAsterisk.getInstance()
                .getLogger()
                .severe(
                    String.format(
                        "Encountered error while updating Players' accesses: %s", exception));

            throw new RuntimeException(exception);
          }
        });
  }
}
