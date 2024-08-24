package net.mineasterisk.mc.service.player;

import java.sql.Connection;
import java.sql.SQLException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.util.DatabaseUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.jetbrains.annotations.NotNull;

public class PlayerService implements Listener {
  @EventHandler
  public void onPlayerLogin(@NotNull PlayerLoginEvent event) {
    final Connection connection = DatabaseUtil.getConnection();
    final Player player = event.getPlayer();
    final Result result = event.getResult();
    final Component component = Component.text("Encountered error").color(NamedTextColor.RED);

    try {
      final PlayerRepository playerRepository = new PlayerRepository(connection);

      playerRepository.insertIfNotExist(player.getUniqueId()).join();
      connection.commit();
      event.allow();
    } catch (SQLException exception) {
      try {
        connection.rollback();
      } catch (SQLException innerException) {
        event.disallow(result, component);

        PluginUtil.getLogger()
            .severe(
                String.format(
                    "Encountered error while rolling back transaction during inserting Player into database: %s",
                    exception));

        return;
      }

      event.disallow(result, component);

      PluginUtil.getLogger()
          .severe(
              String.format(
                  "Encountered error while inserting Player into database: %s", exception));
    } finally {
      try {
        connection.close();
      } catch (SQLException exception) {
        PluginUtil.getLogger()
            .severe(
                String.format(
                    "Encountered error while closing connection during inserting Player into database: %s",
                    exception));
      }
    }
  }
}
