package net.mineasterisk.mc.service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
import org.bukkit.entity.Player;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

public class GuildService extends Service<GuildModel> {
  public GuildService(@NotNull Session session) {
    super(session);
  }

  public @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull Player performedBy, final @NotNull GuildModel guildToAdd) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (!(performedBy.getUniqueId().equals(guildToAdd.getOwner().getUuid()))) {
            throw new ValidationException(
                "Not allowed to add Guild for other Player",
                String.format(
                    "Player %s is trying to add Guild %s for Player %s",
                    performedBy.getUniqueId(),
                    guildToAdd.getName(),
                    guildToAdd.getOwner().getUuid()));
          }

          PlayerRepository playerRepository = new PlayerRepository(this.getSession());
          PlayerModel player =
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

          if (player.getGuild() != null) {
            throw new ValidationException(
                "Already in a Guild",
                String.format(
                    "Player %s have an existing Guild %s",
                    performedBy.getUniqueId(), player.getGuild().getName()));
          }

          GuildRepository guildRepository = new GuildRepository(this.getSession());

          return guildRepository.add(guildToAdd).join();
        });
  }

  public @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull Player performedBy, final @NotNull GuildModel guildToUpdate) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (!(performedBy.getUniqueId().equals(guildToUpdate.getOwner().getUuid()))) {
            throw new ValidationException(
                "Not allowed to update Guild for other Player",
                String.format(
                    "Player %s is trying to update Guild %s for Player %s",
                    performedBy.getUniqueId(),
                    guildToUpdate.getName(),
                    guildToUpdate.getOwner().getUuid()));
          }

          PlayerModel player =
              new PlayerRepository(this.getSession())
                  .get(PlayerAttribute.UUID, performedBy.getUniqueId())
                  .join();

          if (player == null) {
            throw new MissingEntityException(
                "Encountered error",
                String.format("Player %s is not initialized", performedBy.getUniqueId()),
                PlayerModel.class);
          }

          GuildRepository guildRepository = new GuildRepository(this.getSession());
          GuildModel guild = guildRepository.get(GuildAttribute.ID, guildToUpdate.getId()).join();

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
