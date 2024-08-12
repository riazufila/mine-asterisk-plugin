package net.mineasterisk.mc.util;

import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.manager.player.PlayerManager;
import org.bukkit.plugin.PluginManager;

public class EventUtil {
  public static void register() {
    final MineAsterisk plugin = PluginUtil.get();
    final PluginManager manager = plugin.getServer().getPluginManager();

    manager.registerEvents(new PlayerManager(), plugin);
  }
}
