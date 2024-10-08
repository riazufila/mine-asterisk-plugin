package net.mineasterisk.mc.service.team;

import io.papermc.paper.util.Tick;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.cache.access.Access;
import net.mineasterisk.mc.cache.access.AccessCache;
import net.mineasterisk.mc.constant.PermissionConstant;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.service.access.AccessService;
import net.mineasterisk.mc.service.team.invitation.Invitation;
import net.mineasterisk.mc.service.team.invitation.InvitationRunnable;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class TeamService {
  private final @NotNull Player player;
  private final Scoreboard scoreboard =
      MineAsterisk.getInstance().getServer().getScoreboardManager().getMainScoreboard();

  public TeamService(final @NotNull Player player) {
    this.player = player;
  }

  public @NotNull List<@NotNull TeamMember> getMembers() {
    final Team team = this.scoreboard.getEntityTeam(this.player);

    if (team == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format(
              "Player %s (%s) isn't in a Team", this.player.getName(), this.player.getUniqueId()));
    }

    final AccessCache accessCache = new AccessCache();
    final List<TeamMember> members = new ArrayList<>();

    for (final Map.Entry<UUID, Access> entry : accessCache.getAll().entrySet()) {
      final UUID uuid = entry.getKey();
      final Access access = entry.getValue();

      //noinspection deprecation
      for (final OfflinePlayer member : team.getPlayers()) {
        if ((!member.hasPlayedBefore() && !member.isOnline()) || member.getName() == null) {
          continue;
        }

        if (!member.getUniqueId().equals(uuid)) {
          continue;
        }

        if (access.getAccesses().contains(PermissionConstant.TEAM_LEADER.toString())) {
          members.add(new TeamMember(member.getName(), true));
        } else {
          members.add(new TeamMember(member.getName(), false));
        }
      }
    }

    return members;
  }

  public void create(final @NotNull String name) {
    final Team playerTeam = this.scoreboard.getEntityTeam(this.player);

    if (playerTeam != null) {
      throw new ValidationException(
          "Already in a Team",
          String.format(
              "Player %s (%s) is already in a Team",
              this.player.getName(), this.player.getUniqueId()));
    }

    if (this.scoreboard.getTeam(name) != null) {
      throw new ValidationException(
          "Team name is taken",
          String.format(
              "Player %s (%s) is trying to create a Team but the name is taken",
              this.player.getName(), this.player.getUniqueId()));
    }

    final Team team = this.scoreboard.registerNewTeam(name);
    final AccessService accessService = new AccessService(this.player);

    team.addEntity(this.player);
    team.displayName(Component.text(name));
    team.prefix(
        Component.textOfChildren(
            Component.text(name).color(NamedTextColor.GRAY),
            Component.text('.').color(NamedTextColor.GRAY)));

    accessService.add(PermissionConstant.TEAM_LEADER.toString());
    accessService.add(PermissionConstant.TEAM_MEMBER.toString());
  }

  public @NotNull List<@NotNull Player> disband() {
    final Team team = this.scoreboard.getEntityTeam(this.player);

    if (team == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format(
              "Player %s (%s) isn't in a Team", this.player.getName(), this.player.getUniqueId()));
    }

    final List<Player> members = new ArrayList<>();

    //noinspection deprecation
    for (OfflinePlayer offlinePlayer : team.getPlayers()) {
      final Player member = offlinePlayer.getPlayer();

      if (member == null) {
        new AccessService(offlinePlayer).removeIfOffline(PermissionConstant.TEAM_MEMBER.toString());

        continue;
      }

      new AccessService(member).remove(PermissionConstant.TEAM_MEMBER.toString());

      if (!member.getUniqueId().equals(this.player.getUniqueId())) {
        members.add(member);
      }
    }

    new AccessService(this.player).remove(PermissionConstant.TEAM_LEADER.toString());
    team.unregister();

    return members;
  }

  public void sendInvitation(final @NotNull Player invitee) {
    final Team inviterTeam = this.scoreboard.getEntityTeam(this.player);
    final Team inviteeTeam = this.scoreboard.getEntityTeam(invitee);

    if (this.player.getUniqueId().equals(invitee.getUniqueId())) {
      throw new ValidationException(
          "Not allowed to invite yourself",
          String.format(
              "Player %s (%s) is trying to invite itself",
              this.player.getName(), this.player.getUniqueId()));
    }

    if (inviterTeam == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format(
              "Inviter %s (%s) isn't in a Team", this.player.getName(), this.player.getUniqueId()));
    }

    final Map.Entry<Integer, Invitation> existingInvitation =
        TeamServiceManager.getInvitation(this.player, invitee, inviterTeam);

    if (existingInvitation != null) {
      throw new ValidationException(
          "Invitation exists",
          String.format(
              "Inviter %s (%s) is trying to send invitation to Invitee %s (%s) but it already exist",
              this.player.getName(),
              this.player.getUniqueId(),
              invitee.getName(),
              invitee.getUniqueId()));
    }

    if (inviteeTeam != null) {
      throw new ValidationException(
          "Player is already in a Team",
          String.format(
              "Invitee %s (%s) is already in a Team", invitee.getName(), invitee.getUniqueId()));
    }

    final Invitation invitation = new Invitation(inviterTeam, this.player, invitee);
    final InvitationRunnable invitationRunnable = new InvitationRunnable(30, invitation);

    final int taskId =
        invitationRunnable
            .runTaskTimer(
                MineAsterisk.getInstance(),
                Tick.tick().fromDuration(Duration.ofSeconds(0)),
                Tick.tick().fromDuration(Duration.ofSeconds(1)))
            .getTaskId();

    TeamServiceManager.addInvitation(taskId, invitation);
  }

  public void acceptInvitation(final @NotNull Player inviter) {
    final Team inviterTeam = this.scoreboard.getEntityTeam(inviter);
    final Team inviteeTeam = this.scoreboard.getEntityTeam(this.player);

    if (this.player.getUniqueId().equals(inviter.getUniqueId())) {
      throw new ValidationException(
          "Not allowed to accept invitation from yourself",
          String.format(
              "Player %s (%s) is trying to accept invitation from itself",
              this.player.getName(), this.player.getUniqueId()));
    }

    if (inviterTeam == null) {
      throw new ValidationException(
          "Inviter is not in a Team",
          String.format(
              "Inviter %s (%s) isn't in a Team", inviter.getName(), inviter.getUniqueId()));
    }

    final Map.Entry<Integer, Invitation> invitation =
        TeamServiceManager.getInvitation(inviter, this.player, inviterTeam);

    if (invitation == null) {
      throw new ValidationException(
          "Invitation doesn't exist",
          String.format(
              "Invitee %s (%s) is trying to accept Team invitation from Inviter %s (%s) but it doesn't exist",
              this.player.getName(),
              this.player.getUniqueId(),
              inviter.getName(),
              inviter.getUniqueId()));
    }

    final Team team = invitation.getValue().team();

    if (!inviterTeam.getName().equals(team.getName())) {
      throw new ValidationException(
          "Inviter Team has changed since the invitation is sent",
          String.format(
              "Inviter %s (%s) is in Team %s but the invitation Team is %s",
              inviter.getName(), inviter.getUniqueId(), inviterTeam.getName(), team.getName()));
    }

    if (inviteeTeam != null) {
      throw new ValidationException(
          "Already in a Team",
          String.format(
              "Invitee %s (%s) is already in a Team", inviter.getName(), inviter.getUniqueId()));
    }

    team.addEntity(this.player);
    new AccessService(this.player).add(PermissionConstant.TEAM_MEMBER.toString());

    MineAsterisk.getInstance().getServer().getScheduler().cancelTask(invitation.getKey());
    TeamServiceManager.removeInvitation(invitation.getKey());
  }

  public void removeInvitation(final @NotNull Player invitee) {
    final Team inviterTeam = this.scoreboard.getEntityTeam(this.player);

    if (this.player.getUniqueId().equals(invitee.getUniqueId())) {
      throw new ValidationException(
          "Not allowed to remove invitation to yourself",
          String.format(
              "Player %s (%s) is trying to remove invitation to itself",
              this.player.getName(), this.player.getUniqueId()));
    }

    if (inviterTeam == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format(
              "Inviter %s (%s) isn't in a Team", this.player.getName(), this.player.getUniqueId()));
    }

    final Map.Entry<Integer, Invitation> invitation =
        TeamServiceManager.getInvitation(this.player, invitee, inviterTeam);

    if (invitation == null) {
      throw new ValidationException(
          "Invitation doesn't exist",
          String.format(
              "Inviter %s (%s) is trying to remove Team invitation to Invitee %s (%s) but it doesn't exist",
              this.player.getName(),
              this.player.getUniqueId(),
              invitee.getName(),
              invitee.getUniqueId()));
    }

    final Team team = invitation.getValue().team();

    if (!inviterTeam.getName().equals(team.getName())) {
      throw new ValidationException(
          "Team has changed since the invitation is sent",
          String.format(
              "Inviter %s (%s) is in Team %s but the invitation Team is %s",
              this.player.getName(),
              this.player.getUniqueId(),
              inviterTeam.getName(),
              team.getName()));
    }

    MineAsterisk.getInstance().getServer().getScheduler().cancelTask(invitation.getKey());
    TeamServiceManager.removeInvitation(invitation.getKey());
  }

  public void kick(final @NotNull OfflinePlayer offlineKicked) {
    final Team kickerTeam = this.scoreboard.getEntityTeam(this.player);
    final Player kicked = offlineKicked.getPlayer();
    Team kickedTeam = null;

    if (!offlineKicked.hasPlayedBefore() && !offlineKicked.isOnline()) {
      throw new ValidationException(
          "Encountered error",
          String.format(
              "Offline Player (%s) haven't played in the server", offlineKicked.getUniqueId()));
    }

    if (offlineKicked.getName() == null) {
      throw new ValidationException(
          "Encountered error",
          String.format("Offline Player (%s) doesn't have a name", offlineKicked.getUniqueId()));
    }

    if (kicked != null) {
      kickedTeam = this.scoreboard.getEntityTeam(kicked);
    } else {
      for (final Team team : this.scoreboard.getTeams()) {
        if (team.hasEntry(offlineKicked.getName())) {
          kickedTeam = team;

          break;
        }
      }
    }

    if (kickerTeam == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format(
              "Player %s (%s) isn't in a Team", this.player.getName(), this.player.getUniqueId()));
    }

    if (kickedTeam == null) {
      throw new ValidationException(
          "Player isn't in a Team",
          String.format(
              "Player %s (%s) is being kicked but isn't in a Team",
              offlineKicked.getName(), offlineKicked.getUniqueId()));
    }

    if (!kickerTeam.getName().equals(kickedTeam.getName())) {
      throw new ValidationException(
          "Not in the same Team",
          String.format(
              "Player %s (%s) is trying to kick out Player %s (%s) but isn't in the Team",
              this.player.getName(),
              this.player.getUniqueId(),
              offlineKicked.getName(),
              offlineKicked.getUniqueId()));
    }

    if (this.player.getName().equals(offlineKicked.getName())) {
      throw new ValidationException(
          "Not allowed to kick yourself",
          String.format(
              "Player %s (%s) is trying to kick itself",
              this.player.getName(), this.player.getUniqueId()));
    }

    if (kicked != null) {
      kickerTeam.removeEntity(kicked);
      new AccessService(kicked).remove(PermissionConstant.TEAM_MEMBER.toString());
    } else {
      kickerTeam.removeEntry(offlineKicked.getName());
      new AccessService(offlineKicked).removeIfOffline(PermissionConstant.TEAM_MEMBER.toString());
    }
  }

  public @NotNull Team leave() {
    final Team team = this.scoreboard.getEntityTeam(this.player);

    if (team == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format(
              "Player %s (%s) isn't in a Team", this.player.getName(), this.player.getUniqueId()));
    }

    team.removeEntity(this.player);
    new AccessService(this.player).remove(PermissionConstant.TEAM_MEMBER.toString());

    return team;
  }

  public void message(final @NotNull String message) {
    final Team team = this.scoreboard.getEntityTeam(this.player);

    if (team == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format(
              "Player %s (%s) isn't in a Team", this.player.getName(), this.player.getUniqueId()));
    }

    final Component teamMessageComponent =
        Component.textOfChildren(
            Component.text("<"),
            Component.text(team.getName()).color(NamedTextColor.GRAY),
            Component.text('.').color(NamedTextColor.GRAY),
            Component.text(this.player.getName()),
            Component.text(">"),
            Component.space(),
            Component.text("tells the team,"),
            Component.space(),
            Component.text("\""),
            Component.text(message),
            Component.text("\""));

    team.sendMessage(teamMessageComponent.color(NamedTextColor.BLUE));
  }
}
