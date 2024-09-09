package net.mineasterisk.mc.enchantment.frostbite;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry.Builder;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.util.Tick;
import java.time.Duration;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.mineasterisk.mc.MineAsteriskBootstrap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class FrostbiteEnchantment extends net.mineasterisk.mc.enchantment.Enchantment {
  private static final Key KEY = Key.key(MineAsteriskBootstrap.getNamespace() + ":frostbite");
  private final @NotNull LivingEntity attacked;

  public FrostbiteEnchantment(final @NotNull LivingEntity attacked) {
    this.attacked = attacked;
  }

  public static @NotNull Key getKey() {
    return FrostbiteEnchantment.KEY;
  }

  public static @NotNull TypedKey<@NotNull Enchantment> getTypedKey() {
    return TypedKey.create(RegistryKey.ENCHANTMENT, FrostbiteEnchantment.KEY);
  }

  public static @NotNull Consumer<@NotNull Builder> getBuilder(
      final @NotNull RegistryFreezeEvent<@NotNull Enchantment, @NotNull Builder> event) {
    return builder ->
        builder
            .description(Component.text("Frostbite"))
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
            .anvilCost(4)
            .maxLevel(2)
            .weight(1)
            .exclusiveWith(RegistrySet.keySet(RegistryKey.ENCHANTMENT, EnchantmentKeys.FIRE_ASPECT))
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 8))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(18, 8))
            .activeSlots(EquipmentSlotGroup.MAINHAND);
  }

  @Override
  protected void onAttack(final int level) {
    if (level <= 0) {
      throw new IllegalStateException("Enchantment level cannot be zero or lower");
    }

    final int tick = Tick.tick().fromDuration(Duration.ofSeconds(2)) * level;
    final int existingFreezeTicks = this.attacked.getFreezeTicks();

    this.attacked.setFreezeTicks(existingFreezeTicks + tick);
  }
}
