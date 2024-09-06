package net.mineasterisk.mc.service.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerService {
  private final @NotNull Player player;

  public PlayerService(final @NotNull Player player) {
    this.player = player;
  }
}
