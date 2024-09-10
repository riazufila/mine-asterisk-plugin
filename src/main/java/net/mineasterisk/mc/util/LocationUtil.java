package net.mineasterisk.mc.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class LocationUtil {
  public static boolean isLocationAbleToFitPlayer(final @NotNull Location location) {
    final Block blockAbove = location.clone().add(0, 1, 0).getBlock();
    final Block blockAtLocation = location.getBlock();
    final Block blockBelow = location.clone().subtract(0, 1, 0).getBlock();
    final boolean isBlockAboveSolid = blockAbove.isSolid();
    final boolean isBlockAtLocationSolid = blockAtLocation.isSolid();
    final boolean isBlockBelowSolid = blockBelow.isSolid();

    return !isBlockAboveSolid && !isBlockAtLocationSolid && isBlockBelowSolid;
  }
}
