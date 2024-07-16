package net.mineasterisk.mc;

import net.mineasterisk.mc.loader.Loader;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class MineAsterisk extends JavaPlugin {
    public static @NotNull Logger getPluginLogger() {
        return MineAsterisk.getPlugin(MineAsterisk.class).getLogger();
    }

    @Override
    public void onEnable() {
        if (!Loader.startup()) {
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
