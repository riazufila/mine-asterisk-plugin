package net.mineasterisk.mc.service.team;

import io.papermc.paper.util.Tick;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TeamService {
  private static final @NotNull HashMap<@NotNull Integer, @NotNull Invitation> INVITATIONS =
      new HashMap<>();

  public @NotNull List<@NotNull TeamMember> getMembers(final @NotNull Player player) {
    final ScoreboardManager manager = MineAsterisk.getInstance().getServer().getScoreboardManager();
    final Scoreboard scoreboard = manager.getMainScoreboard();
    final Team team = scoreboard.getEntityTeam(player);

    if (team == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format("Player %s (%s) isn't in a Team", player.getName(), player.getUniqueId()));
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

  public void create(final @NotNull Player player, final @NotNull String name) {
    final ScoreboardManager manager = MineAsterisk.getInstance().getServer().getScoreboardManager();
    final Scoreboard scoreboard = manager.getMainScoreboard();
    final Team playerTeam = scoreboard.getEntityTeam(player);

    if (playerTeam != null) {
      throw new ValidationException(
          "Already in a Team",
          String.format(
              "Player %s (%s) is already in a Team", player.getName(), player.getUniqueId()));
    }

    if (scoreboard.getTeam(name) != null) {
      throw new ValidationException(
          "Team name is taken",
          String.format(
              "Player %s (%s) is trying to create a Team but the name is taken",
              player.getName(), player.getUniqueId()));
    }

    final Team team = scoreboard.registerNewTeam(name);
    final AccessService accessService = new AccessService();

    team.addEntity(player);
    team.displayName(Component.text(name));
    team.prefix(
        Component.textOfChildren(
            Component.text(name).color(NamedTextColor.GRAY),
            Component.text('.').color(NamedTextColor.GRAY)));

    accessService.add(player, PermissionConstant.TEAM_LEADER.toString());
    accessService.add(player, PermissionConstant.TEAM_MEMBER.toString());
  }

  public @NotNull List<@NotNull Player> disband(final @NotNull Player player) {
    final ScoreboardManager manager = MineAsterisk.getInstance().getServer().getScoreboardManager();
    final Scoreboard scoreboard = manager.getMainScoreboard();
    final Team team = scoreboard.getEntityTeam(player);

    if (team == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format("Player %s (%s) isn't in a Team", player.getName(), player.getUniqueId()));
    }

    final AccessService accessService = new AccessService();
    final List<Player> members = new ArrayList<>();

    //noinspection deprecation
    for (OfflinePlayer offlinePlayer : team.getPlayers()) {
      final Player member = offlinePlayer.getPlayer();

      if (member == null) {
        accessService.removeIfOffline(
            offlinePlayer.getUniqueId(), PermissionConstant.TEAM_MEMBER.toString());

        continue;
      }

      accessService.remove(member, PermissionConstant.TEAM_MEMBER.toString());

      if (!member.getUniqueId().equals(player.getUniqueId())) {
        members.add(member);
      }
    }

    accessService.remove(player, PermissionConstant.TEAM_LEADER.toString());
    team.unregister();

    return members;
  }

  public void sendInvitation(final @NotNull Player inviter, final @NotNull Player invitee) {
    final ScoreboardManager manager = MineAsterisk.getInstance().getServer().getScoreboardManager();
    final Scoreboard scoreboard = manager.getMainScoreboard();
    final Team inviterTeam = scoreboard.getEntityTeam(inviter);
    final Team inviteeTeam = scoreboard.getEntityTeam(invitee);

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

    final Map.Entry<Integer, Invitation> existingInvitation =
        this.getInvitationByInviterAndInviteeAndTeam(inviter, invitee, inviterTeam);

    if (existingInvitation != null) {
      throw new ValidationException(
          "Invitation exists",
          String.format(
              "Inviter %s (%s) is trying to send invitation to Invitee %s (%s) but it already exist",
              inviter.getName(), inviter.getUniqueId(), invitee.getName(), invitee.getUniqueId()));
    }

    if (inviteeTeam != null) {
      throw new ValidationException(
          "Player is already in a Team",
          String.format(
              "Invitee %s (%s) is already in a Team", invitee.getName(), invitee.getUniqueId()));
    }

    final Invitation invitation = new Invitation(inviterTeam, inviter, invitee);
    final InvitationRunnable invitationRunnable = new InvitationRunnable(30, invitation);

    final int taskId =
        invitationRunnable
            .runTaskTimer(
                MineAsterisk.getInstance(),
                Tick.tick().fromDuration(Duration.ofSeconds(0)),
                Tick.tick().fromDuration(Duration.ofSeconds(1)))
            .getTaskId();

    this.addInvitationTask(taskId, invitation);
  }

  public void acceptInvitation(final @NotNull Player inviter, final @NotNull Player invitee) {
    final ScoreboardManager manager = MineAsterisk.getInstance().getServer().getScoreboardManager();
    final Scoreboard scoreboard = manager.getMainScoreboard();
    final Team inviterTeam = scoreboard.getEntityTeam(inviter);
    final Team inviteeTeam = scoreboard.getEntityTeam(invitee);

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

    final Map.Entry<Integer, Invitation> invitation =
        this.getInvitationByInviterAndInviteeAndTeam(inviter, invitee, inviterTeam);

    if (invitation == null) {
      throw new ValidationException(
          "Invitation doesn't exist",
          String.format(
              "Invitee %s (%s) is trying to accept Team invitation from Inviter %s (%s) but it doesn't exist",
              invitee.getName(), invitee.getUniqueId(), inviter.getName(), inviter.getUniqueId()));
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
              "Invitee %s (%s) is already in a Team", invitee.getName(), invitee.getUniqueId()));
    }

    final AccessService accessService = new AccessService();

    team.addEntity(invitee);
    accessService.add(invitee, PermissionConstant.TEAM_MEMBER.toString());

    MineAsterisk.getInstance().getServer().getScheduler().cancelTask(invitation.getKey());
    this.removeInvitationTask(invitation.getKey());
  }

  public void removeInvitation(final @NotNull Player inviter, final @NotNull Player invitee) {
    final ScoreboardManager manager = MineAsterisk.getInstance().getServer().getScoreboardManager();
    final Scoreboard scoreboard = manager.getMainScoreboard();
    final Team inviterTeam = scoreboard.getEntityTeam(inviter);

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

    final Map.Entry<Integer, Invitation> invitation =
        this.getInvitationByInviterAndInviteeAndTeam(inviter, invitee, inviterTeam);

    if (invitation == null) {
      throw new ValidationException(
          "Invitation doesn't exist",
          String.format(
              "Inviter %s (%s) is trying to remove Team invitation to Invitee %s (%s) but it doesn't exist",
              inviter.getName(), inviter.getUniqueId(), invitee.getName(), invitee.getUniqueId()));
    }

    final Team team = invitation.getValue().team();

    if (!inviterTeam.getName().equals(team.getName())) {
      throw new ValidationException(
          "Team has changed since the invitation is sent",
          String.format(
              "Inviter %s (%s) is in Team %s but the invitation Team is %s",
              inviter.getName(), inviter.getUniqueId(), inviterTeam.getName(), team.getName()));
    }

    MineAsterisk.getInstance().getServer().getScheduler().cancelTask(invitation.getKey());
    this.removeInvitationTask(invitation.getKey());
  }

  public void kick(final @NotNull Player kicker, final @NotNull OfflinePlayer offlineKicked) {
    final ScoreboardManager manager = MineAsterisk.getInstance().getServer().getScoreboardManager();
    final Scoreboard scoreboard = manager.getMainScoreboard();
    final Team kickerTeam = scoreboard.getEntityTeam(kicker);
    final Player kicked = offlineKicked.getPlayer();
    Team kickedTeam = null;

    if (!offlineKicked.hasPlayedBefore() && !offlineKicked.isOnline()) {
      throw new ValidationException(
          "Encountered error",
          String.format(
              "Offline Player (%s) haven't played in the server.", offlineKicked.getUniqueId()));
    }

    if (offlineKicked.getName() == null) {
      throw new ValidationException(
          "Encountered error",
          String.format("Offline Player (%s) doesn't have a name.", offlineKicked.getUniqueId()));
    }

    if (kicked != null) {
      kickedTeam = scoreboard.getEntityTeam(kicked);
    } else {
      for (final Team team : scoreboard.getTeams()) {
        if (team.hasEntry(offlineKicked.getName())) {
          kickedTeam = team;

          break;
        }
      }
    }

    if (kickerTeam == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format("Player %s (%s) isn't in a Team", kicker.getName(), kicker.getUniqueId()));
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
              kicker.getName(),
              kicker.getUniqueId(),
              offlineKicked.getName(),
              offlineKicked.getUniqueId()));
    }

    if (kicker.getName().equals(offlineKicked.getName())) {
      throw new ValidationException(
          "Not allowed to kick yourself",
          String.format(
              "Player %s (%s) is trying to kick itself", kicker.getName(), kicker.getUniqueId()));
    }

    final AccessService accessService = new AccessService();

    if (kicked != null) {
      kickerTeam.removeEntity(kicked);
      accessService.remove(kicked, PermissionConstant.TEAM_MEMBER.toString());
    } else {
      kickerTeam.removeEntry(offlineKicked.getName());
      accessService.removeIfOffline(
          offlineKicked.getUniqueId(), PermissionConstant.TEAM_MEMBER.toString());
    }
  }

  public @NotNull Team leave(final @NotNull Player player) {
    final ScoreboardManager manager = MineAsterisk.getInstance().getServer().getScoreboardManager();
    final Scoreboard scoreboard = manager.getMainScoreboard();
    final Team team = scoreboard.getEntityTeam(player);

    if (team == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format("Player %s (%s) isn't in a Team", player.getName(), player.getUniqueId()));
    }

    final AccessService accessService = new AccessService();

    team.removeEntity(player);
    accessService.remove(player, PermissionConstant.TEAM_MEMBER.toString());

    return team;
  }

  private @Nullable Map.Entry<Integer, Invitation> getInvitationByInviterAndInviteeAndTeam(
      final @NotNull Player inviter,
      final @NotNull Player invitee,
      final @NotNull Team inviterTeam) {
    try {
      final Optional<Map.Entry<Integer, Invitation>> invitation =
          TeamService.INVITATIONS.entrySet().stream()
              .filter(
                  entry ->
                      entry.getValue().inviter().getUniqueId().equals(inviter.getUniqueId())
                          && entry.getValue().invitee().getUniqueId().equals(invitee.getUniqueId())
                          && entry.getValue().team().getName().equals(inviterTeam.getName()))
              .findFirst();

      return invitation.orElse(null);
    } catch (IllegalStateException exception) {
      throw new ValidationException(
          "Inviter Team when this invitation was made has been disbanded",
          String.format(
              "Inviter %s (%s) Team when the invitation was first made has been disbanded",
              inviter.getName(), inviter.getUniqueId()));
    }
  }

  private void addInvitationTask(final int taskId, final @NotNull Invitation invitation) {
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
