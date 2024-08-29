package net.mineasterisk.mc.util;

import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.service.access.AccessService;
import net.mineasterisk.mc.service.player.PlayerService;
import org.bukkit.plugin.PluginManager;

class EventUtil {
  protected static void register() {
    final MineAsterisk plugin = MineAsterisk.getInstance();
    final PluginManager manager = plugin.getServer().getPluginManager();

    manager.registerEvents(new AccessService(), plugin);
    manager.registerEvents(new PlayerService(), plugin);

    plugin.getLogger().info("Registered event(s)");
  }
}
