package net.mineasterisk.mc.service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.forcefetch.PlayerForceFetch;
import net.mineasterisk.mc.model.InvitationModel;
import net.mineasterisk.mc.repository.InvitationRepository;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InvitationService {
  public static @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull Player performedBy, final @NotNull InvitationModel invitationToAdd) {
    if (performedBy.getUniqueId() != invitationToAdd.getInviter().getUuid()) {
      PluginUtil.getLogger().info("Unable to add Guild invitation: Inviter mismatch");

      return CompletableFuture.completedFuture(null);
    }

    return PlayerRepository.get(
            PlayerAttribute.UUID, performedBy.getUniqueId(), Set.of(PlayerForceFetch.GUILD))
        .thenCompose(
            inviter -> {
              if (inviter == null) {
                PluginUtil.getLogger()
                    .info("Unable to add Guild invitation: Inviter doesn't exist");

                return CompletableFuture.completedFuture(null);
              }

              if (inviter.getGuild() == null) {
                PluginUtil.getLogger()
                    .info("Unable to add Guild invitation: Inviter isn't in a Guild");

                return CompletableFuture.completedFuture(null);
              }

              if (inviter.getGuild().getOwner().getUuid() == inviter.getUuid()) {
                PluginUtil.getLogger()
                    .info("Unable to add Guild invitation: Inviter doesn't own a Guild");

                return CompletableFuture.completedFuture(null);
              }

              return PlayerRepository.get(
                      PlayerAttribute.UUID,
                      invitationToAdd.getInvitee().getUuid(),
                      Set.of(PlayerForceFetch.GUILD))
                  .thenCompose(
                      invitee -> {
                        if (invitee == null) {
                          PluginUtil.getLogger()
                              .info("Unable to add Guild invitation: Invitee doesn't exist");

                          return CompletableFuture.completedFuture(null);
                        }

                        if (invitee.getGuild() != null) {
                          PluginUtil.getLogger()
                              .info("Unable to add Guild invitation: Invitee is in a Guild");

                          return CompletableFuture.completedFuture(null);
                        }

                        return InvitationRepository.add(invitationToAdd);
                      });
            });
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull Player performedBy, final @NotNull InvitationModel updatedInvitation) {
    if (!(performedBy.getUniqueId() == updatedInvitation.getInviter().getUuid()
        || performedBy.getUniqueId() == updatedInvitation.getInvitee().getUuid())) {
      PluginUtil.getLogger()
          .info("Unable to update Guild invitation: Performer is not inviter or invitee");

      return CompletableFuture.completedFuture(null);
    }

    return PlayerRepository.get(
            PlayerAttribute.UUID, performedBy.getUniqueId(), Set.of(PlayerForceFetch.GUILD))
        .thenCompose(
            inviter -> {
              if (inviter == null) {
                PluginUtil.getLogger()
                    .info("Unable to update Guild invitation: Inviter doesn't exist");

                return CompletableFuture.completedFuture(null);
              }

              if (inviter.getGuild() == null) {
                PluginUtil.getLogger()
                    .info("Unable to update Guild invitation: Inviter isn't in a Guild");

                return CompletableFuture.completedFuture(null);
              }

              if (inviter.getGuild().getOwner().getUuid() == inviter.getUuid()) {
                PluginUtil.getLogger()
                    .info("Unable to update Guild invitation: Inviter doesn't own a Guild");

                return CompletableFuture.completedFuture(null);
              }

              return PlayerRepository.get(
                      PlayerAttribute.UUID,
                      updatedInvitation.getInvitee().getUuid(),
                      Set.of(PlayerForceFetch.GUILD))
                  .thenCompose(
                      invitee -> {
                        if (invitee == null) {
                          PluginUtil.getLogger()
                              .info("Unable to update Guild invitation: Invitee doesn't exist");

                          return CompletableFuture.completedFuture(null);
                        }

                        if (invitee.getGuild() != null) {
                          PluginUtil.getLogger()
                              .info("Unable to update Guild invitation: Invitee is in a Guild");

                          return CompletableFuture.completedFuture(null);
                        }

                        return InvitationRepository.add(updatedInvitation);
                      });
            });
  }
}
