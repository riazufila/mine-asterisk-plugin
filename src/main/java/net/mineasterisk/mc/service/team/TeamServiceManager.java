package net.mineasterisk.mc.service.team;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import net.mineasterisk.mc.service.team.invitation.Invitation;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeamServiceManager {
  private static final @NotNull HashMap<@NotNull Integer, @NotNull Invitation> INVITATIONS =
      new HashMap<>();

  protected static @Nullable Map.Entry<Integer, Invitation> getInvitation(
      final @NotNull Player inviter,
      final @NotNull Player invitee,
      final @NotNull Team inviterTeam) {
    final Optional<Entry<Integer, Invitation>> invitation =
        TeamServiceManager.INVITATIONS.entrySet().stream()
            .filter(
                entry -> {
                  try {
                    return entry.getValue().inviter().getUniqueId().equals(inviter.getUniqueId())
                        && entry.getValue().invitee().getUniqueId().equals(invitee.getUniqueId())
                        && entry.getValue().team().getName().equals(inviterTeam.getName());
                  } catch (final IllegalStateException exception) {
                    return false;
                  }
                })
            .findFirst();

    return invitation.orElse(null);
  }

  protected static void addInvitation(
      final @NotNull Integer taskId, final @NotNull Invitation invitation) {
    if (TeamServiceManager.INVITATIONS.containsKey(taskId)) {
      return;
    }

    TeamServiceManager.INVITATIONS.put(taskId, invitation);
  }

  public static void removeInvitation(final @NotNull Integer taskId) {
    if (!TeamServiceManager.INVITATIONS.containsKey(taskId)) {
      return;
    }

    TeamServiceManager.INVITATIONS.remove(taskId);
  }
}
