package net.mineasterisk.mc.service.player;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.service.team.TeamService;
import net.mineasterisk.mc.util.LoaderUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class PlayerServiceTest {
  private final @NotNull MockedStatic<LoaderUtil> loaderUtil = Mockito.mockStatic(LoaderUtil.class);
  private @NotNull ServerMock server;

  @BeforeEach
  public void setUp() {
    this.server = MockBukkit.mock();
    MockBukkit.load(MineAsterisk.class);
  }

  @AfterEach
  public void tearDown() {
    MockBukkit.unmock();
    loaderUtil.close();
  }

  @Test
  void givenPlayerHasNoTeam_whenPlayerMessageRecipient_thenMessage() {
    final PlayerMock sender = this.server.addPlayer();
    final PlayerMock recipient = this.server.addPlayer();
    final PlayerService playerService = new PlayerService(sender);
    final String MESSAGE = "Message0";

    Assertions.assertDoesNotThrow(() -> playerService.message(recipient, MESSAGE));
  }

  @Test
  void givenPlayerHasTeam_whenPlayerMessageRecipient_thenMessage() {
    final PlayerMock sender = this.server.addPlayer();
    final PlayerMock recipient = this.server.addPlayer();
    final PlayerService playerService = new PlayerService(sender);
    final TeamService teamService = new TeamService(sender);
    final String MESSAGE = "Message0";

    teamService.create("Team0");

    Assertions.assertDoesNotThrow(() -> playerService.message(recipient, MESSAGE));
  }
}
