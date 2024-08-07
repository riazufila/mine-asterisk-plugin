package net.mineasterisk.mc.service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.GuildAttribute;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.forcefetch.PlayerForceFetch;
import net.mineasterisk.mc.constant.status.GuildStatus;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.repository.GuildRepository;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GuildService {
  public static @NotNull CompletableFuture<@NotNull Boolean> add(
      final @NotNull Player performedBy, final @NotNull GuildModel guildToAdd) {
    final LoggerUtil logger = new LoggerUtil(performedBy, "Added Guild", "Unable to add Guild");

    if (!(performedBy.getUniqueId().equals(guildToAdd.getOwner().getUuid()))) {
      logger.warn(
          "Not allowed to add Guild for other Player",
          String.format(
              "Player %s is trying to add Guild %s for Player %s",
              performedBy.getUniqueId(), guildToAdd.getName(), guildToAdd.getOwner().getUuid()));

      return CompletableFuture.completedFuture(false);
    }

    return PlayerRepository.get(
            PlayerAttribute.UUID, performedBy.getUniqueId(), Set.of(PlayerForceFetch.GUILD))
        .thenCompose(
            player -> {
              if (player == null) {
                logger.warn(
                    "Player doesn't exist",
                    String.format("Player %s is not initialized", performedBy.getUniqueId()));

                return CompletableFuture.completedFuture(false);
              }

              if (player.getGuild() != null) {
                logger.warn(
                    "Already on a Guild",
                    String.format(
                        "Player %s have an existing Guild %s",
                        performedBy.getUniqueId(), guildToAdd.getName()));

                return CompletableFuture.completedFuture(false);
              }

              return GuildRepository.add(guildToAdd).thenApply((object) -> true);
            })
        .exceptionally(
            exception -> {
              logger.error(
                  "Encountered error",
                  String.format(
                      "Player %s encountered error trying to add Guild %s",
                      performedBy.getUniqueId(), guildToAdd.getName()));

              return false;
            });
  }

  public static @NotNull CompletableFuture<@NotNull Boolean> update(
      final @NotNull Player performedBy, final @NotNull GuildModel updatedGuild) {
    final LoggerUtil logger =
        new LoggerUtil(performedBy, "Updated Guild", "Unable to update Guild");

    if (!(performedBy.getUniqueId().equals(updatedGuild.getOwner().getUuid()))) {
      logger.warn(
          "Not allowed to update Guild for other Player",
          String.format(
              "Player %s is trying to update Guild %s for Player %s",
              performedBy.getUniqueId(),
              updatedGuild.getName(),
              updatedGuild.getOwner().getUuid()));

      return CompletableFuture.completedFuture(false);
    }

    return PlayerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId())
        .thenCompose(
            player -> {
              if (player == null) {
                logger.warn(
                    "Player doesn't exist",
                    String.format("Player %s is not initialized", performedBy.getUniqueId()));

                return CompletableFuture.completedFuture(false);
              }

              return GuildRepository.get(GuildAttribute.ID, updatedGuild.getId())
                  .thenCompose(
                      guild -> {
                        if (guild == null) {
                          logger.warn(
                              "Guild doesn't exist",
                              String.format(
                                  "Player %s is trying to update a non-existent Guild %s",
                                  performedBy.getUniqueId(), updatedGuild.getName()));

                          return CompletableFuture.completedFuture(false);
                        }

                        if (guild.getOwner().getId() != player.getId()) {
                          logger.warn(
                              "Player is not the Guild owner",
                              String.format(
                                  "Player %s is not the owner of Guild %s",
                                  performedBy.getUniqueId(), updatedGuild.getName()));

                          return CompletableFuture.completedFuture(false);
                        }

                        if (guild.getStatus() == GuildStatus.INACTIVE) {
                          logger.warn(
                              "Guild is inactive",
                              String.format(
                                  "Player %s is trying to update an inactive Guild %s",
                                  performedBy.getUniqueId(), updatedGuild.getName()));

                          return CompletableFuture.completedFuture(false);
                        }

                        return GuildRepository.update(updatedGuild).thenApply((object) -> true);
                      });
            })
        .exceptionally(
            exception -> {
              logger.error(
                  "Encountered error",
                  String.format(
                      "Player %s encountered error trying to add Guild %s",
                      performedBy.getUniqueId(), updatedGuild.getName()));

              return false;
            });
  }
}
