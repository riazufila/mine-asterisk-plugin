package net.mineasterisk.mc.manager.player;

import java.time.Instant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.service.PlayerService;
import net.mineasterisk.mc.util.HibernateUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

public class PlayerManager implements Listener {
  @EventHandler
  public void onPlayerJoin(final @NotNull PlayerLoginEvent event) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    session.getTransaction().begin();

    try {
      Player performedBy = event.getPlayer();
      PlayerModel playerToAdd = new PlayerModel(Instant.now(), performedBy.getUniqueId(), null);
      PlayerRepository playerRepository = new PlayerRepository(session);
      PlayerModel player =
          playerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId()).join();

      PlayerService playerService = new PlayerService(session);

      if (player == null) {
        playerService.add(performedBy, playerToAdd).join();
      }

      session.getTransaction().commit();
    } catch (Exception exception) {
      event.disallow(
          Result.KICK_OTHER,
          Component.text("Encountered error while joining...").color(NamedTextColor.RED));

      session.getTransaction().rollback();

      PluginUtil.getLogger()
          .severe(
              String.format(
                  "Unable to initialize Player %s: %s",
                  event.getPlayer().getUniqueId(), exception));
    } finally {
      session.close();
    }
  }
}
