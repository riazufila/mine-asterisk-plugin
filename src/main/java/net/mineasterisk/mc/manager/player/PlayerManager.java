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
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;

public class PlayerManager implements Listener {
  @SuppressWarnings("TryFinallyCanBeTryWithResources")
  @EventHandler
  public void onPlayerJoin(final @NotNull PlayerLoginEvent event) {
    final StatelessSession statelessSession = HibernateUtil.getStatelessSession();
    final Transaction transaction = statelessSession.beginTransaction();

    try {
      final Player performedBy = event.getPlayer();
      final PlayerModel playerToAdd =
          new PlayerModel(Instant.now(), performedBy.getUniqueId(), null);

      final PlayerRepository playerRepository = new PlayerRepository(statelessSession);
      final PlayerService playerService = new PlayerService(statelessSession);
      final PlayerModel player =
          playerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId()).join();

      if (player == null) {
        playerService.add(performedBy, playerToAdd).join();
      }

      transaction.commit();
    } catch (Exception exception) {
      event.disallow(
          Result.KICK_OTHER,
          Component.text("Encountered error while joining...").color(NamedTextColor.RED));

      transaction.rollback();

      PluginUtil.getLogger()
          .severe(
              String.format(
                  "Unable to initialize Player %s: %s",
                  event.getPlayer().getUniqueId(), exception));
    } finally {
      statelessSession.close();
    }
  }
}
