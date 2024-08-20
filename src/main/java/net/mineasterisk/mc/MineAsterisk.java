package net.mineasterisk.mc;

import net.mineasterisk.mc.util.LoaderUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class MineAsterisk extends JavaPlugin {
  @Override
  public void onEnable() {
    try {
      LoaderUtil.preLoad();

      this.getLogger().info("Plugin enabled");
    } catch (Exception exception) {
      this.getServer().getPluginManager().disablePlugin(this);

      PluginUtil.getLogger()
          .severe(String.format("Encountered error while enabling Plugin: %s", exception));
    }
  }

  @Override
  public void onDisable() {
    try {
      LoaderUtil.postLoad();

      this.getLogger().info("Plugin disabled");
    } catch (Exception exception) {
      PluginUtil.getLogger()
          .severe(String.format("Encountered error while disabling Plugin: %s", exception));
    }
  }
}
