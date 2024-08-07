package net.mineasterisk.mc.util;

import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.playermanager.PlayerManager;
import org.bukkit.plugin.PluginManager;

public class EventUtil {
  public static void register() {
    MineAsterisk plugin = PluginUtil.get();
    PluginManager manager = plugin.getServer().getPluginManager();

    manager.registerEvents(new PlayerManager(), plugin);
  }
}
