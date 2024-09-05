package net.mineasterisk.mc.service.access;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class AccessServiceListener implements Listener {
  @EventHandler
  public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    final AccessService accessService = new AccessService(player);

    accessService.negateAllDefaultPermissions();
    accessService.attach();
  }

  @EventHandler
  public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    final Player player = event.getPlayer();
    final AccessService accessService = new AccessService(player);

    accessService.detach();
  }
}
