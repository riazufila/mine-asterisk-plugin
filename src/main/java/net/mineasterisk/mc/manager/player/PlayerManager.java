package net.mineasterisk.mc.manager.player;

import java.time.Instant;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.service.PlayerService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerManager implements Listener {
  @EventHandler
  public void onPlayerJoin(final @NotNull PlayerJoinEvent event) {
    Player performedBy = event.getPlayer();
    PlayerModel playerToAdd = new PlayerModel(Instant.now(), performedBy.getUniqueId(), null);

    PlayerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId())
        .thenAccept(
            player -> {
              if (player == null) {
                PlayerService.add(performedBy, playerToAdd);
              }
            });
  }
}
