package net.mineasterisk.mc.repository;

import be.seeseemelk.mockbukkit.MockBukkit;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.cache.access.Access;
import net.mineasterisk.mc.constant.PermissionConstant;
import net.mineasterisk.mc.util.DatabaseUtil;
import net.mineasterisk.mc.util.LoaderUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class AccessRepositoryTest {
  private final @NotNull MockedStatic<@NotNull LoaderUtil> loaderUtil =
      Mockito.mockStatic(LoaderUtil.class);

  private @NotNull Connection connection;

  @BeforeEach
  public void setUp() throws SQLException {
    MockBukkit.mock();
    MockBukkit.load(MineAsterisk.class);
    DatabaseUtil.initialize();

    this.connection = DatabaseUtil.getConnection();
    final CallableStatement statement = this.connection.prepareCall("{CALL reset()}");

    statement.execute();
  }

  @AfterEach
  public void tearDown() throws SQLException {
    final CallableStatement statement = this.connection.prepareCall("{CALL reset()}");

    statement.execute();
    this.connection.close();
    MockBukkit.unmock();
    loaderUtil.close();
  }

  @Test
  void givenUnpopulatedAccess_whenGetAllPlayersAccesses_thenReturnEmptyMap() {
    final AccessRepository accessRepository = new AccessRepository(this.connection);
    final HashMap<UUID, Access> playersAccesses = accessRepository.getAllPlayersAccesses().join();

    Assertions.assertTrue(playersAccesses.isEmpty());
  }

  @Test
  void givenPopulatedAccess_whenGetAllPlayersAccesses_thenReturnPopulatedMap() {
    final AccessRepository accessRepository = new AccessRepository(this.connection);
    final PlayerRepository playerRepository = new PlayerRepository(this.connection);
    final HashMap<UUID, Access> playersAccessesToUpdate = new HashMap<>();
    final UUID uuid0 = UUID.randomUUID();
    final UUID uuid1 = UUID.randomUUID();
    final Access access0 =
        new Access(
            Set.of(
                PermissionConstant.TEAM_LEADER.toString(),
                PermissionConstant.TEAM_MEMBER.toString()));

    final Access access1 = new Access(Set.of(PermissionConstant.TEAM_MEMBER.toString()));

    playerRepository.insert(uuid0).join();
    playerRepository.insert(uuid1).join();
    playersAccessesToUpdate.put(uuid0, access0);
    playersAccessesToUpdate.put(uuid1, access1);
    accessRepository.updatePlayersAccesses(playersAccessesToUpdate).join();

    final HashMap<UUID, Access> playersAccesses = accessRepository.getAllPlayersAccesses().join();

    Assertions.assertFalse(playersAccesses.isEmpty());
    Assertions.assertEquals(2, playersAccesses.size());
  }

  @Test
  void givenPlayerAccess_whenUpdatePlayerAccess_thenUpdatePlayerAccess() {
    final AccessRepository accessRepository = new AccessRepository(this.connection);
    final PlayerRepository playerRepository = new PlayerRepository(this.connection);
    final HashMap<UUID, Access> playerAccessToUpdate = new HashMap<>();
    final UUID uuid = UUID.randomUUID();
    final Set<String> permissions = new HashSet<>();

    permissions.add(PermissionConstant.TEAM_MEMBER.toString());

    final Access access = new Access(permissions);

    playerRepository.insert(uuid).join();
    playerAccessToUpdate.put(uuid, access);
    accessRepository.updatePlayersAccesses(playerAccessToUpdate).join();

    final Set<String> playerAccesses =
        accessRepository.getAllPlayersAccesses().join().get(uuid).getAccesses();

    Assertions.assertFalse(playerAccesses.isEmpty());
    Assertions.assertEquals(1, playerAccesses.size());

    playerAccessToUpdate.get(uuid).getAccesses().add(PermissionConstant.TEAM_LEADER.toString());
    accessRepository.updatePlayersAccesses(playerAccessToUpdate).join();

    final Set<String> playerAccessesAfterLastUpdate =
        accessRepository.getAllPlayersAccesses().join().get(uuid).getAccesses();

    Assertions.assertFalse(playerAccessesAfterLastUpdate.isEmpty());
    Assertions.assertEquals(2, playerAccessesAfterLastUpdate.size());
  }

  @Test
  void givenPlayersAccesses_whenUpdatePlayersAccesses_thenUpdatePlayersAccesses() {
    final AccessRepository accessRepository = new AccessRepository(this.connection);
    final PlayerRepository playerRepository = new PlayerRepository(this.connection);
    final HashMap<UUID, Access> playerAccessToUpdate = new HashMap<>();
    final UUID uuid0 = UUID.randomUUID();
    final UUID uuid1 = UUID.randomUUID();
    final Set<String> permissions0 = new HashSet<>();
    final Set<String> permissions1 = new HashSet<>();

    permissions0.add(PermissionConstant.TEAM_MEMBER.toString());
    permissions1.add(PermissionConstant.TEAM_LEADER.toString());
    permissions1.add(PermissionConstant.TEAM_MEMBER.toString());

    final Access access0 = new Access(permissions0);
    final Access access1 = new Access(permissions1);

    playerRepository.insert(uuid0).join();
    playerRepository.insert(uuid1).join();
    playerAccessToUpdate.put(uuid0, access0);
    playerAccessToUpdate.put(uuid1, access1);
    accessRepository.updatePlayersAccesses(playerAccessToUpdate).join();

    final Set<String> playerAccesses0 =
        accessRepository.getAllPlayersAccesses().join().get(uuid0).getAccesses();

    final Set<String> playerAccesses1 =
        accessRepository.getAllPlayersAccesses().join().get(uuid1).getAccesses();

    Assertions.assertFalse(playerAccesses0.isEmpty());
    Assertions.assertEquals(1, playerAccesses0.size());
    Assertions.assertFalse(playerAccesses1.isEmpty());
    Assertions.assertEquals(2, playerAccesses1.size());

    playerAccessToUpdate.get(uuid0).getAccesses().add(PermissionConstant.TEAM_LEADER.toString());
    playerAccessToUpdate.get(uuid1).getAccesses().remove(PermissionConstant.TEAM_LEADER.toString());
    accessRepository.updatePlayersAccesses(playerAccessToUpdate).join();

    final Set<String> playerAccessesAfterLastUpdate0 =
        accessRepository.getAllPlayersAccesses().join().get(uuid0).getAccesses();

    final Set<String> playerAccessesAfterLastUpdate1 =
        accessRepository.getAllPlayersAccesses().join().get(uuid1).getAccesses();

    Assertions.assertFalse(playerAccessesAfterLastUpdate0.isEmpty());
    Assertions.assertEquals(2, playerAccessesAfterLastUpdate0.size());
    Assertions.assertFalse(playerAccessesAfterLastUpdate1.isEmpty());
    Assertions.assertEquals(1, playerAccessesAfterLastUpdate1.size());
  }
}
