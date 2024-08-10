package net.mineasterisk.mc.service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.forcefetch.PlayerForceFetch;
import net.mineasterisk.mc.exception.MissingEntityException;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.model.InvitationModel;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.InvitationRepository;
import net.mineasterisk.mc.repository.PlayerRepository;
import org.bukkit.entity.Player;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

public class InvitationService extends Service<InvitationModel> {
  public InvitationService(@NotNull Session session) {
    super(session);
  }

  public @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull Player performedBy, final @NotNull InvitationModel invitationToAdd) {

    return CompletableFuture.supplyAsync(
        () -> {
          if (!(performedBy.getUniqueId().equals(invitationToAdd.getInviter().getUuid()))) {
            throw new ValidationException(
                "Not allowed to send Guild invitation for other Player",
                String.format(
                    "Player %s is trying to send Player %s invitation to Guild %s for Player %s",
                    performedBy.getUniqueId(),
                    invitationToAdd.getInvitee().getUuid(),
                    invitationToAdd.getGuild().getName(),
                    invitationToAdd.getInviter().getUuid()));
          }

          PlayerRepository playerRepository = new PlayerRepository(this.getSession());
          PlayerModel inviter =
              playerRepository
                  .get(
                      PlayerAttribute.UUID,
                      performedBy.getUniqueId(),
                      Set.of(PlayerForceFetch.GUILD))
                  .join();

          if (inviter == null) {
            throw new MissingEntityException(
                "Encountered error",
                String.format(
                    String.format("Inviter %s is not initialized", performedBy.getUniqueId())),
                PlayerModel.class);
          }

          if (inviter.getGuild() == null) {
            throw new ValidationException(
                "Must be in a Guild to send a Guild invitation",
                String.format(
                    "Inviter %s is trying to send Player %s invitation to Guild %s, but isn't in a Guild",
                    performedBy.getUniqueId(),
                    invitationToAdd.getInvitee().getUuid(),
                    invitationToAdd.getGuild().getName()));
          }

          if (inviter.getGuild().getId() == invitationToAdd.getGuild().getId()) {
            throw new ValidationException(
                "Must be in the Guild to send a Guild invitation",
                String.format(
                    "Inviter %s is trying to send Player %s invitation to Guild %s, but isn't in the Guild",
                    performedBy.getUniqueId(),
                    invitationToAdd.getInvitee().getUuid(),
                    invitationToAdd.getGuild().getName()));
          }

          if (inviter.getGuild().getOwner().getId() == inviter.getId()) {
            throw new ValidationException(
                "Must be a Guild owner to send a Guild invitation",
                String.format(
                    "Inviter %s is trying to send Player %s invitation to Guild %s, but isn't a Guild owner",
                    performedBy.getUniqueId(),
                    invitationToAdd.getInvitee().getUuid(),
                    invitationToAdd.getGuild().getName()));
          }

          PlayerModel invitee =
              playerRepository
                  .get(
                      PlayerAttribute.UUID,
                      invitationToAdd.getInvitee().getUuid(),
                      Set.of(PlayerForceFetch.GUILD))
                  .join();

          if (invitee == null) {
            throw new MissingEntityException(
                "Encountered error",
                String.format(
                    String.format(
                        "Invitee %s is not initialized", invitationToAdd.getInvitee().getId())),
                PlayerModel.class);
          }

          if (invitee.getGuild() != null) {
            throw new ValidationException(
                "Invitee is already in a Guild",
                String.format(
                    "Inviter %s is trying to send Invitee %s invitation to Guild %s, but Invitee is already in a Guild",
                    performedBy.getUniqueId(),
                    invitationToAdd.getInvitee().getUuid(),
                    invitationToAdd.getGuild().getName()));
          }

          InvitationRepository invitationRepository = new InvitationRepository(this.getSession());

          return invitationRepository.add(invitationToAdd).join();
        });
  }

  public @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull Player performedBy, final @NotNull InvitationModel invitationToUpdate) {
    return CompletableFuture.supplyAsync(
        () -> {
          if (!(performedBy.getUniqueId().equals(invitationToUpdate.getInviter().getUuid())
              || performedBy.getUniqueId().equals(invitationToUpdate.getInvitee().getUuid()))) {
            throw new ValidationException(
                "Not allowed to update Guild invitation if not Inviter nor Invitee",
                String.format(
                    "Player %s is trying to update Player %s's invitation to Guild %s but is neither the Inviter or Invitee",
                    performedBy.getUniqueId(),
                    invitationToUpdate.getInvitee().getUuid(),
                    invitationToUpdate.getGuild().getName()));
          }

          PlayerRepository playerRepository = new PlayerRepository(this.getSession());
          PlayerModel inviter =
              playerRepository
                  .get(
                      PlayerAttribute.UUID,
                      performedBy.getUniqueId(),
                      Set.of(PlayerForceFetch.GUILD))
                  .join();

          if (inviter == null) {
            throw new MissingEntityException(
                "Encountered error",
                String.format(
                    String.format(
                        "Inviter %s is not initialized", invitationToUpdate.getInvitee().getId())),
                PlayerModel.class);
          }

          if (inviter.getGuild() == null) {
            throw new ValidationException(
                "Must be in a Guild to update a Guild invitation",
                String.format(
                    "Inviter %s is trying to update Player %s's invitation to Guild %s, but isn't in a Guild",
                    performedBy.getUniqueId(),
                    invitationToUpdate.getInvitee().getUuid(),
                    invitationToUpdate.getGuild().getName()));
          }

          if (inviter.getGuild().getId() == invitationToUpdate.getGuild().getId()) {
            throw new ValidationException(
                "Must be in the Guild to update a Guild invitation",
                String.format(
                    "Inviter %s is trying to update Player %s's invitation to Guild %s, but isn't in the Guild",
                    performedBy.getUniqueId(),
                    invitationToUpdate.getInvitee().getUuid(),
                    invitationToUpdate.getGuild().getName()));
          }

          if (inviter.getGuild().getOwner().getId() == inviter.getId()) {
            throw new ValidationException(
                "Must be the Guild owner to update a Guild invitation",
                String.format(
                    "Inviter %s is trying to update Player %s's invitation to Guild %s, but isn't the Guild owner",
                    performedBy.getUniqueId(),
                    invitationToUpdate.getInvitee().getUuid(),
                    invitationToUpdate.getGuild().getName()));
          }

          PlayerModel invitee =
              playerRepository
                  .get(
                      PlayerAttribute.UUID,
                      invitationToUpdate.getInvitee().getUuid(),
                      Set.of(PlayerForceFetch.GUILD))
                  .join();

          if (invitee == null) {
            throw new MissingEntityException(
                "Encountered error",
                String.format(
                    String.format(
                        "Invitee %s is not initialized", invitationToUpdate.getInvitee().getId())),
                PlayerModel.class);
          }

          if (invitee.getGuild() != null) {
            throw new ValidationException(
                "Invitee is already in a Guild",
                String.format(
                    "Inviter %s is trying to update Invitee %s's invitation to Guild %s, but Invitee is already in a Guild",
                    performedBy.getUniqueId(),
                    invitationToUpdate.getInvitee().getUuid(),
                    invitationToUpdate.getGuild().getName()));
          }

          InvitationRepository invitationRepository = new InvitationRepository(this.getSession());

          return invitationRepository.update(invitationToUpdate).join();
        });
  }
}
