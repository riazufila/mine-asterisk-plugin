package net.mineasterisk.mc.service.LivingEntity;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class LivingEntityService {
  private final @NotNull LivingEntity livingEntity;

  public LivingEntityService(final @NotNull LivingEntity livingEntity) {
    this.livingEntity = livingEntity;
  }

  public void teleport(final @NotNull Location destination) {
    final World sourceWorld = this.livingEntity.getWorld();
    final World destinationWorld = destination.getWorld();
    final Location sourceLocation = this.livingEntity.getLocation();

    sourceWorld.playSound(sourceLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
    this.spawnParticle(0.5, 1, new DustOptions(Color.PURPLE, 1));

    this.livingEntity.teleport(destination);

    destinationWorld.playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
    this.spawnParticle(0.5, 1, new DustOptions(Color.PURPLE, 1));
  }

  public <T> void spawnParticle(final double density, final int count, final T dustConfiguration) {
    final BoundingBox boundingBox = this.livingEntity.getBoundingBox();
    final World world = this.livingEntity.getWorld();

    for (double x = boundingBox.getMinX(); x <= boundingBox.getMaxX(); x += density) {
      for (double y = boundingBox.getMinY(); y <= boundingBox.getMaxY(); y += density) {
        for (double z = boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z += density) {
          world.spawnParticle(Particle.DUST, x, y, z, count, dustConfiguration);
        }
      }
    }
  }
}
