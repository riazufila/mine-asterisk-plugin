package net.mineasterisk.mc.service;

import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.exception.MissingEntityException;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.PlayerRepository;
import org.bukkit.entity.Player;
import org.hibernate.StatelessSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerService extends Service<PlayerModel> {
  public PlayerService(final @NotNull StatelessSession statelessSession) {
    super(statelessSession);
  }

  public @NotNull CompletableFuture<@Nullable Void> add(
      final @NotNull Player performedBy, final @NotNull PlayerModel playerToAdd) {
    return CompletableFuture.supplyAsync(
        () -> {
          final PlayerRepository playerRepository =
              new PlayerRepository(this.getStatelessSession());

          if (!(performedBy.getUniqueId().equals(playerToAdd.getUuid()))) {
            throw new ValidationException(
                "Not allowed to add other Player",
                String.format(
                    "Player %s is trying to add other Player %s",
                    performedBy.getUniqueId(), playerToAdd.getUuid()));
          }

          final PlayerModel player =
              playerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId()).join();

          if (player != null) {
            throw new ValidationException(
                "Encountered error",
                String.format(
                    "Player %s is trying to add existing Player %s",
                    performedBy.getUniqueId(), player.getUuid()));
          }

          return playerRepository.add(playerToAdd).join();
        });
  }

  public @NotNull CompletableFuture<@Nullable Void> update(
      final @NotNull Player performedBy, final @NotNull PlayerModel playerToUpdate) {
    return CompletableFuture.supplyAsync(
        () -> {
          final PlayerRepository playerRepository =
              new PlayerRepository(this.getStatelessSession());

          if (!(performedBy.getUniqueId().equals(playerToUpdate.getUuid()))) {
            throw new ValidationException(
                "Can't update other Player",
                String.format(
                    "Player %s is trying to update other Player %s",
                    performedBy.getUniqueId(), playerToUpdate.getUuid()));
          }

          final PlayerModel player =
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
