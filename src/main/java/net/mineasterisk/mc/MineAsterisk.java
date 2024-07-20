package net.mineasterisk.mc;

import java.util.logging.Logger;
import net.mineasterisk.mc.util.LoaderUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MineAsterisk extends JavaPlugin {
  public static @NotNull Logger getPluginLogger() {
    return MineAsterisk.getPlugin(MineAsterisk.class).getLogger();
  }

  @Override
  public void onEnable() {
    if (!LoaderUtil.startup()) {
      this.getServer().getPluginManager().disablePlugin(this);

      return;
    }

    this.getLogger().info("Plugin enabled.");
  }

  @Override
  public void onDisable() {
    this.getLogger().severe("Plugin disabled.");
  }
}
