package net.mineasterisk.mc.service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.forcefetch.PlayerForceFetch;
import net.mineasterisk.mc.model.InvitationModel;
import net.mineasterisk.mc.repository.InvitationRepository;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.util.LoggerUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InvitationService {
  public static @NotNull CompletableFuture<@NotNull Boolean> add(
      final @NotNull Player performedBy, final @NotNull InvitationModel invitationToAdd) {
    final LoggerUtil logger =
        new LoggerUtil(performedBy, "Added Guild invitation", "Unable to add Guild invitation");

    if (!(performedBy.getUniqueId().equals(invitationToAdd.getInviter().getUuid()))) {
      logger.warn(
          "Not allowed to send Guild invitation for other Player",
          String.format(
              "Player %s is trying to send Player %s invitation to Guild %s for Player %s",
              performedBy.getUniqueId(),
              invitationToAdd.getInvitee().getUuid(),
              invitationToAdd.getGuild().getName(),
              invitationToAdd.getInviter().getUuid()));

      return CompletableFuture.completedFuture(false);
    }

    return PlayerRepository.get(
            PlayerAttribute.UUID, performedBy.getUniqueId(), Set.of(PlayerForceFetch.GUILD))
        .thenCompose(
            inviter -> {
              if (inviter == null) {
                logger.warn(
                    "Encountered error",
                    String.format(
                        String.format("Inviter %s is not initialized", performedBy.getUniqueId())));

                return CompletableFuture.completedFuture(false);
              }

              if (inviter.getGuild() == null) {
                logger.warn(
                    "Must be in a Guild to send a Guild invitation",
                    String.format(
                        "Inviter %s is trying to send Player %s invitation to Guild %s, but isn't in a Guild",
                        performedBy.getUniqueId(),
                        invitationToAdd.getInvitee().getUuid(),
                        invitationToAdd.getGuild().getName()));

                return CompletableFuture.completedFuture(false);
              }

              if (inviter.getGuild().getOwner().getId() == inviter.getId()) {
                logger.warn(
                    "Must be a Guild owner to send a Guild invitation",
                    String.format(
                        "Inviter %s is trying to send Player %s invitation to Guild %s, but isn't a Guild owner",
                        performedBy.getUniqueId(),
                        invitationToAdd.getInvitee().getUuid(),
                        invitationToAdd.getGuild().getName()));

                return CompletableFuture.completedFuture(false);
              }

              return PlayerRepository.get(
                      PlayerAttribute.UUID,
                      invitationToAdd.getInvitee().getUuid(),
                      Set.of(PlayerForceFetch.GUILD))
                  .thenCompose(
                      invitee -> {
                        if (invitee == null) {
                          logger.warn(
                              "Encountered error",
                              String.format(
                                  String.format(
                                      "Invitee %s is not initialized",
                                      invitationToAdd.getInvitee().getId())));

                          return CompletableFuture.completedFuture(false);
                        }

                        if (invitee.getGuild() != null) {
                          logger.warn(
                              "Invitee is already on a Guild",
                              String.format(
                                  "Inviter %s is trying to send Invitee %s invitation to Guild %s, but Invitee is already on a Guild",
                                  performedBy.getUniqueId(),
                                  invitationToAdd.getInvitee().getUuid(),
                                  invitationToAdd.getGuild().getName()));

                          return CompletableFuture.completedFuture(false);
                        }

                        return InvitationRepository.add(invitationToAdd).thenApply(object -> true);
                      });
            })
        .exceptionally(
            exception -> {
              logger.error(
                  "Encountered error",
                  String.format(
                      "Player %s encountered error while trying to send Player %s invitation to Guild %s",
                      performedBy.getUniqueId(),
                      invitationToAdd.getInvitee().getUuid(),
                      invitationToAdd.getGuild().getName()));

              return false;
            });
  }

  public static @NotNull CompletableFuture<@NotNull Boolean> update(
      final @NotNull Player performedBy, final @NotNull InvitationModel invitationToUpdate) {
    final LoggerUtil logger =
        new LoggerUtil(
            performedBy, "Updated Guild invitation", "Unable to update Guild invitation");

    if (!(performedBy.getUniqueId().equals(invitationToUpdate.getInviter().getUuid())
        || performedBy.getUniqueId().equals(invitationToUpdate.getInvitee().getUuid()))) {
      logger.warn(
          "Not allowed to update Guild invitation if not Inviter nor Invitee",
          String.format(
              "Player %s is trying to update Player %s's invitation to Guild %s but is neither the Inviter or Invitee",
              performedBy.getUniqueId(),
              invitationToUpdate.getInvitee().getUuid(),
              invitationToUpdate.getGuild().getName()));

      return CompletableFuture.completedFuture(false);
    }

    return PlayerRepository.get(
            PlayerAttribute.UUID, performedBy.getUniqueId(), Set.of(PlayerForceFetch.GUILD))
        .thenCompose(
            inviter -> {
              if (inviter == null) {
                logger.warn(
                    "Encountered error",
                    String.format(
                        String.format(
                            "Inviter %s is not initialized",
                            invitationToUpdate.getInvitee().getId())));

                return CompletableFuture.completedFuture(false);
              }

              if (inviter.getGuild() == null) {
                logger.warn(
                    "Must be in a Guild to update a Guild invitation",
                    String.format(
                        "Inviter %s is trying to update Player %s's invitation to Guild %s, but isn't in a Guild",
                        performedBy.getUniqueId(),
                        invitationToUpdate.getInvitee().getUuid(),
                        invitationToUpdate.getGuild().getName()));

                return CompletableFuture.completedFuture(false);
              }

              if (inviter.getGuild().getOwner().getId() == inviter.getId()) {
                logger.warn(
                    "Must be a Guild owner to update a Guild invitation",
                    String.format(
                        "Inviter %s is trying to update Player %s's invitation to Guild %s, but isn't a Guild owner",
                        performedBy.getUniqueId(),
                        invitationToUpdate.getInvitee().getUuid(),
                        invitationToUpdate.getGuild().getName()));

                return CompletableFuture.completedFuture(false);
              }

              return PlayerRepository.get(
                      PlayerAttribute.UUID,
                      invitationToUpdate.getInvitee().getUuid(),
                      Set.of(PlayerForceFetch.GUILD))
                  .thenCompose(
                      invitee -> {
                        if (invitee == null) {
                          logger.warn(
                              "Encountered error",
                              String.format(
                                  String.format(
                                      "Invitee %s is not initialized",
                                      invitationToUpdate.getInvitee().getId())));

                          return CompletableFuture.completedFuture(false);
                        }

                        if (invitee.getGuild() != null) {
                          logger.warn(
                              "Invitee is already on a Guild",
                              String.format(
                                  "Inviter %s is trying to update Invitee %s's invitation to Guild %s, but Invitee is already on a Guild",
                                  performedBy.getUniqueId(),
                                  invitationToUpdate.getInvitee().getUuid(),
                                  invitationToUpdate.getGuild().getName()));

                          return CompletableFuture.completedFuture(false);
                        }

                        return InvitationRepository.update(invitationToUpdate)
                            .thenApply(object -> true);
                      });
            })
        .exceptionally(
            exception -> {
              logger.error(
                  "Encountered error",
                  String.format(
                      "Player %s encountered error while trying to update Player %s's invitation to Guild %s",
                      performedBy.getUniqueId(),
                      invitationToUpdate.getInvitee().getUuid(),
                      invitationToUpdate.getGuild().getName()));

              return false;
            });
  }
}
