package net.mineasterisk.mc.util;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

public class ParticleUtil {
  public static <T> void spawnDustOnBoundingBox(
      final @NotNull Entity entity,
      final double density,
      final int count,
      final T dustConfiguration) {
    final BoundingBox boundingBox = entity.getBoundingBox();
    final World world = entity.getWorld();

    for (double x = boundingBox.getMinX(); x <= boundingBox.getMaxX(); x += density) {
      for (double y = boundingBox.getMinY(); y <= boundingBox.getMaxY(); y += density) {
        for (double z = boundingBox.getMinZ(); z <= boundingBox.getMaxZ(); z += density) {
          world.spawnParticle(Particle.DUST, x, y, z, count, dustConfiguration);
        }
      }
    }
  }
}
