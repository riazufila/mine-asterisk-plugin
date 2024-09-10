package net.mineasterisk.mc;

import net.mineasterisk.mc.util.LoaderUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MineAsterisk extends JavaPlugin {
  private static @Nullable MineAsterisk instance;

  public static @NotNull String getNamespace() {
    return MineAsterisk.getInstance().getName().toLowerCase();
  }

  public static @NotNull MineAsterisk getInstance() {
    if (MineAsterisk.instance == null) {
      throw new IllegalStateException("Plugin instance is not initialized");
    }

    return MineAsterisk.instance;
  }

  @Override
  public void onEnable() {
    MineAsterisk.instance = this;

    try {
      LoaderUtil.preLoad();

      this.getLogger().info("Plugin enabled");
    } catch (final Exception exception) {
      this.getServer().getPluginManager().disablePlugin(this);

      this.getLogger()
          .severe(String.format("Encountered error while enabling Plugin: %s", exception));
    }
  }

  @Override
  public void onDisable() {
    try {
      LoaderUtil.postLoad();

      this.getLogger().info("Plugin disabled");
    } catch (final Exception exception) {
      this.getLogger()
          .severe(String.format("Encountered error while disabling Plugin: %s", exception));
    }
  }
}
