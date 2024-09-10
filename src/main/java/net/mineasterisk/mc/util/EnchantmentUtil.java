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

  public static @NotNull Enchantment get(final @NotNull Key key) {
    final Registry<Enchantment> enchantmentRegistry =
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

    return enchantmentRegistry.getOrThrow(TypedKey.create(RegistryKey.ENCHANTMENT, key));
  }

  public static @NotNull String getKeyFromName(final @NotNull String name) {
    return name.toLowerCase().replace(" ", "_");
  }

  public static int getTotalEnchantmentLevel(
      final @NotNull Enchantment enchantment, final @NotNull EntityEquipment equipment) {
    final Set<EquipmentSlotGroup> activeSlotGroups = enchantment.getActiveSlotGroups();
    int totalLevel = 0;

    for (final EquipmentSlotGroup activeSlotGroup : activeSlotGroups) {
      if (activeSlotGroup.equals(EquipmentSlotGroup.MAINHAND)) {
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.HAND, enchantment);
      } else if (activeSlotGroup.equals(EquipmentSlotGroup.OFFHAND)) {
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.OFF_HAND, enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.HAND) {
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.HAND, enchantment);
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.OFF_HAND, enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.HEAD) {
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.HEAD, enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.CHEST) {
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.CHEST, enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.LEGS) {
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.LEGS, enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.FEET) {
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.FEET, enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.ARMOR) {
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.HEAD, enchantment)
                + EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.CHEST, enchantment)
                + EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.LEGS, enchantment)
                + EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.FEET, enchantment)
                + EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.BODY, enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.BODY) {
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.BODY, enchantment);
      } else if (activeSlotGroup == EquipmentSlotGroup.ANY) {
        totalLevel +=
            EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.HAND, enchantment)
                + EnchantmentUtil.getEnchantmentLevel(
                    equipment, EquipmentSlot.OFF_HAND, enchantment)
                + EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.HEAD, enchantment)
                + EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.CHEST, enchantment)
                + EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.LEGS, enchantment)
                + EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.FEET, enchantment)
                + EnchantmentUtil.getEnchantmentLevel(equipment, EquipmentSlot.BODY, enchantment);
      }
    }

    return totalLevel;
  }

  private static int getEnchantmentLevel(
      final @NotNull EntityEquipment equipment,
      final @NotNull EquipmentSlot equipmentSlot,
      final @NotNull Enchantment enchantment) {
    try {
      return equipment.getItem(equipmentSlot).getEnchantmentLevel(enchantment);
    } catch (IllegalArgumentException exception) {
      return 0;
    }
  }
}
