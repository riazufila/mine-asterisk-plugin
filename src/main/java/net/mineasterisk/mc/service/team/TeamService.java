package net.mineasterisk.mc.service.team;

import io.papermc.paper.util.Tick;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.constant.PermissionConstant;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.model.Invitation;
import net.mineasterisk.mc.service.access.AccessService;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeamService {
  private static final @NotNull Map<@NotNull Integer, @NotNull Invitation> INVITATIONS =
      new HashMap<>();

  public @Nullable Team get(final @NotNull Player player) {
    return PluginUtil.getMainScoreboard().getEntityTeam(player);
  }

  public void create(final @NotNull Player player, final @NotNull String name) {
    final Team playerTeam = this.get(player);

    if (playerTeam != null) {
      throw new ValidationException(
          "Already in a Team",
          String.format(
              "Player %s (%s) is already in a Team", player.getName(), player.getUniqueId()));
    }

    final Scoreboard scoreboard = PluginUtil.getMainScoreboard();
    final Team team = scoreboard.registerNewTeam(name);
    final AccessService accessService = new AccessService();

    team.addEntity(player);
    team.displayName(Component.text(name));
    team.prefix(
        Component.textOfChildren(
            Component.text(name).color(NamedTextColor.GRAY),
            Component.text('.').color(NamedTextColor.GRAY)));

    accessService.add(player, PermissionConstant.TEAM_OWNER.toString());
    accessService.add(player, PermissionConstant.TEAM_MEMBER.toString());
  }

  public void disband(final @NotNull Player player) {
    final Team team = this.get(player);

    if (team == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format("Player %s (%s) isn't in a Team", player.getName(), player.getUniqueId()));
    }

    team.unregister();

    final AccessService accessService = new AccessService();

    accessService.remove(player, PermissionConstant.TEAM_OWNER.toString());
    accessService.remove(player, PermissionConstant.TEAM_MEMBER.toString());

    // TODO: Remove other Players' permission.
  }

  public void sendInvitation(final @NotNull Player inviter, final @NotNull Player invitee) {
    final Team inviterTeam = this.get(inviter);
    final Team inviteeTeam = this.get(invitee);

    if (inviter.getUniqueId().equals(invitee.getUniqueId())) {
      throw new ValidationException(
          "Not allowed to invite yourself",
          String.format(
              "Player %s (%s) is trying to invite itself",
              inviter.getName(), inviter.getUniqueId()));
    }

    if (inviterTeam == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format(
              "Inviter %s (%s) isn't in a Team", inviter.getName(), inviter.getUniqueId()));
    }

    if (inviteeTeam != null) {
      throw new ValidationException(
          "Player is already in a Team",
          String.format(
              "Invitee %s (%s) is already in a Team", invitee.getName(), invitee.getUniqueId()));
    }

    final Invitation invitation = new Invitation(inviterTeam, inviter, invitee);
    final InvitationRunnable invitationRunnable = new InvitationRunnable(10, invitation);

    invitationRunnable.runTaskTimerAsynchronously(
        PluginUtil.get(),
        Tick.tick().fromDuration(Duration.ofSeconds(0)),
        Tick.tick().fromDuration(Duration.ofSeconds(1)));
  }

  public void acceptInvitation(final @NotNull Player inviter, final @NotNull Player invitee) {
    final Team inviterTeam = this.get(inviter);
    final Team inviteeTeam = this.get(invitee);
    final Map.Entry<Integer, Invitation> invitation = this.getInvitationByInviter(inviter);

    if (invitation == null) {
      throw new ValidationException(
          "Invitation doesn't exist",
          String.format(
              "Invitee %s (%s) is trying to accept Team invitation from Inviter %s (%s) but it doesn't exist",
              invitee.getName(), invitee.getUniqueId(), inviter.getName(), inviter.getUniqueId()));
    }

    final Team team = invitation.getValue().team();

    if (invitee.getUniqueId().equals(inviter.getUniqueId())) {
      throw new ValidationException(
          "Not allowed to accept invitation from yourself",
          String.format(
              "Player %s (%s) is trying to accept invitation from itself",
              invitee.getName(), invitee.getUniqueId()));
    }

    if (inviterTeam == null) {
      throw new ValidationException(
          "Inviter is not in a Team",
          String.format(
              "Inviter %s (%s) isn't in a Team", inviter.getName(), inviter.getUniqueId()));
    }

    if (!inviterTeam.getName().equals(team.getName())) {
      throw new ValidationException(
          "Inviter Team and the invitation's Team is different",
          String.format(
              "Inviter %s (%s) is in Team %s but the invitation Team is %s",
              inviter.getName(), inviter.getUniqueId(), inviterTeam.getName(), team.getName()));
    }

    if (inviteeTeam != null) {
      throw new ValidationException(
          "Already in a Team",
          String.format(
              "Invitee %s (%s) is already in a Team", invitee.getName(), invitee.getUniqueId()));
    }

    try {
      final AccessService accessService = new AccessService();

      team.addEntity(invitee);
      accessService.add(invitee, PermissionConstant.TEAM_MEMBER.toString());
    } catch (IllegalStateException exception) {
      throw new ValidationException(
          "Team doesn't exist",
          String.format(
              "Invitee %s (%s) is trying to accept Team invitation from Inviter %s (%s) but the Team %s doesn't exist",
              invitee.getName(),
              invitee.getUniqueId(),
              inviter.getName(),
              inviter.getUniqueId(),
              team.getName()));
    }

    PluginUtil.getScheduler().cancelTask(invitation.getKey());
  }

  public void removeInvitation(final @NotNull Player inviter, final @NotNull Player invitee) {
    final Team inviterTeam = this.get(inviter);
    final Team inviteeTeam = this.get(invitee);
    final Map.Entry<Integer, Invitation> invitation = this.getInvitationByInvitee(invitee);

    if (invitation == null) {
      throw new ValidationException(
          "Invitation doesn't exist",
          String.format(
              "Inviter %s (%s) is trying to remove Team invitation to Invitee %s (%s) but it doesn't exist",
              inviter.getName(), inviter.getUniqueId(), invitee.getName(), invitee.getUniqueId()));
    }

    final Team team = invitation.getValue().team();

    if (inviter.getUniqueId().equals(invitee.getUniqueId())) {
      throw new ValidationException(
          "Not allowed to remove invitation to yourself",
          String.format(
              "Player %s (%s) is trying to remove invitation to itself",
              inviter.getName(), inviter.getUniqueId()));
    }

    if (inviterTeam == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format(
              "Inviter %s (%s) isn't in a Team", inviter.getName(), inviter.getUniqueId()));
    }

    if (!inviterTeam.getName().equals(team.getName())) {
      throw new ValidationException(
          "Inviter Team and the invitation's Team is different",
          String.format(
              "Inviter %s (%s) is in Team %s but the invitation Team is %s",
              inviter.getName(), inviter.getUniqueId(), inviterTeam.getName(), team.getName()));
    }

    if (inviteeTeam != null) {
      throw new ValidationException(
          "Player is already in a Team",
          String.format(
              "Invitee %s (%s) is already in a Team", invitee.getName(), invitee.getUniqueId()));
    }

    PluginUtil.getScheduler().cancelTask(invitation.getKey());
  }

  private @Nullable Map.Entry<Integer, Invitation> getInvitationByInviter(
      final @NotNull Player inviter) {
    final Optional<Map.Entry<Integer, Invitation>> invitation =
        TeamService.INVITATIONS.entrySet().stream()
            .filter(entry -> entry.getValue().inviter().getUniqueId().equals(inviter.getUniqueId()))
            .findFirst();

    return invitation.orElse(null);
  }

  private @Nullable Entry<Integer, Invitation> getInvitationByInvitee(
      final @NotNull Player invitee) {
    final Optional<Map.Entry<Integer, Invitation>> invitation =
        TeamService.INVITATIONS.entrySet().stream()
            .filter(entry -> entry.getValue().invitee().getUniqueId().equals(invitee.getUniqueId()))
            .findFirst();

    return invitation.orElse(null);
  }

  public void addInvitationTask(final int taskId, final @NotNull Invitation invitation) {
    if (TeamService.INVITATIONS.containsKey(taskId)) {
      return;
    }

    TeamService.INVITATIONS.put(taskId, invitation);
  }

  public void removeInvitationTask(final int taskId) {
    if (!TeamService.INVITATIONS.containsKey(taskId)) {
      return;
    }

    TeamService.INVITATIONS.remove(taskId);
  }
}
