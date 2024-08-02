package net.mineasterisk.mc;

import net.mineasterisk.mc.util.LoaderUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class MineAsterisk extends JavaPlugin {
  @Override
  public void onEnable() {
    if (!LoaderUtil.startup()) {
      this.getServer().getPluginManager().disablePlugin(this);

      return;
    }

    this.getLogger().info("Plugin enabled");
  }

  @Override
  public void onDisable() {
    this.getLogger().severe("Plugin disabled");
  }
}
