package net.mineasterisk.mc.util;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry.Builder;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.event.WritableRegistry;
import java.util.Set;
import net.kyori.adventure.key.Key;
import net.mineasterisk.mc.MineAsteriskBootstrap;
import net.mineasterisk.mc.enchantment.blinkstrike.BlinkStrikeEnchantment;
import net.mineasterisk.mc.enchantment.frostbite.FrostbiteEnchantment;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

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

                  registry.register(
                      FrostbiteEnchantment.getTypedKey(), FrostbiteEnchantment.getBuilder(event));

                  registry.register(
                      BlinkStrikeEnchantment.getTypedKey(),
                      BlinkStrikeEnchantment.getBuilder(event));
                }));
  }

  public static Enchantment get(final @NotNull Key key) {
    final Registry<Enchantment> enchantmentRegistry =
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

    return enchantmentRegistry.getOrThrow(TypedKey.create(RegistryKey.ENCHANTMENT, key));
  }

  public static int getTotalEnchantmentLevel(
      final @NotNull Enchantment enchantment, final @NotNull EntityEquipment equipment) {
    final Set<EquipmentSlotGroup> activeSlotGroups = enchantment.getActiveSlotGroups();
    int totalLevel = 0;

    for (final EquipmentSlotGroup activeSlotGroup : activeSlotGroups) {
      if (activeSlotGroup.equals(EquipmentSlotGroup.MAINHAND)) {
        totalLevel += equipment.getItem(EquipmentSlot.HAND).getEnchantmentLevel(enchantment);
      } else if (activeSlotGroup.equals(EquipmentSlotGroup.OFFHAND)) {
        totalLevel += equipment.getItem(EquipmentSlot.OFF_HAND).getEnchantmentLevel(enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.HAND) {
        totalLevel += equipment.getItem(EquipmentSlot.HAND).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.OFF_HAND).getEnchantmentLevel(enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.HEAD) {
        totalLevel += equipment.getItem(EquipmentSlot.HEAD).getEnchantmentLevel(enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.CHEST) {
        totalLevel += equipment.getItem(EquipmentSlot.CHEST).getEnchantmentLevel(enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.LEGS) {
        totalLevel += equipment.getItem(EquipmentSlot.LEGS).getEnchantmentLevel(enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.FEET) {
        totalLevel += equipment.getItem(EquipmentSlot.FEET).getEnchantmentLevel(enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.ARMOR) {
        totalLevel += equipment.getItem(EquipmentSlot.HEAD).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.CHEST).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.LEGS).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.FEET).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.BODY).getEnchantmentLevel(enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.BODY) {
        totalLevel += equipment.getItem(EquipmentSlot.BODY).getEnchantmentLevel(enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.ANY) {
        totalLevel += equipment.getItem(EquipmentSlot.HAND).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.OFF_HAND).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.HEAD).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.CHEST).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.LEGS).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.FEET).getEnchantmentLevel(enchantment);
        totalLevel += equipment.getItem(EquipmentSlot.BODY).getEnchantmentLevel(enchantment);
      }
    }

    return totalLevel;
  }
}
