package net.mineasterisk.mc.enchantment.blinkstrike;

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
public class BlinkStrikeEnchantment extends net.mineasterisk.mc.enchantment.Enchantment {
  private static final String NAME = "Blink Strike";
  private static final Key NAMESPACED_KEY =
      Key.key(MineAsteriskBootstrap.getNamespace() + ":" + BlinkStrikeEnchantment.getKey());

  private final @NotNull LivingEntity attacker;
  private final @NotNull LivingEntity attacked;

  public BlinkStrikeEnchantment(
      final @NotNull LivingEntity attacker, final @NotNull LivingEntity attacked) {
    this.attacker = attacker;
    this.attacked = attacked;
  }

  @Subst("blink_strike")
  private static @NotNull String getKey() {
    return EnchantmentUtil.getKeyFromName(BlinkStrikeEnchantment.NAME);
  }

  public static @NotNull Key getNamedspacedKey() {
    return BlinkStrikeEnchantment.NAMESPACED_KEY;
  }

  public static @NotNull TypedKey<@NotNull Enchantment> getTypedKey() {
    return TypedKey.create(RegistryKey.ENCHANTMENT, BlinkStrikeEnchantment.NAMESPACED_KEY);
  }

  public static @NotNull Consumer<@NotNull Builder> getBuilder(
      final @NotNull RegistryFreezeEvent<@NotNull Enchantment, @NotNull Builder> event) {
    return builder ->
        builder
            .description(Component.text(BlinkStrikeEnchantment.NAME))
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_SWORD))
            .anvilCost(10)
            .maxLevel(41)
            .weight(1)
            .exclusiveWith(
                RegistrySet.keySet(RegistryKey.ENCHANTMENT, EnchantmentKeys.SWEEPING_EDGE))
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(50, 20))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(70, 20))
            .activeSlots(EquipmentSlotGroup.MAINHAND);
  }

  @Override
  protected void onAttack(final int level) {
    if (level <= 0) {
      throw new IllegalStateException("Enchantment level cannot be zero or lower");
    }

    new LivingEntityService(this.attacker).teleportAround(this.attacked, level - 1);
  }
}
