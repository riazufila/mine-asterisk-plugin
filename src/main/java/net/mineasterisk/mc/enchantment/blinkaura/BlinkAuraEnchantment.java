package net.mineasterisk.mc.enchantment.blinkaura;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry.Builder;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.mineasterisk.mc.MineAsteriskBootstrap;
import net.mineasterisk.mc.service.livingentity.LivingEntityService;
import net.mineasterisk.mc.util.EnchantmentUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class BlinkAuraEnchantment extends net.mineasterisk.mc.enchantment.Enchantment {
  private static final String NAME = "Blink Aura";
  private static final Key NAMESPACED_KEY =
      Key.key(MineAsteriskBootstrap.getNamespace() + ":" + BlinkAuraEnchantment.getKey());

  private final @NotNull LivingEntity attacker;
  private final @NotNull LivingEntity attacked;

  public BlinkAuraEnchantment(
      final @NotNull LivingEntity attacker, final @NotNull LivingEntity attacked) {
    this.attacker = attacker;
    this.attacked = attacked;
  }

  @Subst("blink_aura")
  private static @NotNull String getKey() {
    return EnchantmentUtil.getKeyFromName(BlinkAuraEnchantment.NAME);
  }

  public static @NotNull Key getNamespacedKey() {
    return BlinkAuraEnchantment.NAMESPACED_KEY;
  }

  public static @NotNull TypedKey<@NotNull Enchantment> getTypedKey() {
    return TypedKey.create(RegistryKey.ENCHANTMENT, BlinkAuraEnchantment.NAMESPACED_KEY);
  }

  public static @NotNull Consumer<@NotNull Builder> getBuilder(
      final @NotNull RegistryFreezeEvent<@NotNull Enchantment, @NotNull Builder> event) {
    return builder ->
        builder
            .description(Component.text(BlinkAuraEnchantment.NAME))
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_ARMOR))
            .anvilCost(15)
            .maxLevel(10)
            .weight(1)
            .exclusiveWith(
                RegistrySet.keySet(
                    RegistryKey.ENCHANTMENT,
                    EnchantmentKeys.RESPIRATION,
                    EnchantmentKeys.AQUA_AFFINITY,
                    EnchantmentKeys.THORNS,
                    EnchantmentKeys.SWIFT_SNEAK,
                    EnchantmentKeys.SOUL_SPEED,
                    EnchantmentKeys.DEPTH_STRIDER,
                    EnchantmentKeys.FROST_WALKER))
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(55, 25))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 25))
            .activeSlots(EquipmentSlotGroup.ARMOR);
  }

  @Override
  protected void onAttack(final int level) {
    if (level <= 0) {
      throw new IllegalStateException("Enchantment level cannot be zero or lower");
    }

    new LivingEntityService(this.attacked).teleportAround(this.attacker, level);
  }
}
