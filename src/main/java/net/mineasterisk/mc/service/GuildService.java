package net.mineasterisk.mc.service;

import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.constant.GuildStatus;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.repository.GuildRepository;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.repository.option.attribute.GuildRepositoryOptionAttribute;
import net.mineasterisk.mc.repository.option.attribute.PlayerRepositoryOptionAttribute;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class GuildService {
  public static @NotNull CompletableFuture<@NotNull Void> add(@NotNull GuildModel guild) {
    return GuildRepository.add(guild);
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      @NotNull Player performedBy, @NotNull GuildModel updatedGuild) {
    return PlayerRepository.get(PlayerRepositoryOptionAttribute.UUID, performedBy.getUniqueId())
        .thenCompose(
            player -> {
              if (player == null) {
                MineAsterisk.getPluginLogger()
                    .info("Unable to update Guild: Player doesn't exist.");

                return CompletableFuture.completedFuture(null);
              }

              GuildModel playerGuild = player.getGuild();

              if (playerGuild == null) {
                MineAsterisk.getPluginLogger()
                    .info("Unable to update Guild: Player doesn't have a Guild.");

                return CompletableFuture.completedFuture(null);
              }

              if (playerGuild.getOwner().getId() != player.getId()) {
                MineAsterisk.getPluginLogger()
                    .info("Unable to update Guild: Player is not the Guild owner.");

                return CompletableFuture.completedFuture(null);
              }

              if (playerGuild.getStatus() == GuildStatus.INACTIVE) {
                MineAsterisk.getPluginLogger().info("Unable to update Guild: Guild is inactive.");

                return CompletableFuture.completedFuture(null);
              }

              return GuildRepository.get(GuildRepositoryOptionAttribute.ID, playerGuild.getId())
                  .thenCompose(guild -> GuildRepository.update(updatedGuild));
            });
  }
}
