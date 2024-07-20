package net.mineasterisk.mc.service;

import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.repository.option.attribute.PlayerRepositoryOptionAttribute;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PlayerService {
  public static @NotNull CompletableFuture<@NotNull Void> add(
      @NotNull Player performedBy, @NotNull PlayerModel playerToAdd) {
    if (performedBy.getUniqueId() != playerToAdd.getUuid()) {
      MineAsterisk.getPluginLogger().info("Unable to add Player: Not allowed to add other Player.");

      return CompletableFuture.completedFuture(null);
    }

    return PlayerRepository.get(PlayerRepositoryOptionAttribute.UUID, performedBy.getUniqueId())
        .thenCompose(
            player -> {
              if (player != null) {
                MineAsterisk.getPluginLogger().info("Unable to add Player: Player already exist.");

                return CompletableFuture.completedFuture(null);
              }

              return PlayerRepository.add(playerToAdd);
            });
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      @NotNull Player performedBy, @NotNull PlayerModel updatedPlayer) {
    return PlayerRepository.get(PlayerRepositoryOptionAttribute.UUID, performedBy.getUniqueId())
        .thenCompose(
            player -> {
              if (player == null) {
                MineAsterisk.getPluginLogger()
                    .info("Unable to update Player: Player doesn't exist.");

                return CompletableFuture.completedFuture(null);
              }

              if (performedBy.getUniqueId() != player.getUuid()) {
                MineAsterisk.getPluginLogger()
                    .info("Unable to update Player: Cannot update other Player.");

                return CompletableFuture.completedFuture(null);
              }

              return PlayerRepository.update(updatedPlayer);
            });
  }
}
