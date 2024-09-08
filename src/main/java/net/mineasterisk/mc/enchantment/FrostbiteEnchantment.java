package net.mineasterisk.mc.enchantment;

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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class FrostbiteEnchantment {
  private final @NotNull TypedKey<@NotNull Enchantment> key;
  private final @NotNull Consumer<@NotNull Builder> builder;

  public FrostbiteEnchantment(
      final @NotNull RegistryFreezeEvent<@NotNull Enchantment, @NotNull Builder> event) {
    this.key =
        TypedKey.create(
            RegistryKey.ENCHANTMENT, Key.key(MineAsteriskBootstrap.getNamespace() + ":frostbite"));

    this.builder =
        builder ->
            builder
                .description(Component.text("Frostbite"))
                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
                .anvilCost(4)
                .maxLevel(2)
                .weight(1)
                .exclusiveWith(
                    RegistrySet.keySet(RegistryKey.ENCHANTMENT, EnchantmentKeys.FIRE_ASPECT))
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 8))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(18, 8))
                .activeSlots(EquipmentSlotGroup.MAINHAND);
  }

  public @NotNull TypedKey<@NotNull Enchantment> getKey() {
    return this.key;
  }

  public @NotNull Consumer<@NotNull Builder> getBuilder() {
    return this.builder;
  }
}
