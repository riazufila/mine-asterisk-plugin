package net.mineasterisk.mc.repository;

import be.seeseemelk.mockbukkit.MockBukkit;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.util.DatabaseUtil;
import net.mineasterisk.mc.util.LoaderUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class PlayerRepositoryTest {
  private final @NotNull MockedStatic<LoaderUtil> loaderUtil = Mockito.mockStatic(LoaderUtil.class);
  private Connection connection;

  @BeforeEach
  public void setUp() {
    MockBukkit.mock();
    MockBukkit.load(MineAsterisk.class);

    this.connection = DatabaseUtil.getConnection();
  }

  @AfterEach
  public void tearDown() throws SQLException {
    this.connection.rollback();
    this.connection.close();
    MockBukkit.unmock();
    loaderUtil.close();
  }

  @Test
  void givenRandomUuid_whenCheckIsPlayerExist_thenReturnFalse() {
    final PlayerRepository playerRepository = new PlayerRepository(this.connection);
    final UUID randomUuid = UUID.randomUUID();
    final boolean isPlayerExist = playerRepository.isPlayerExist(randomUuid).join();

    Assertions.assertFalse(isPlayerExist);
  }

  @Test
  void givenUuid_whenCheckIsPlayerExist_thenReturnTrue() {
    final PlayerRepository playerRepository = new PlayerRepository(this.connection);
    final UUID randomUuid = UUID.randomUUID();

    playerRepository.insert(randomUuid).join();

    final boolean isPlayerExist = playerRepository.isPlayerExist(randomUuid).join();

    Assertions.assertTrue(isPlayerExist);
  }

  @Test
  void givenUuid_whenInsert_thenInsert() {
    final PlayerRepository playerRepository = new PlayerRepository(this.connection);
    final UUID uuid = UUID.randomUUID();

    Assertions.assertDoesNotThrow(() -> playerRepository.insert(uuid).join());
  }
}
