package net.mineasterisk.mc.service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.GuildStatus;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.repository.GuildRepository;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.repository.option.attribute.PlayerRepositoryOptionAttribute;
import net.mineasterisk.mc.repository.option.forcefetch.PlayerRepositoryOptionForceFetch;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GuildService {
  public static @NotNull CompletableFuture<@NotNull Void> add(
      @NotNull Player performedBy, @NotNull GuildModel guildToAdd) {

    return PlayerRepository.get(
            PlayerRepositoryOptionAttribute.UUID,
            performedBy.getUniqueId(),
            Set.of(PlayerRepositoryOptionForceFetch.GUILD))
        .thenCompose(
            player -> {
              if (player == null) {
                PluginUtil.getLogger().info("Unable to add Guild: Player doesn't exist.");

                return CompletableFuture.completedFuture(null);
              }

              if (player.getGuild() != null) {
                PluginUtil.getLogger().info("Unable to add Guild: Already on a Guild.");

                return CompletableFuture.completedFuture(null);
              }

              return GuildRepository.add(guildToAdd);
            });
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      @NotNull Player performedBy, @NotNull GuildModel updatedGuild) {
    return PlayerRepository.get(PlayerRepositoryOptionAttribute.UUID, performedBy.getUniqueId())
        .thenCompose(
            player -> {
              if (player == null) {
                PluginUtil.getLogger().info("Unable to update Guild: Player doesn't exist.");

                return CompletableFuture.completedFuture(null);
              }

              GuildModel playerGuild = player.getGuild();

              if (playerGuild == null) {
                PluginUtil.getLogger().info("Unable to update Guild: Player doesn't have a Guild.");

                return CompletableFuture.completedFuture(null);
              }

              if (playerGuild.getOwner().getId() != player.getId()) {
                PluginUtil.getLogger()
                    .info("Unable to update Guild: Player is not the Guild owner.");

                return CompletableFuture.completedFuture(null);
              }

              if (playerGuild.getStatus() == GuildStatus.INACTIVE) {
                PluginUtil.getLogger().info("Unable to update Guild: Guild is inactive.");

                return CompletableFuture.completedFuture(null);
              }

              return GuildRepository.update(updatedGuild);
            });
  }
}
