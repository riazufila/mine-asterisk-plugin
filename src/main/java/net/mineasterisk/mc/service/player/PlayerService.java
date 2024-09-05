package net.mineasterisk.mc.service.player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.util.DatabaseUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.jetbrains.annotations.NotNull;

public class PlayerService implements Listener {
  @EventHandler
  public void onAsyncPlayerPreLogin(@NotNull AsyncPlayerPreLoginEvent event) {
    final Connection connection = DatabaseUtil.getConnection();
    final String name = event.getName();
    final UUID uuid = event.getUniqueId();
    final Result result = event.getLoginResult();
    final Component component = Component.text("Encountered error").color(NamedTextColor.RED);

    try {
      final PlayerRepository playerRepository = new PlayerRepository(connection);
      final boolean isPlayerExist = playerRepository.isPlayerExist(uuid).join();

      if (!isPlayerExist) {
        playerRepository.insert(uuid).join();
        connection.commit();
      }

      event.allow();
    } catch (SQLException exception) {
      try {
        connection.rollback();
      } catch (SQLException innerException) {
        event.disallow(result, component);

        MineAsterisk.getInstance()
            .getLogger()
            .severe(
                String.format(
                    "Encountered error while rolling back transaction during inserting Player %s (%s) into database: %s",
                    name, uuid, exception));

        return;
      }

      event.disallow(result, component);

      MineAsterisk.getInstance()
          .getLogger()
          .severe(
              String.format(
                  "Encountered error while inserting Player %s (%s) into database: %s",
                  name, uuid, exception));
    } finally {
      try {
        connection.close();
      } catch (SQLException exception) {
        MineAsterisk.getInstance()
            .getLogger()
            .severe(
                String.format(
                    "Encountered error while closing connection during inserting Player %s (%s) into database: %s",
                    name, uuid, exception));
      }
    }
  }
}
