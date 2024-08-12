package net.mineasterisk.mc.service;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.constant.attribute.GuildAttribute;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.forcefetch.PlayerForceFetch;
import net.mineasterisk.mc.constant.status.GuildStatus;
import net.mineasterisk.mc.exception.MissingEntityException;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.GuildRepository;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.hibernate.StatelessSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildService extends Service<GuildModel> {
  public GuildService(final @NotNull StatelessSession statelessSession) {
    super(statelessSession);
  }

  public @NotNull CompletableFuture<@Nullable Void> create(
      final @NotNull Player performedBy, final @NotNull String name) {
    return CompletableFuture.supplyAsync(
        () -> {
          final PlayerRepository playerRepository =
              new PlayerRepository(this.getStatelessSession());

          final GuildRepository guildRepository = new GuildRepository(this.getStatelessSession());
          final PlayerService playerService = new PlayerService(this.getStatelessSession());
          final PlayerModel player =
              playerRepository
                  .get(
                      PlayerAttribute.UUID,
                      performedBy.getUniqueId(),
                      Set.of(PlayerForceFetch.GUILD))
                  .join();

          if (player == null) {
            throw new MissingEntityException(
                "Encountered error",
                String.format("Player %s is not initialized", performedBy.getUniqueId()),
                PlayerModel.class);
          }

          final GuildModel guild =
              new GuildModel(
                  Instant.now(),
                  player,
                  null,
                  null,
                  name,
                  player,
                  GuildStatus.ACTIVE,
                  Collections.emptySet());

          if (!(performedBy.getUniqueId().equals(guild.getOwner().getUuid()))) {
            throw new ValidationException(
                "Not allowed to add Guild for other Player",
                String.format(
                    "Player %s is trying to add Guild %s for Player %s",
                    performedBy.getUniqueId(), guild.getName(), guild.getOwner().getUuid()));
          }

          if (player.getGuild() != null) {
            throw new ValidationException(
                "Already in a Guild",
                String.format(
                    "Player %s have an existing Guild %s",
                    performedBy.getUniqueId(), player.getGuild().getName()));
          }

          guildRepository.add(guild).join();
          player.setGuild(guild);
          playerService.update(performedBy, player).join();

          final Scoreboard scoreboard = PluginUtil.getMainScoreboard();
          final Team team = scoreboard.registerNewTeam(name);

          team.addEntity(performedBy);
          team.displayName(Component.text(name));
          team.prefix(
              Component.textOfChildren(
                  Component.text(name).color(NamedTextColor.GRAY),
                  Component.text('.').color(NamedTextColor.GRAY)));

          return null;
        });
  }

  public @NotNull CompletableFuture<@Nullable Void> update(
      final @NotNull Player performedBy, final @NotNull GuildModel guildToUpdate) {
    return CompletableFuture.supplyAsync(
        () -> {
          final PlayerRepository playerRepository =
              new PlayerRepository(this.getStatelessSession());
          final GuildRepository guildRepository = new GuildRepository(this.getStatelessSession());
          if (!(performedBy.getUniqueId().equals(guildToUpdate.getOwner().getUuid()))) {
            throw new ValidationException(
                "Not allowed to update Guild for other Player",
                String.format(
                    "Player %s is trying to update Guild %s for Player %s",
                    performedBy.getUniqueId(),
                    guildToUpdate.getName(),
                    guildToUpdate.getOwner().getUuid()));
          }

          final PlayerModel player =
              playerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId()).join();

          if (player == null) {
            throw new MissingEntityException(
                "Encountered error",
                String.format("Player %s is not initialized", performedBy.getUniqueId()),
                PlayerModel.class);
          }

          final GuildModel guild =
              guildRepository.get(GuildAttribute.ID, guildToUpdate.getId()).join();

          if (guild == null) {
            throw new MissingEntityException(
                "Encountered error",
                String.format(
                    "Player %s is trying to update a non-existent Guild %s",
                    performedBy.getUniqueId(), guildToUpdate.getName()),
                GuildModel.class);
          }

          if (guild.getOwner().getId() != player.getId()) {
            throw new ValidationException(
                "Must be the Guild owner to update Guild",
                String.format(
                    "Player %s is not the owner of Guild %s",
                    performedBy.getUniqueId(), guild.getName()));
          }

          if (guild.getStatus() == GuildStatus.INACTIVE) {
            throw new ValidationException(
                "Guild is inactive",
                String.format(
                    "Player %s is trying to update an inactive Guild %s",
                    performedBy.getUniqueId(), guild.getName()));
          }

          return guildRepository.update(guildToUpdate).join();
        });
  }
}
