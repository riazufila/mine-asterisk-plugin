package net.mineasterisk.mc.service;

import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerService {
  public static @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull Player performedBy, final @NotNull PlayerModel playerToAdd) {
    if (performedBy.getUniqueId() != playerToAdd.getUuid()) {
      PluginUtil.getLogger().info("Unable to add Player: Not allowed to add other Player");

      return CompletableFuture.completedFuture(null);
    }

    return PlayerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId())
        .thenCompose(
            player -> {
              if (player != null) {
                PluginUtil.getLogger().info("Unable to add Player: Player already exist");

                return CompletableFuture.completedFuture(null);
              }

              return PlayerRepository.add(playerToAdd);
            });
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull Player performedBy, final @NotNull PlayerModel updatedPlayer) {
    if (performedBy.getUniqueId() != updatedPlayer.getUuid()) {
      PluginUtil.getLogger().info("Unable to update Player: Cannot update other Player");

      return CompletableFuture.completedFuture(null);
    }

    return PlayerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId())
        .thenCompose(
            player -> {
              if (player == null) {
                PluginUtil.getLogger().info("Unable to update Player: Player doesn't exist");

                return CompletableFuture.completedFuture(null);
              }

              return PlayerRepository.update(updatedPlayer);
            });
  }
}
