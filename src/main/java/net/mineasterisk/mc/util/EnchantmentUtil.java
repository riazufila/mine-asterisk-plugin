package net.mineasterisk.mc.util;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry.Builder;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.event.WritableRegistry;
import net.mineasterisk.mc.MineAsteriskBootstrap;
import org.bukkit.enchantments.Enchantment;

@SuppressWarnings("UnstableApiUsage")
public class EnchantmentUtil {
  public static void register() {
    final BootstrapContext context = MineAsteriskBootstrap.getContext();
    final LifecycleEventManager<BootstrapContext> manager = context.getLifecycleManager();

    manager.registerEventHandler(
        RegistryEvents.ENCHANTMENT
            .freeze()
            .newHandler(
                event -> {
                  final WritableRegistry<Enchantment, Builder> registry = event.registry();

                  // registry.register(..., ...);
                }));
  }
}
