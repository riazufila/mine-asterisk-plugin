package net.mineasterisk.mc.service.livingentity;

import net.mineasterisk.mc.util.LocationUtil;
import net.mineasterisk.mc.util.MathUtil;
import net.mineasterisk.mc.util.ParticleUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class LivingEntityService {
  private final @NotNull LivingEntity livingEntity;

  public LivingEntityService(final @NotNull LivingEntity livingEntity) {
    this.livingEntity = livingEntity;
  }

  public void teleportAround(final @NotNull LivingEntity targetLivingEntity, final int spread) {
    final double MIN_DEFAULT_THETA = -45;
    final double MAX_DEFAULT_THETA = 45;
    final double MIN_THETA_SPREAD = MIN_DEFAULT_THETA * 0.1 * spread;
    final double MAX_THETA_SPREAD = MAX_DEFAULT_THETA * 0.1 * spread;
    final Location targetLivingEntityLocation = targetLivingEntity.getLocation();
    final float YAW = targetLivingEntityLocation.getYaw();
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
              targetLivingEntity.getWorld(),
              targetLivingEntity.getX() + OFFSET_X,
              targetLivingEntity.getY() + OFFSET_Y,
              targetLivingEntity.getZ() + OFFSET_Z);

      isSafeLocationFound = LocationUtil.isLocationAbleToFitPlayer(teleportLocation);
    }

    final Vector facingAttacked =
        targetLivingEntityLocation.clone().subtract(teleportLocation).toVector();

    teleportLocation.setDirection(facingAttacked);
    this.teleport(teleportLocation);
  }

  public void teleport(final @NotNull Location destination) {
    final World sourceWorld = this.livingEntity.getWorld();
    final World destinationWorld = destination.getWorld();
    final Location sourceLocation = this.livingEntity.getLocation();

    sourceWorld.playSound(sourceLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
    ParticleUtil.spawnDustOnBoundingBox(
        this.livingEntity, 0.5, 1, new DustOptions(Color.PURPLE, 1));

    this.livingEntity.teleport(destination);

    destinationWorld.playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
    ParticleUtil.spawnDustOnBoundingBox(
        this.livingEntity, 0.5, 1, new DustOptions(Color.PURPLE, 1));
  }
}
