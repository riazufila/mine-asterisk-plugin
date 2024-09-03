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
    final PlayerMock player = this.server.addPlayer();
    final TeamService teamService = new TeamService(player);

    Assertions.assertThrows(ValidationException.class, teamService::getMembers);
  }

  @Test
  void givenPlayer_whenGetMembersAndNoneOfflineAndHasTeam_thenReturnMembers() {
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final PlayerMock player2 = this.server.addPlayer();
    final TeamService teamService0 = new TeamService(player0);
    final TeamService teamService1 = new TeamService(player1);
    final TeamService teamService2 = new TeamService(player2);

    teamService0.create("Team0");

    final Team team = scoreboard.getEntityTeam(player0);

    Assertions.assertNotNull(team);

    teamService0.sendInvitation(player1);
    teamService0.sendInvitation(player2);
    teamService1.acceptInvitation(player0);
    teamService2.acceptInvitation(player0);

    final List<TeamMember> expectedResult =
        List.of(
            new TeamMember(player0.getName(), true),
            new TeamMember(player1.getName(), false),
            new TeamMember(player2.getName(), false));

    Assertions.assertTrue(teamService0.getMembers().containsAll(expectedResult));
  }

  @Test
  void givenPlayer_whenGetMembersAndSomeOfflineAndHasTeam_thenReturnMembers() {
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final PlayerMock player2 = this.server.addPlayer();
    final TeamService teamService0 = new TeamService(player0);
    final TeamService teamService1 = new TeamService(player1);
    final TeamService teamService2 = new TeamService(player2);

    teamService0.create("Team0");

    final Team team = scoreboard.getEntityTeam(player0);

    Assertions.assertNotNull(team);

    teamService0.sendInvitation(player1);
    teamService0.sendInvitation(player2);

    teamService1.acceptInvitation(player0);
    teamService2.acceptInvitation(player0);

    player0.disconnect();
    player2.disconnect();

    final List<TeamMember> expectedResult =
        List.of(
            new TeamMember(player0.getName(), true),
            new TeamMember(player1.getName(), false),
            new TeamMember(player2.getName(), false));

    Assertions.assertTrue(teamService0.getMembers().containsAll(expectedResult));
  }

  @Test
  void givenPlayerAndName_whenCreateAndHasTeamAndIsTeamLeader_thenThrowValidationException() {
    final PlayerMock player = this.server.addPlayer();
    final TeamService teamService = new TeamService(player);

    teamService.create("Team0");

    Assertions.assertThrows(ValidationException.class, () -> teamService.create("Team1"));
  }

  @Test
  void givenPlayerAndName_whenCreateAndHasTeamAndIsTeamMember_thenThrowValidationException() {
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final TeamService teamService0 = new TeamService(player0);
    final TeamService teamService1 = new TeamService(player1);

    teamService0.create("Team0");

    final Team team = scoreboard.getEntityTeam(player0);

    Assertions.assertNotNull(team);

    teamService0.sendInvitation(player1);
    teamService1.acceptInvitation(player0);

    Assertions.assertThrows(ValidationException.class, () -> teamService1.create("Team1"));
  }

  @Test
  void givenPlayerAndName_whenCreateAndNameIsTaken_thenThrowValidationException() {
    final String takenName = "Team0";
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final TeamService teamService0 = new TeamService(player0);
    final TeamService teamService1 = new TeamService(player1);

    teamService0.create(takenName);

    Assertions.assertThrows(ValidationException.class, () -> teamService1.create(takenName));
  }

  @Test
  void givenPlayerAndName_whenCreateAndPlayerHasNoTeamAndNameIsNotTaken_thenCreate() {
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock player = this.server.addPlayer();
    final TeamService teamService = new TeamService(player);
    final String name = "Team0";

    teamService.create(name);

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
    final PlayerMock player = this.server.addPlayer();
    final TeamService teamService = new TeamService(player);

    Assertions.assertThrows(ValidationException.class, teamService::disband);
  }

  @Test
  void givenPlayer_whenDisbandAndHasTeamWithNoOtherMembers_thenDisbandAndReturnOnlineMembers() {
    final PlayerMock player = this.server.addPlayer();
    final TeamService teamService = new TeamService(player);

    teamService.create("Team0");

    final List<Player> members = teamService.disband();

    this.assertPlayerHasNoTeam(player);
    Assertions.assertEquals(0, members.size());
  }

  @Test
  void givenPlayer_whenDisbandAndHasTeamWithNoneOfflineMembers_thenDisbandAndReturnOnlineMembers() {
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final PlayerMock player2 = this.server.addPlayer();
    final TeamService teamService0 = new TeamService(player0);
    final TeamService teamService1 = new TeamService(player1);
    final TeamService teamService2 = new TeamService(player2);

    teamService0.create("Team0");

    teamService0.sendInvitation(player1);
    teamService0.sendInvitation(player2);

    teamService1.acceptInvitation(player0);
    teamService2.acceptInvitation(player0);

    final List<Player> members = teamService0.disband();

    this.assertPlayerHasNoTeam(player0);
    this.assertPlayerHasNoTeam(player1);
    this.assertPlayerHasNoTeam(player2);
    Assertions.assertEquals(2, members.size());
  }

  @Test
  void givenPlayer_whenDisbandAndHasTeamWithSomeOfflineMembers_thenDisbandAndReturnOnlineMembers() {
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final PlayerMock player2 = this.server.addPlayer();
    final TeamService teamService0 = new TeamService(player0);
    final TeamService teamService1 = new TeamService(player1);
    final TeamService teamService2 = new TeamService(player2);

    teamService0.create("Team0");

    teamService0.sendInvitation(player1);
    teamService0.sendInvitation(player2);

    teamService1.acceptInvitation(player0);
    teamService2.acceptInvitation(player0);

    player1.disconnect();

    final List<Player> members = teamService0.disband();

    this.assertPlayerHasNoTeam(player0);
    this.assertPlayerHasNoTeam(player1);
    this.assertPlayerHasNoTeam(player2);
    Assertions.assertEquals(1, members.size());
  }

  @Test
  void givenInviterAndInvitee_whenSendInvitationAndInviteeIsInviter_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    Assertions.assertThrows(ValidationException.class, () -> teamService.sendInvitation(inviter));
  }

  @Test
  void givenInviterAndInvitee_whenSendInvitationAndInviterHasNoTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    Assertions.assertThrows(ValidationException.class, () -> teamService.sendInvitation(invitee));
  }

  @Test
  void givenInviterAndInvitee_whenSendInvitationAndInvitationExists_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    teamService.create("Team0");
    teamService.sendInvitation(invitee);

    Assertions.assertThrows(ValidationException.class, () -> teamService.sendInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenSendInvitationAndInviteeHasTeamAndInviteeIsTeamLeader_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService inviterTeamService = new TeamService(inviter);
    final TeamService inviteeTeamService = new TeamService(invitee);

    inviterTeamService.create("Team0");
    inviteeTeamService.create("Team1");

    Assertions.assertThrows(
        ValidationException.class, () -> inviterTeamService.sendInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenSendInvitationAndInviteeHasTeamAndInviteeIsTeamMember_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();
    final TeamService inviterTeamService = new TeamService(inviter);
    final TeamService inviteeTeamService = new TeamService(invitee);
    final TeamService otherTeamLeaderTeamService = new TeamService(otherTeamLeader);

    inviterTeamService.create("Team0");
    otherTeamLeaderTeamService.create("Team1");

    otherTeamLeaderTeamService.sendInvitation(invitee);
    inviteeTeamService.acceptInvitation(otherTeamLeader);

    Assertions.assertThrows(
        ValidationException.class, () -> inviterTeamService.sendInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenSendInvitationAndInviteeHasNoTeamAndInviterInOriginalTeam_thenSendInvitation() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);
    final String teamName = "Team0";

    teamService.create(teamName);

    Assertions.assertDoesNotThrow(() -> teamService.sendInvitation(invitee));
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
      givenInviterAndInvitee_whenSendInvitationAndInvitationFromOldTeamExistsAndInviteeHasNoTeam_thenSendNewInvitation() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);
    final String oldTeamName = "Team0";
    final String newTeamName = "Team0";

    teamService.create(oldTeamName);
    teamService.sendInvitation(invitee);
    teamService.disband();
    teamService.create(newTeamName);

    Assertions.assertDoesNotThrow(() -> teamService.sendInvitation(invitee));
    Assertions.assertTrue(
        this.invitations.values().stream()
            .anyMatch(
                invitation -> {
                  try {

                    return invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(newTeamName);
                  } catch (IllegalStateException exception) {
                    return false;
                  }
                }));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInvitationDoesNotExist_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    teamService.create("Team0");

    Assertions.assertThrows(ValidationException.class, () -> teamService.acceptInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInvitationDoesNotExistAndInviterHasNoTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    Assertions.assertThrows(ValidationException.class, () -> teamService.acceptInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviteeIsInviterAndInviterHasNoTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    Assertions.assertThrows(ValidationException.class, () -> teamService.acceptInvitation(inviter));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviteeIsInviterAndInviterHasTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    teamService.create("Team0");

    Assertions.assertThrows(ValidationException.class, () -> teamService.acceptInvitation(inviter));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviterDisbandedTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    teamService.create("Team0");
    teamService.sendInvitation(invitee);
    teamService.disband();

    Assertions.assertThrows(ValidationException.class, () -> teamService.acceptInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviterHasDifferentTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    teamService.create("Team0");
    teamService.sendInvitation(invitee);
    teamService.disband();
    teamService.create("Team1");

    Assertions.assertThrows(ValidationException.class, () -> teamService.acceptInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviteeHasTeamAfterInvitationAndInviteeIsTeamLeader_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService inviterTeamService = new TeamService(inviter);
    final TeamService inviteeTeamService = new TeamService(invitee);

    inviterTeamService.create("Team0");
    inviterTeamService.sendInvitation(invitee);
    inviteeTeamService.create("Team1");

    Assertions.assertThrows(
        ValidationException.class, () -> inviterTeamService.acceptInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviteeHasTeamAfterInvitationAndInviteeIsTeamMember_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();
    final TeamService inviterTeamService = new TeamService(inviter);
    final TeamService inviteeTeamService = new TeamService(invitee);
    final TeamService otherTeamLeaderTeamService = new TeamService(otherTeamLeader);

    inviterTeamService.create("Team0");
    inviterTeamService.sendInvitation(invitee);
    otherTeamLeaderTeamService.create("Team1");
    otherTeamLeaderTeamService.sendInvitation(invitee);
    inviteeTeamService.acceptInvitation(otherTeamLeader);

    Assertions.assertThrows(
        ValidationException.class, () -> inviterTeamService.acceptInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenAcceptInvitationAndInviterHasSameTeamAndInviteeHasNoTeamAndInvitationExpires_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);
    final String teamName = "Team0";

    teamService.create(teamName);
    teamService.sendInvitation(invitee);

    this.server.getScheduler().performTicks(Tick.tick().fromDuration(Duration.ofSeconds(31)));

    Assertions.assertThrows(ValidationException.class, () -> teamService.acceptInvitation(invitee));

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
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService inviterTeamService = new TeamService(inviter);
    final TeamService inviteeTeamService = new TeamService(invitee);
    final String teamName = "Team0";

    inviterTeamService.create(teamName);
    inviterTeamService.sendInvitation(invitee);

    Assertions.assertDoesNotThrow(() -> inviteeTeamService.acceptInvitation(inviter));

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
      givenInviterAndInvitee_whenAcceptInvitationAndInviterDifferentTeamAndInviterResentInvitationAndInviteeHasNoTeam_thenAcceptNewInvitation() {
    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService inviterTeamService = new TeamService(inviter);
    final TeamService inviteeTeamService = new TeamService(invitee);
    final String oldTeamName = "Team0";
    final String newTeamName = "Team1";

    inviterTeamService.create(oldTeamName);
    inviterTeamService.sendInvitation(invitee);
    inviterTeamService.disband();
    inviterTeamService.create(newTeamName);
    inviterTeamService.sendInvitation(invitee);

    Assertions.assertDoesNotThrow(() -> inviteeTeamService.acceptInvitation(inviter));

    final Team team = scoreboard.getEntityTeam(invitee);

    Assertions.assertNotNull(team);
    this.assertPlayerInTeamAndIsTeamMember(invitee, team);
    Assertions.assertTrue(
        this.invitations.values().stream()
            .noneMatch(
                invitation -> {
                  try {

                    return invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(newTeamName);
                  } catch (IllegalStateException exception) {
                    return false;
                  }
                }));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInvitationDoesNotExist_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    teamService.create("Team0");

    Assertions.assertThrows(ValidationException.class, () -> teamService.removeInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInvitationDoesNotExistAndInviterHasNoTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    Assertions.assertThrows(ValidationException.class, () -> teamService.removeInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeIsInviterAndInviterHasNoTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    Assertions.assertThrows(ValidationException.class, () -> teamService.removeInvitation(inviter));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeIsInviterAndInviterHasTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    teamService.create("Team0");

    Assertions.assertThrows(ValidationException.class, () -> teamService.removeInvitation(inviter));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviterDisbandedTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    teamService.create("Team0");
    teamService.sendInvitation(invitee);
    teamService.disband();

    Assertions.assertThrows(ValidationException.class, () -> teamService.removeInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviterHasDifferentTeam_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);

    teamService.create("Team0");
    teamService.sendInvitation(invitee);
    teamService.disband();
    teamService.create("Team1");

    Assertions.assertThrows(ValidationException.class, () -> teamService.removeInvitation(invitee));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeHasNoTeamAndInviterHasSameTeamAndInvitationExpires_thenThrowValidationException() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);
    final String teamName = "Team0";

    teamService.create(teamName);
    teamService.sendInvitation(invitee);

    this.server.getScheduler().performTicks(Tick.tick().fromDuration(Duration.ofSeconds(31)));

    Assertions.assertThrows(ValidationException.class, () -> teamService.removeInvitation(invitee));

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
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);
    final String teamName = "Team0";

    teamService.create(teamName);
    teamService.sendInvitation(invitee);

    Assertions.assertDoesNotThrow(() -> teamService.removeInvitation(invitee));

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
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeHasTeamAfterInvitationAndInviteeIsTeamLeaderAndInviterHasSameTeam_thenRemoveInvitation() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService inviterTeamService = new TeamService(inviter);
    final TeamService inviteeTeamService = new TeamService(invitee);
    final String inviterTeamName = "Team0";

    inviterTeamService.create(inviterTeamName);
    inviterTeamService.sendInvitation(invitee);
    inviteeTeamService.create("Team1");

    Assertions.assertDoesNotThrow(() -> inviterTeamService.removeInvitation(invitee));
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
      givenInviterAndInvitee_whenRemoveInvitationAndInviteeHasTeamAfterInvitationAndInviteeIsTeamMemberAndInviterHasSameTeam_thenRemoveInvitation() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();
    final TeamService inviterTeamService = new TeamService(inviter);
    final TeamService inviteeTeamService = new TeamService(invitee);
    final TeamService otherTeamLeaderTeamService = new TeamService(otherTeamLeader);
    final String inviterTeamName = "Team0";

    inviterTeamService.create(inviterTeamName);
    otherTeamLeaderTeamService.create("Team1");
    inviterTeamService.sendInvitation(invitee);
    otherTeamLeaderTeamService.sendInvitation(invitee);
    inviteeTeamService.acceptInvitation(otherTeamLeader);

    Assertions.assertDoesNotThrow(() -> inviterTeamService.removeInvitation(invitee));
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
      givenInviterAndInvitee_whenRemoveInvitationAndInviterHasDifferentTeamAndInviterResentInvitation_thenRemoveNewInvitation() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService teamService = new TeamService(inviter);
    final String oldInviterTeamName = "Team0";
    final String newInviterTeamName = "Team1";

    teamService.create(oldInviterTeamName);
    teamService.sendInvitation(invitee);
    teamService.disband();
    teamService.create(newInviterTeamName);
    teamService.sendInvitation(invitee);

    Assertions.assertDoesNotThrow(() -> teamService.removeInvitation(invitee));
    Assertions.assertTrue(
        this.invitations.values().stream()
            .noneMatch(
                invitation -> {
                  try {

                    return invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(newInviterTeamName);
                  } catch (IllegalStateException exception) {
                    return false;
                  }
                }));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviterHasDifferentTeamAndInviterResentInvitationAndInviteeHasTeamAfterInvitationAndInviteeIsTeamLeader_thenRemoveNewInvitation() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final TeamService inviterTeamService = new TeamService(inviter);
    final TeamService inviteeTeamService = new TeamService(invitee);
    final String oldInviterTeamName = "Team0";
    final String newInviterTeamName = "Team1";
    final String inviteeTeamName = "Team2";

    inviterTeamService.create(oldInviterTeamName);
    inviterTeamService.sendInvitation(invitee);
    inviterTeamService.disband();
    inviterTeamService.create(newInviterTeamName);
    inviterTeamService.sendInvitation(invitee);
    inviteeTeamService.create(inviteeTeamName);

    Assertions.assertDoesNotThrow(() -> inviterTeamService.removeInvitation(invitee));
    Assertions.assertTrue(
        this.invitations.values().stream()
            .noneMatch(
                invitation -> {
                  try {

                    return invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(newInviterTeamName);
                  } catch (IllegalStateException exception) {
                    return false;
                  }
                }));
  }

  @Test
  void
      givenInviterAndInvitee_whenRemoveInvitationAndInviterHasDifferentTeamAndInviterResentInvitationAndInviteeHasTeamAfterInvitationAndInviteeIsTeamMember_thenRemoveNewInvitation() {
    final PlayerMock inviter = this.server.addPlayer();
    final PlayerMock invitee = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();
    final TeamService inviterTeamService = new TeamService(inviter);
    final TeamService inviteeTeamService = new TeamService(invitee);
    final TeamService otherTeamLeaderTeamService = new TeamService(otherTeamLeader);
    final String oldInviterTeamName = "Team0";
    final String newInviterTeamName = "Team1";
    final String otherTeamLeaderTeamName = "Team2";

    inviterTeamService.create(oldInviterTeamName);
    inviterTeamService.sendInvitation(invitee);
    inviterTeamService.disband();
    inviterTeamService.create(newInviterTeamName);
    inviterTeamService.sendInvitation(invitee);
    otherTeamLeaderTeamService.create(otherTeamLeaderTeamName);
    otherTeamLeaderTeamService.sendInvitation(invitee);
    inviteeTeamService.acceptInvitation(otherTeamLeader);

    Assertions.assertDoesNotThrow(() -> inviterTeamService.removeInvitation(invitee));
    Assertions.assertTrue(
        this.invitations.values().stream()
            .noneMatch(
                invitation -> {
                  try {

                    return invitation.invitee().getUniqueId().equals(invitee.getUniqueId())
                        && invitation.inviter().getUniqueId().equals(inviter.getUniqueId())
                        && invitation.team().getName().equals(newInviterTeamName);
                  } catch (IllegalStateException exception) {
                    return false;
                  }
                }));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedNeverPlayedAndOfflineKickedIsNotOnline_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final TeamService teamService = new TeamService(kicker);
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(UUID.randomUUID());

    Assertions.assertThrows(ValidationException.class, () -> teamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedHasNoNameAndOfflineKickedIsOnline_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService teamService = new TeamService(kicker);
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = Mockito.spy(this.server.getOfflinePlayer(kickedUuid));

    Mockito.when(offlineKicked.getName()).thenReturn(null);

    Assertions.assertThrows(ValidationException.class, () -> teamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedHasNoNameAndOfflineKickedIsOffline_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService teamService = new TeamService(kicker);
    final UUID kickedUuid = kicked.getUniqueId();

    kicked.disconnect();

    final OfflinePlayer offlineKicked = Mockito.spy(this.server.getOfflinePlayer(kickedUuid));

    Mockito.when(offlineKicked.getName()).thenReturn(null);

    Assertions.assertThrows(ValidationException.class, () -> teamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndKickerIsNotInTeamAndOfflineKickedIsOnline_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService teamService = new TeamService(kicker);
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertThrows(ValidationException.class, () -> teamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndKickerIsNotInTeamAndOfflineKickedIsOffline_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService teamService = new TeamService(kicker);
    final UUID kickedUuid = kicked.getUniqueId();

    kicked.disconnect();

    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertThrows(ValidationException.class, () -> teamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOnlineAndOfflineKickedHasNoTeam_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService teamService = new TeamService(kicker);
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    teamService.create("Team0");

    Assertions.assertThrows(ValidationException.class, () -> teamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOfflineAndOfflineKickedHasNoTeam_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService teamService = new TeamService(kicker);
    final UUID kickedUuid = kicked.getUniqueId();

    kicked.disconnect();

    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    teamService.create("Team0");

    Assertions.assertThrows(ValidationException.class, () -> teamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOnlineAndTeamIsDisbanded_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService kickerTeamService = new TeamService(kicker);
    final TeamService kickedTeamService = new TeamService(kicked);
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    kickerTeamService.create("Team0");
    kickerTeamService.sendInvitation(kicked);
    kickedTeamService.acceptInvitation(kicker);
    kickerTeamService.disband();

    Assertions.assertThrows(ValidationException.class, () -> kickerTeamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOfflineAndTeamIsDisbanded_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService kickerTeamService = new TeamService(kicker);
    final TeamService kickedTeamService = new TeamService(kicked);

    kickerTeamService.create("Team0");
    kickerTeamService.sendInvitation(kicked);
    kickedTeamService.acceptInvitation(kicker);
    kickerTeamService.disband();

    kicked.disconnect();

    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertThrows(ValidationException.class, () -> kickerTeamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOnlineAndTeamIsDifferentAndOfflineKickedIsTeamLeader_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService kickerTeamService = new TeamService(kicker);
    final TeamService kickedTeamService = new TeamService(kicked);
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    kickerTeamService.create("Team0");
    kickedTeamService.create("Team1");

    Assertions.assertThrows(ValidationException.class, () -> kickerTeamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOfflineAndTeamIsDifferentAndOfflineKickedIsTeamLeader_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService kickerTeamService = new TeamService(kicker);
    final TeamService kickedTeamService = new TeamService(kicked);

    kickerTeamService.create("Team0");
    kickerTeamService.sendInvitation(kicked);
    kickedTeamService.acceptInvitation(kicker);
    kickerTeamService.disband();

    kicked.disconnect();

    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertThrows(ValidationException.class, () -> kickerTeamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOnlineAndTeamIsDifferentAndOfflineKickedIsTeamMember_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();
    final TeamService kickerTeamService = new TeamService(kicker);
    final TeamService kickedTeamService = new TeamService(kicked);
    final TeamService otherTeamLeaderTeamService = new TeamService(otherTeamLeader);
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    kickerTeamService.create("Team0");
    otherTeamLeaderTeamService.create("Team1");
    otherTeamLeaderTeamService.sendInvitation(kicked);
    kickedTeamService.acceptInvitation(otherTeamLeader);

    Assertions.assertThrows(ValidationException.class, () -> kickerTeamService.kick(offlineKicked));
  }

  @Test
  void
      givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOfflineAndTeamIsDifferentAndOfflineKickedIsTeamMember_thenThrowValidationException() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final PlayerMock otherTeamLeader = this.server.addPlayer();
    final TeamService kickerTeamService = new TeamService(kicker);
    final TeamService kickedTeamService = new TeamService(kicked);
    final TeamService otherTeamLeaderTeamService = new TeamService(otherTeamLeader);

    kickerTeamService.create("Team0");
    otherTeamLeaderTeamService.create("Team1");
    otherTeamLeaderTeamService.sendInvitation(kicked);
    kickedTeamService.acceptInvitation(otherTeamLeader);

    kicked.disconnect();

    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertThrows(ValidationException.class, () -> kickerTeamService.kick(offlineKicked));
  }

  @Test
  void givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOnline_thenKick() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService kickerTeamService = new TeamService(kicker);
    final TeamService kickedTeamService = new TeamService(kicked);
    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    kickerTeamService.create("Team0");
    kickerTeamService.sendInvitation(kicked);
    kickedTeamService.acceptInvitation(kicker);

    Assertions.assertDoesNotThrow(() -> kickerTeamService.kick(offlineKicked));
    this.assertPlayerHasNoTeam(kicked);
  }

  @Test
  void givenKickerAndOfflineKicked_whenKickAndOfflineKickedIsOffline_thenKick() {
    final PlayerMock kicker = this.server.addPlayer();
    final PlayerMock kicked = this.server.addPlayer();
    final TeamService kickerTeamService = new TeamService(kicker);
    final TeamService kickedTeamService = new TeamService(kicked);

    kickerTeamService.create("Team0");
    kickerTeamService.sendInvitation(kicked);
    kickedTeamService.acceptInvitation(kicker);

    kicked.disconnect();

    final UUID kickedUuid = kicked.getUniqueId();
    final OfflinePlayer offlineKicked = this.server.getOfflinePlayer(kickedUuid);

    Assertions.assertDoesNotThrow(() -> kickerTeamService.kick(offlineKicked));
    this.assertPlayerHasNoTeam(kicked);
  }

  @Test
  void givenPlayer_whenLeaveAndHasNoTeam_thenThrowValidationException() {
    final PlayerMock player = this.server.addPlayer();
    final TeamService teamService = new TeamService(player);

    Assertions.assertThrows(ValidationException.class, teamService::leave);
  }

  @Test
  void givenPlayer_whenLeaveAndHasTeamAndIsTeamMember_thenLeave() {
    final PlayerMock player0 = this.server.addPlayer();
    final PlayerMock player1 = this.server.addPlayer();
    final TeamService teamService0 = new TeamService(player0);
    final TeamService teamService1 = new TeamService(player1);

    teamService0.create("Team0");
    teamService0.sendInvitation(player1);
    teamService1.acceptInvitation(player0);

    final ScoreboardManagerMock manager = this.server.getScoreboardManager();
    final ScoreboardMock scoreboard = manager.getMainScoreboard();
    final Team team = scoreboard.getEntityTeam(player0);

    Assertions.assertNotNull(team);
    Assertions.assertEquals(team.getName(), teamService1.leave().getName());
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
