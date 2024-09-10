package net.mineasterisk.mc.util;

import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.enchantment.blinkaura.BlinkAuraEnchantmentListener;
import net.mineasterisk.mc.enchantment.blinkstrike.BlinkStrikeEnchantmentListener;
import net.mineasterisk.mc.enchantment.frostbite.FrostbiteEnchantmentListener;
import net.mineasterisk.mc.service.access.AccessServiceListener;
import net.mineasterisk.mc.service.player.PlayerServiceListener;
import org.bukkit.plugin.PluginManager;

class EventUtil {
  protected static void register() {
    final MineAsterisk plugin = MineAsterisk.getInstance();
    final PluginManager manager = plugin.getServer().getPluginManager();

    manager.registerEvents(new AccessServiceListener(), plugin);
    manager.registerEvents(new PlayerServiceListener(), plugin);
    manager.registerEvents(new FrostbiteEnchantmentListener(), plugin);
    manager.registerEvents(new BlinkStrikeEnchantmentListener(), plugin);
    manager.registerEvents(new BlinkAuraEnchantmentListener(), plugin);

    plugin.getLogger().info("Registered event(s)");
  }
}
