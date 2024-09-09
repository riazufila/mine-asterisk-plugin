package net.mineasterisk.mc.service.LivingEntity;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
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
    sourceWorld.spawnParticle(Particle.PORTAL, sourceLocation, 5, 0.5, 0.5, 0.5);

    this.livingEntity.teleport(destination);

    destinationWorld.playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
    destinationWorld.spawnParticle(Particle.PORTAL, destination, 5, 0.5, 0.5, 0.5);
  }
}
