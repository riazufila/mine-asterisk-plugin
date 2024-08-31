package net.mineasterisk.mc.service.team;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import be.seeseemelk.mockbukkit.scoreboard.ScoreboardManagerMock;
import be.seeseemelk.mockbukkit.scoreboard.ScoreboardMock;
import io.papermc.paper.util.Tick;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.cache.access.AccessCache;
import net.mineasterisk.mc.constant.PermissionConstant;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.service.team.invitation.Invitation;
import net.mineasterisk.mc.util.LoaderUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class TeamServiceTest {
  private final @NotNull MockedStatic<LoaderUtil> loaderUtil = Mockito.mockStatic(LoaderUtil.class);
  private @NotNull ServerMock server;
  private HashMap<Integer, Invitation> invitations;

  @BeforeEach
  public void setUp() throws NoSuchFieldException, IllegalAccessException {
    final Field field = TeamService.class.getDeclaredField("INVITATIONS");

    field.setAccessible(true);

    this.server = MockBukkit.mock();
    //noinspection unchecked
    invitations = (HashMap<Integer, Invitation>) field.get(null);
    MockBukkit.load(MineAsterisk.class);
  }

  @AfterEach
  public void tearDown() {
    invitations.clear();
    MockBukkit.unmock();
    loaderUtil.close();
  }

  @Test
  void givenPlayer_whenGetMembersAndHasNoTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock player = this.server.addPlayer();

    Assertions.assertThrows(ValidationException.class, () -> teamService.getMembers(player));
  }

  @Test
  void givenPlayer_whenGetMembersAndNoneOfflineAndHasTeam_thenReturnMembers() {
    final TeamService teamService = new TeamService();
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final PlayerMock player2 = this.server.addPlayer();

    teamService.create(player0, "Team0");

    final Team team = scoreboard.getEntityTeam(player0);

    Assertions.assertNotNull(team);

    teamService.sendInvitation(player0, player1);
    teamService.sendInvitation(player0, player2);
    teamService.acceptInvitation(player0, player1);
    teamService.acceptInvitation(player0, player2);

    final List<TeamMember> expectedResult =
        List.of(
            new TeamMember(player0.getName(), true),
            new TeamMember(player1.getName(), false),
            new TeamMember(player2.getName(), false));

    Assertions.assertTrue(teamService.getMembers(player0).containsAll(expectedResult));
  }

  @Test
  void givenPlayer_whenGetMembersAndSomeOfflineAndHasTeam_thenReturnMembers() {
    final TeamService teamService = new TeamService();
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final PlayerMock player2 = this.server.addPlayer();

    teamService.create(player0, "Team0");

    final Team team = scoreboard.getEntityTeam(player0);

    Assertions.assertNotNull(team);

    teamService.sendInvitation(player0, player1);
    teamService.sendInvitation(player0, player2);

    teamService.acceptInvitation(player0, player1);
    teamService.acceptInvitation(player0, player2);

    player0.disconnect();
    player2.disconnect();

    final List<TeamMember> expectedResult =
        List.of(
            new TeamMember(player0.getName(), true),
            new TeamMember(player1.getName(), false),
            new TeamMember(player2.getName(), false));

    Assertions.assertTrue(teamService.getMembers(player0).containsAll(expectedResult));
  }

  @Test
  void givenPlayerAndName_whenCreateAndHasTeamAndIsTeamLeader_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock player = this.server.addPlayer();

    teamService.create(player, "Team0");

    Assertions.assertThrows(ValidationException.class, () -> teamService.create(player, "Team1"));
  }

  @Test
  void givenPlayerAndName_whenCreateAndHasTeamAndIsTeamMember_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();

    teamService.create(player0, "Team0");

    final Team team = scoreboard.getEntityTeam(player0);

    Assertions.assertNotNull(team);

    teamService.sendInvitation(player0, player1);
    teamService.acceptInvitation(player0, player1);

    Assertions.assertThrows(ValidationException.class, () -> teamService.create(player1, "Team1"));
  }

  @Test
  void givenPlayerAndName_whenCreateAndNameIsTaken_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final String takenName = "Team0";
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();

    teamService.create(player0, takenName);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.create(player1, takenName));
  }

  @Test
  void givenPlayerAndName_whenCreateAndPlayerHasNoTeamAndNameIsNotTaken_thenCreate() {
    final TeamService teamService = new TeamService();
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock player = this.server.addPlayer();
    final String name = "Team0";

    teamService.create(player, name);

    final Team team = scoreboard.getEntityTeam(player);
    final Component expectedPrefixComponent =
        Component.textOfChildren(
            Component.text(name).color(NamedTextColor.GRAY),
            Component.text('.').color(NamedTextColor.GRAY));

    Assertions.assertNotNull(team);
    this.assertPlayerInTeamAndIsTeamLeader(player, team);
    Assertions.assertEquals(expectedPrefixComponent, team.prefix());
  }

  @Test
  void givenPlayer_whenDisbandAndHasNoTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock player = this.server.addPlayer();

    Assertions.assertThrows(ValidationException.class, () -> teamService.disband(player));
  }

  @Test
  void givenPlayer_whenDisbandAndHasTeamWithNoOtherMembers_thenDisbandAndReturnOnlineMembers() {
    final TeamService teamService = new TeamService();
    final PlayerMock player = this.server.addPlayer();

    teamService.create(player, "Team0");

    final List<Player> members = teamService.disband(player);

    this.assertPlayerHasNoTeam(player);
    Assertions.assertEquals(0, members.size());
  }

  @Test
  void givenPlayer_whenDisbandAndHasTeamWithNoneOfflineMembers_thenDisbandAndReturnOnlineMembers() {
    final TeamService teamService = new TeamService();
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final PlayerMock player2 = this.server.addPlayer();

    teamService.create(player0, "Team0");

    teamService.sendInvitation(player0, player1);
    teamService.sendInvitation(player0, player2);

    teamService.acceptInvitation(player0, player1);
    teamService.acceptInvitation(player0, player2);

    final List<Player> members = teamService.disband(player0);

    this.assertPlayerHasNoTeam(player0);
    this.assertPlayerHasNoTeam(player1);
    this.assertPlayerHasNoTeam(player2);
    Assertions.assertEquals(2, members.size());
  }

  @Test
  void givenPlayer_whenDisbandAndHasTeamWithSomeOfflineMembers_thenDisbandAndReturnOnlineMembers() {
    final TeamService teamService = new TeamService();
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final PlayerMock player2 = this.server.addPlayer();

    teamService.create(player0, "Team0");

    teamService.sendInvitation(player0, player1);
    teamService.sendInvitation(player0, player2);

    teamService.acceptInvitation(player0, player1);
    teamService.acceptInvitation(player0, player2);

    player1.disconnect();

    final List<Player> members = teamService.disband(player0);

    this.assertPlayerHasNoTeam(player0);
    this.assertPlayerHasNoTeam(player1);
    this.assertPlayerHasNoTeam(player2);
    Assertions.assertEquals(1, members.size());
  }

  @Test
  void givenInviterAndInvitee_whenSendInvitationAndInviteeIsInviter_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.sendInvitation(inviter, inviter));
  }

  @Test
  void givenInviterAndInvitee_whenSendInvitationAndInviterHasNoTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.sendInvitation(inviter, invitee));
  }

  @Test
  void givenInviterAndInvitee_whenSendInvitationAndInvitationExists_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    teamService.create(inviter, "Team0");
    teamService.sendInvitation(inviter, invitee);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.sendInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenSendInvitationAndInviteeHasTeamAndInviteeIsTeamLeader_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    teamService.create(inviter, "Team0");
    teamService.create(invitee, "Team1");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.sendInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenSendInvitationAndInviteeHasTeamAndInviteeIsTeamMember_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();

    teamService.create(inviter, "Team0");
    teamService.create(otherTeamLeader, "Team1");

    teamService.sendInvitation(otherTeamLeader, invitee);
    teamService.acceptInvitation(otherTeamLeader, invitee);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.sendInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenSendInvitationAndInviteeHasNoTeamAndInviterInOriginalTeam_thenSendInvitation() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final String teamName = "Team0";

    teamService.create(inviter, teamName);

    Assertions.assertDoesNotThrow(() -> teamService.sendInvitation(inviter, invitee));
    Assertions.assertTrue(
        this.invitations.values().stream()
            .anyMatch(
                invitation ->
                    invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(teamName)));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInvitationDoesNotExist_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    teamService.create(inviter, "Team0");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.acceptInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInvitationDoesNotExistAndInviterHasNoTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.acceptInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviteeIsInviterAndInviterHasNoTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.acceptInvitation(inviter, inviter));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviteeIsInviterAndInviterHasTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();

    teamService.create(inviter, "Team0");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.acceptInvitation(inviter, inviter));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviterDisbandedTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    teamService.create(inviter, "Team0");
    teamService.sendInvitation(inviter, invitee);
    teamService.disband(inviter);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.acceptInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviterHasDifferentTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    teamService.create(inviter, "Team0");
    teamService.sendInvitation(inviter, invitee);
    teamService.disband(inviter);
    teamService.create(inviter, "Team1");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.acceptInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviteeHasTeamAndInviteeIsTeamLeader_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    teamService.create(inviter, "Team0");
    teamService.sendInvitation(inviter, invitee);
    teamService.create(invitee, "Team1");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.acceptInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviteeHasTeamAndInviteeIsTeamMember_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();

    teamService.create(inviter, "Team0");
    teamService.sendInvitation(inviter, invitee);
    teamService.create(otherTeamLeader, "Team1");
    teamService.sendInvitation(otherTeamLeader, invitee);
    teamService.acceptInvitation(otherTeamLeader, invitee);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.acceptInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviterHasSameTeamAndInviteeHasNoTeamAndInvitationExpires_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final String teamName = "Team0";

    teamService.create(inviter, teamName);
    teamService.sendInvitation(inviter, invitee);

    this.server.getScheduler().performTicks(Tick.tick().fromDuration(Duration.ofSeconds(31)));

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.acceptInvitation(inviter, invitee));

    Assertions.assertTrue(
        this.invitations.values().stream()
            .noneMatch(
                invitation ->
                    invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(teamName)));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviterHasSameTeamAndInviteeHasNoTeam_thenAcceptInvitation() {
    final TeamService teamService = new TeamService();
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final String teamName = "Team0";

    teamService.create(inviter, teamName);
    teamService.sendInvitation(inviter, invitee);

    Assertions.assertDoesNotThrow(() -> teamService.acceptInvitation(inviter, invitee));

    final Team team = scoreboard.getEntityTeam(invitee);

    Assertions.assertNotNull(team);
    this.assertPlayerInTeamAndIsTeamMember(invitee, team);
    Assertions.assertTrue(
        this.invitations.values().stream()
            .noneMatch(
                invitation ->
                    invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(teamName)));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInvitationDoesNotExist_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    teamService.create(inviter, "Team0");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.removeInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInvitationDoesNotExistAndInviterHasNoTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.removeInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeIsInviterAndInviterHasNoTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.removeInvitation(inviter, inviter));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeIsInviterAndInviterHasTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();

    teamService.create(inviter, "Team0");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.removeInvitation(inviter, inviter));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviterDisbandedTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    teamService.create(inviter, "Team0");
    teamService.sendInvitation(inviter, invitee);
    teamService.disband(inviter);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.removeInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviterHasDifferentTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();

    teamService.create(inviter, "Team0");
    teamService.sendInvitation(inviter, invitee);
    teamService.disband(inviter);
    teamService.create(inviter, "Team1");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.removeInvitation(inviter, invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeHasNoTeamAndInviterHasSameTeamAndInvitationExpires_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final String teamName = "Team0";

    teamService.create(inviter, teamName);
    teamService.sendInvitation(inviter, invitee);

    this.server.getScheduler().performTicks(Tick.tick().fromDuration(Duration.ofSeconds(31)));

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.removeInvitation(inviter, invitee));

    this.assertPlayerHasNoTeam(invitee);
    Assertions.assertTrue(
        this.invitations.values().stream()
            .noneMatch(
                invitation ->
                    invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(teamName)));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeHasNoTeamAndInviterHasSameTeam_thenRemoveInvitation() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final String teamName = "Team0";

    teamService.create(inviter, teamName);
    teamService.sendInvitation(inviter, invitee);

    Assertions.assertDoesNotThrow(() -> teamService.removeInvitation(inviter, invitee));

    this.assertPlayerHasNoTeam(invitee);
    Assertions.assertTrue(
        this.invitations.values().stream()
            .noneMatch(
                invitation ->
                    invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(teamName)));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeHasTeamAndInviteeIsTeamLeaderAndInviterHasSameTeam_thenRemoveInvitation() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final String inviterTeamName = "Team0";

    teamService.create(inviter, inviterTeamName);
    teamService.sendInvitation(inviter, invitee);
    teamService.create(invitee, "Team1");

    Assertions.assertDoesNotThrow(() -> teamService.removeInvitation(inviter, invitee));
    Assertions.assertTrue(
        this.invitations.values().stream()
            .noneMatch(
                invitation ->
                    invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(inviterTeamName)));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeHasTeamAndInviteeIsTeamMemberAndInviterHasSameTeam_thenRemoveInvitation() {
    final TeamService teamService = new TeamService();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();
    final String inviterTeamName = "Team0";

    teamService.create(inviter, inviterTeamName);
    teamService.create(otherTeamLeader, "Team1");
    teamService.sendInvitation(inviter, invitee);
    teamService.sendInvitation(otherTeamLeader, invitee);
    teamService.acceptInvitation(otherTeamLeader, invitee);

    Assertions.assertDoesNotThrow(() -> teamService.removeInvitation(inviter, invitee));
    Assertions.assertTrue(
        this.invitations.values().stream()
            .noneMatch(
                invitation ->
                    invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(inviterTeamName)));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedNeverPlayedAndOfflineKickedIsNotOnline_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(UUID.randomUUID());

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedHasNoNameAndOfflineKickedIsOnline_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = Mockito.spy(this.server.getOfflinePlayer(kickedUuid));

    Mockito.when(offlineKicked.getName()).thenReturn(null);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedHasNoNameAndOfflineKickedIsOffline_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final UUID kickedUuid = kicked.getUniqueId();

    kicked.disconnect();

    final OfflinePlayer offlineKicked = Mockito.spy(this.server.getOfflinePlayer(kickedUuid));

    Mockito.when(offlineKicked.getName()).thenReturn(null);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndKickerIsNotInTeamAndOfflineKickedIsOnline_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndKickerIsNotInTeamAndOfflineKickedIsOffline_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final UUID kickedUuid = kicked.getUniqueId();

    kicked.disconnect();

    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOnlineAndOfflineKickedHasNoTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    teamService.create(kicker, "Team0");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOfflineAndOfflineKickedHasNoTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final UUID kickedUuid = kicked.getUniqueId();

    kicked.disconnect();

    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    teamService.create(kicker, "Team0");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOnlineAndTeamIsDisbanded_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    teamService.create(kicker, "Team0");
    teamService.sendInvitation(kicker, kicked);
    teamService.acceptInvitation(kicker, kicked);
    teamService.disband(kicker);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOfflineAndTeamIsDisbanded_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();

    teamService.create(kicker, "Team0");
    teamService.sendInvitation(kicker, kicked);
    teamService.acceptInvitation(kicker, kicked);
    teamService.disband(kicker);

    kicked.disconnect();

    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOnlineAndTeamIsDifferentAndOfflineKickedIsTeamLeader_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    teamService.create(kicker, "Team0");
    teamService.create(kicked, "Team1");

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOfflineAndTeamIsDifferentAndOfflineKickedIsTeamLeader_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();

    teamService.create(kicker, "Team0");
    teamService.sendInvitation(kicker, kicked);
    teamService.acceptInvitation(kicker, kicked);
    teamService.disband(kicker);

    kicked.disconnect();

    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOnlineAndTeamIsDifferentAndOfflineKickedIsTeamMember_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    teamService.create(kicker, "Team0");
    teamService.create(otherTeamLeader, "Team1");
    teamService.sendInvitation(otherTeamLeader, kicked);
    teamService.acceptInvitation(otherTeamLeader, kicked);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOfflineAndTeamIsDifferentAndOfflineKickedIsTeamMember_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();

    teamService.create(kicker, "Team0");
    teamService.create(otherTeamLeader, "Team1");
    teamService.sendInvitation(otherTeamLeader, kicked);
    teamService.acceptInvitation(otherTeamLeader, kicked);

    kicked.disconnect();

    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertThrows(
        ValidationException.class, () -> teamService.kick(kicker, offlineKicked));
  }

  @Test
  void givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOnline_thenKick() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    teamService.create(kicker, "Team0");
    teamService.sendInvitation(kicker, kicked);
    teamService.acceptInvitation(kicker, kicked);

    Assertions.assertDoesNotThrow(() -> teamService.kick(kicker, offlineKicked));
    this.assertPlayerHasNoTeam(kicked);
  }

  @Test
  void givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOffline_thenKick() {
    final TeamService teamService = new TeamService();
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();

    teamService.create(kicker, "Team0");
    teamService.sendInvitation(kicker, kicked);
    teamService.acceptInvitation(kicker, kicked);

    kicked.disconnect();

    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertDoesNotThrow(() -> teamService.kick(kicker, offlineKicked));
    this.assertPlayerHasNoTeam(kicked);
  }

  @Test
  void givenPlayer_whenLeaveAndHasNoTeam_thenThrowValidationException() {
    final TeamService teamService = new TeamService();
    final PlayerMock player = this.server.addPlayer();

    Assertions.assertThrows(ValidationException.class, () -> teamService.leave(player));
  }

  @Test
  void givenPlayer_whenLeaveAndHasTeamAndIsTeamMember_thenLeave() {
    final TeamService teamService = new TeamService();
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();

    teamService.create(player0, "Team0");
    teamService.sendInvitation(player0, player1);
    teamService.acceptInvitation(player0, player1);

    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final Team team = scoreboard.getEntityTeam(player0);

    Assertions.assertNotNull(team);
    Assertions.assertEquals(team.getName(), teamService.leave(player1).getName());
    this.assertPlayerHasNoTeam(player1);
  }

  private void assertPlayerInTeamAndIsTeamLeader(
      final @NotNull PlayerMock player, final @NotNull Team team) {
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final Team playerTeam = scoreboard.getEntityTeam(player);
    final AccessCache accessCache = new AccessCache();

    Assertions.assertNotNull(playerTeam);
    Assertions.assertEquals(team.getName(), playerTeam.getName());
    Assertions.assertTrue(player.hasPermission(PermissionConstant.TEAM_LEADER.toString()));
    Assertions.assertTrue(player.hasPermission(PermissionConstant.TEAM_MEMBER.toString()));
    Assertions.assertTrue(
        accessCache
            .get(player.getUniqueId())
            .getAccesses()
            .containsAll(
                Set.of(
                    PermissionConstant.TEAM_LEADER.toString(),
                    PermissionConstant.TEAM_MEMBER.toString())));
  }

  private void assertPlayerInTeamAndIsTeamMember(
      final @NotNull PlayerMock player, final @NotNull Team team) {
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final Team playerTeam = scoreboard.getEntityTeam(player);
    final AccessCache accessCache = new AccessCache();

    Assertions.assertNotNull(playerTeam);
    Assertions.assertEquals(team.getName(), playerTeam.getName());
    Assertions.assertFalse(player.hasPermission(PermissionConstant.TEAM_LEADER.toString()));
    Assertions.assertTrue(player.hasPermission(PermissionConstant.TEAM_MEMBER.toString()));

    Assertions.assertFalse(
        accessCache
            .get(player.getUniqueId())
            .getAccesses()
            .contains(PermissionConstant.TEAM_LEADER.toString()));

    Assertions.assertTrue(
        accessCache
            .get(player.getUniqueId())
            .getAccesses()
            .contains(PermissionConstant.TEAM_MEMBER.toString()));
  }

  private void assertPlayerHasNoTeam(final @NotNull PlayerMock player) {
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final Team playerTeam = scoreboard.getEntityTeam(player);

    Assertions.assertNull(playerTeam);
  }
}
