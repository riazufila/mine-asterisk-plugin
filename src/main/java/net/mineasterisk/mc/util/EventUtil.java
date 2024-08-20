package net.mineasterisk.mc.util;

import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.service.access.AccessService;
import org.bukkit.plugin.PluginManager;

public class EventUtil {
  public static void register() {
    final MineAsterisk plugin = PluginUtil.get();
    final PluginManager manager = plugin.getServer().getPluginManager();

    manager.registerEvents(new AccessService(), plugin);

    PluginUtil.getLogger().info("Registered event(s)");
  }
}
