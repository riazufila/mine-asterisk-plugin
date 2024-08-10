package net.mineasterisk.mc.service;

import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.exception.MissingEntityException;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.PlayerRepository;
import org.bukkit.entity.Player;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

public class PlayerService extends Service<PlayerModel> {
  public PlayerService(@NotNull Session session) {
    super(session);
  }

  public @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull Player performedBy, final @NotNull PlayerModel playerToAdd) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (!(performedBy.getUniqueId().equals(playerToAdd.getUuid()))) {
            throw new ValidationException(
                "Not allowed to add other Player",
                String.format(
                    "Player %s is trying to add other Player %s",
                    performedBy.getUniqueId(), playerToAdd.getUuid()));
          }

          PlayerRepository playerRepository = new PlayerRepository(this.getSession());
          PlayerModel player =
              playerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId()).join();

          if (player != null) {
            throw new ValidationException(
                "Encountered error",
                String.format(
                    "Player %s is trying to add existing Player %s",
                    performedBy.getUniqueId(), playerToAdd.getUuid()));
          }

          return playerRepository.add(playerToAdd).join();
        });
  }

  public @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull Player performedBy, final @NotNull PlayerModel playerToUpdate) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (!(performedBy.getUniqueId().equals(playerToUpdate.getUuid()))) {
            throw new ValidationException(
                "Can't update other Player",
                String.format(
                    "Player %s is trying to update other Player %s",
                    performedBy.getUniqueId(), playerToUpdate.getUuid()));
          }

          PlayerRepository playerRepository = new PlayerRepository(this.getSession());
          PlayerModel player =
              playerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId()).join();

          if (player == null) {
            throw new MissingEntityException(
                "Encountered error",
                String.format(
                    "Player %s is trying to update non-existent Player %s",
                    performedBy.getUniqueId(), playerToUpdate.getUuid()),
                PlayerModel.class);
          }

          return playerRepository.update(playerToUpdate).join();
        });
  }
}
