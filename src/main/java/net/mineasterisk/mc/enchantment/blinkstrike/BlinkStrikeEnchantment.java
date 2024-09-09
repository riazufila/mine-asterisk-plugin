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
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.mineasterisk.mc.MineAsteriskBootstrap;
import net.mineasterisk.mc.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class BlinkStrikeEnchantment extends net.mineasterisk.mc.enchantment.Enchantment {
  private static final Key KEY = Key.key(MineAsteriskBootstrap.getNamespace() + ":blink_strike");
  private final @NotNull LivingEntity attacker;
  private final @NotNull LivingEntity attacked;

  public BlinkStrikeEnchantment(
      final @NotNull LivingEntity attacker, final @NotNull LivingEntity attacked) {
    this.attacker = attacker;
    this.attacked = attacked;
  }

  public static @NotNull Key getKey() {
    return BlinkStrikeEnchantment.KEY;
  }

  public static @NotNull TypedKey<@NotNull Enchantment> getTypedKey() {
    return TypedKey.create(RegistryKey.ENCHANTMENT, BlinkStrikeEnchantment.KEY);
  }

  public static @NotNull Consumer<@NotNull Builder> getBuilder(
      final @NotNull RegistryFreezeEvent<@NotNull Enchantment, @NotNull Builder> event) {
    return builder ->
        builder
            .description(Component.text("Blink Strike"))
            .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.SWORDS))
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

    final double MIN_DEFAULT_THETA = -45;
    final double MAX_DEFAULT_THETA = 45;
    final double MIN_THETA_SPREAD = MIN_DEFAULT_THETA * 0.1 * (level - 1);
    final double MAX_THETA_SPREAD = MAX_DEFAULT_THETA * 0.1 * (level - 1);
    final Location attackedLocation = attacked.getLocation();
    final float YAW = attackedLocation.clone().getYaw();
    final double MIN_RADIUS = 2;
    final double MAX_RADIUS = 4;
    final double MIN_THETA = Math.toRadians(MIN_DEFAULT_THETA + MIN_THETA_SPREAD + YAW);
    final double MAX_THETA = Math.toRadians(MAX_DEFAULT_THETA + MAX_THETA_SPREAD + YAW);
    final double MIN_PHI = Math.toRadians(0);
    final double MAX_PHI = Math.toRadians(180);
    boolean isSafeLocationFound = false;
    Location teleportLocation = null;

    while (!isSafeLocationFound) {
      final double RADIUS = MathUtil.getRandomDouble(MIN_RADIUS, MAX_RADIUS);
      final double THETA = MathUtil.getRandomDouble(MIN_THETA, MAX_THETA);
      final double PHI = MathUtil.getRandomDouble(MIN_PHI, MAX_PHI);
      final double OFFSET_X = RADIUS * Math.sin(THETA) * Math.cos(PHI);
      final double OFFSET_Y = RADIUS * Math.sin(THETA) * Math.sin(PHI);
      final double OFFSET_Z = RADIUS * Math.cos(THETA);

      teleportLocation =
          new Location(
              attacked.getWorld(),
              attacked.getX() + OFFSET_X,
              attacked.getY() + OFFSET_Y,
              attacked.getZ() + OFFSET_Z);

      final Block blockAbove = teleportLocation.clone().add(0, 1, 0).getBlock();
      final Block blockAtLocation = teleportLocation.getBlock();
      final Block blockBelow = teleportLocation.clone().subtract(0, 1, 0).getBlock();
      final boolean isBlockAboveSolid = blockAbove.isSolid();
      final boolean isBlockAtLocationSolid = blockAtLocation.isSolid();
      final boolean isBlockBelowSolid = blockBelow.isSolid();

      if (!isBlockAboveSolid && !isBlockAtLocationSolid && isBlockBelowSolid) {
        isSafeLocationFound = true;
      }
    }

    final Vector facingAttacked = attackedLocation.clone().subtract(teleportLocation).toVector();

    teleportLocation.setDirection(facingAttacked);
    attacker.teleport(teleportLocation);
    attacker.playSound(
        Sound.sound(Key.key("minecraft:entity.enderman.teleport"), Source.PLAYER, 1f, 1f));

    teleportLocation
        .getWorld()
        .spawnParticle(Particle.PORTAL, teleportLocation, 5, 0.5, 0.5, 0.5, 0.1);
  }
}
