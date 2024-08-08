package net.mineasterisk.mc.util;

import java.util.logging.Logger;
import net.mineasterisk.mc.MineAsterisk;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

public class PluginUtil {
  private static final @NotNull MineAsterisk plugin = JavaPlugin.getPlugin(MineAsterisk.class);

  public static @NotNull MineAsterisk get() {
    return PluginUtil.plugin;
  }

  public static @NotNull Scoreboard getMainScoreboard() {
    return PluginUtil.plugin.getServer().getScoreboardManager().getMainScoreboard();
  }

  public static @NotNull Logger getLogger() {
    return PluginUtil.plugin.getLogger();
  }
}
