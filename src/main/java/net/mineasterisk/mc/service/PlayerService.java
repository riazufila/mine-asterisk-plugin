package net.mineasterisk.mc.service;

import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerService {
  public static @NotNull CompletableFuture<@NotNull Boolean> add(
      final @NotNull Player performedBy, final @NotNull PlayerModel playerToAdd) {
    final LoggerUtil logger = new LoggerUtil(performedBy, "Added Player", "Unable to add Player");

    if (!(performedBy.getUniqueId().equals(playerToAdd.getUuid()))) {
      logger.warn(
          "Not allowed to add other Player",
          String.format(
              "Player %s is trying to add other Player %s",
              performedBy.getUniqueId(), playerToAdd.getUuid()));

      return CompletableFuture.completedFuture(false);
    }

    return PlayerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId())
        .thenCompose(
            player -> {
              if (player != null) {
                logger.warn(
                    "Encountered error",
                    String.format(
                        "Player %s is trying to add existing Player %s",
                        performedBy.getUniqueId(), playerToAdd.getUuid()));

                return CompletableFuture.completedFuture(false);
              }

              return PlayerRepository.add(playerToAdd).thenApply(object -> true);
            })
        .exceptionally(
            exception -> {
              logger.error(
                  "Encountered error",
                  String.format(
                      "Player %s encountered error trying to add Player %s",
                      performedBy.getUniqueId(), playerToAdd.getUuid()));

              return false;
            });
  }

  public static @NotNull CompletableFuture<@NotNull Boolean> update(
      final @NotNull Player performedBy, final @NotNull PlayerModel playerToUpdate) {
    final LoggerUtil logger =
        new LoggerUtil(performedBy, "Updated Player", "Unable to update Player");

    if (!(performedBy.getUniqueId().equals(playerToUpdate.getUuid()))) {
      logger.warn(
          "Can't update other Player",
          String.format(
              "Player %s is trying to update other Player %s",
              performedBy.getUniqueId(), playerToUpdate.getUuid()));

      return CompletableFuture.completedFuture(false);
    }

    return PlayerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId())
        .thenCompose(
            player -> {
              if (player == null) {
                logger.warn(
                    "Encountered error",
                    String.format(
                        "Player %s is trying to update non-existent Player %s",
                        performedBy.getUniqueId(), playerToUpdate.getUuid()));

                return CompletableFuture.completedFuture(false);
              }

              return PlayerRepository.update(playerToUpdate).thenApply(object -> true);
            })
        .exceptionally(
            exception -> {
              logger.error(
                  "Encountered error",
                  String.format(
                      "Player %s encountered error trying to add Player %s",
                      performedBy.getUniqueId(), playerToUpdate.getUuid()));

              return false;
            });
  }
}
