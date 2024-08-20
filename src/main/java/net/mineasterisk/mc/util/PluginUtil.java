package net.mineasterisk.mc.util;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import java.io.File;
import java.util.logging.Logger;
import net.mineasterisk.mc.MineAsterisk;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

public class PluginUtil {
  private static final @NotNull MineAsterisk PLUGIN = JavaPlugin.getPlugin(MineAsterisk.class);

  public static @NotNull MineAsterisk get() {
    return PluginUtil.PLUGIN;
  }

  public static @NotNull Server getServer() {
    return PluginUtil.PLUGIN.getServer();
  }

  public static @NotNull Scoreboard getMainScoreboard() {
    return PluginUtil.getServer().getScoreboardManager().getMainScoreboard();
  }

  public static @NotNull File getDataFolder() {
    return PluginUtil.PLUGIN.getDataFolder();
  }

  @SuppressWarnings("UnstableApiUsage")
  public static @NotNull LifecycleEventManager<Plugin> getLifecycleManager() {
    return PluginUtil.PLUGIN.getLifecycleManager();
  }

  public static @NotNull BukkitScheduler getScheduler() {
    return PluginUtil.getServer().getScheduler();
  }

  public static @NotNull Logger getLogger() {
    return PluginUtil.PLUGIN.getLogger();
  }
}
