package net.mineasterisk.mc.service.access;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.cache.access.AccessCache;
import net.mineasterisk.mc.constant.PermissionConstant;
import net.mineasterisk.mc.util.LoaderUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class AccessServiceTest {
  private final @NotNull MockedStatic<LoaderUtil> loaderUtil = Mockito.mockStatic(LoaderUtil.class);
  private @NotNull ServerMock server;
  private HashMap<UUID, PermissionAttachment> permissionAttachments;

  @BeforeEach
  public void setUp() throws NoSuchFieldException, IllegalAccessException {
    final Field field = AccessService.class.getDeclaredField("PERMISSION_ATTACHMENTS");

    field.setAccessible(true);

    this.server = MockBukkit.mock();
    //noinspection unchecked
    permissionAttachments = (HashMap<UUID, PermissionAttachment>) field.get(null);
    MockBukkit.load(MineAsterisk.class);
  }

  @AfterEach
  public void tearDown() {
    permissionAttachments.clear();
    MockBukkit.unmock();
    loaderUtil.close();
  }

  @Test
  void givenPlayerAndPermission_whenPlayerHasNoPermission_thenAdd() {
    final AccessCache accessCache = new AccessCache();
    final PlayerMock player = this.server.addPlayer();
    final AccessService accessService = new AccessService(player);
    final String permission = PermissionConstant.TEAM_LEADER.toString();
    final Set<String> accesses = accessCache.get(player.getUniqueId()).getAccesses();

    accessService.add(permission);

    Assertions.assertTrue(player.hasPermission(permission));
    Assertions.assertTrue(accesses.contains(permission));
  }

  @Test
  void givenPlayerAndPermission_whenPlayerHasNoPermissionAndAddPermissions_thenAdd() {
    final AccessCache accessCache = new AccessCache();
    final PlayerMock player = this.server.addPlayer();
    final AccessService accessService = new AccessService(player);
    final String permission0 = PermissionConstant.TEAM_LEADER.toString();
    final String permission1 = PermissionConstant.TEAM_MEMBER.toString();

    accessService.add(permission0);
    accessService.add(permission1);

    final Set<String> accesses = accessCache.get(player.getUniqueId()).getAccesses();

    Assertions.assertTrue(player.hasPermission(permission0));
    Assertions.assertTrue(player.hasPermission(permission1));
    Assertions.assertTrue(accesses.contains(permission0));
    Assertions.assertTrue(accesses.contains(permission1));
  }

  @Test
  void givenPlayerAndPermission_whenPlayerHasPermission_thenRemove() {
    final AccessCache accessCache = new AccessCache();
    final PlayerMock player = this.server.addPlayer();
    final AccessService accessService = new AccessService(player);
    final String permission = PermissionConstant.TEAM_LEADER.toString();

    accessService.add(permission);

    final Set<String> accesses = accessCache.get(player.getUniqueId()).getAccesses();

    Assertions.assertTrue(player.hasPermission(permission));
    Assertions.assertTrue(accesses.contains(permission));

    accessService.remove(permission);

    Assertions.assertFalse(player.hasPermission(permission));
    Assertions.assertFalse(accesses.contains(permission));
  }

  @Test
  void givenPlayerAndPermission_whenPlayerHasPermissionsAndRemovePermission_thenRemove() {
    final AccessCache accessCache = new AccessCache();
    final PlayerMock player = this.server.addPlayer();
    final AccessService accessService = new AccessService(player);
    final String permission0 = PermissionConstant.TEAM_LEADER.toString();
    final String permission1 = PermissionConstant.TEAM_MEMBER.toString();

    accessService.add(permission0);
    accessService.add(permission1);

    final Set<String> accesses = accessCache.get(player.getUniqueId()).getAccesses();

    Assertions.assertTrue(player.hasPermission(permission0));
    Assertions.assertTrue(player.hasPermission(permission1));
    Assertions.assertTrue(accesses.contains(permission0));
    Assertions.assertTrue(accesses.contains(permission1));

    accessService.remove(permission0);

    Assertions.assertFalse(player.hasPermission(permission0));
    Assertions.assertTrue(player.hasPermission(permission1));
    Assertions.assertFalse(accesses.contains(permission0));
    Assertions.assertTrue(accesses.contains(permission1));
  }

  @Test
  void givenPlayerAndPermission_whenPlayerHasPermissionsAndRemovePermissions_thenRemove() {
    final AccessCache accessCache = new AccessCache();
    final PlayerMock player = this.server.addPlayer();
    final AccessService accessService = new AccessService(player);
    final String permission0 = PermissionConstant.TEAM_LEADER.toString();
    final String permission1 = PermissionConstant.TEAM_MEMBER.toString();

    accessService.add(permission0);
    accessService.add(permission1);

    final Set<String> accesses = accessCache.get(player.getUniqueId()).getAccesses();

    Assertions.assertTrue(player.hasPermission(permission0));
    Assertions.assertTrue(player.hasPermission(permission1));
    Assertions.assertTrue(accesses.contains(permission0));
    Assertions.assertTrue(accesses.contains(permission1));

    accessService.remove(permission0);
    accessService.remove(permission1);

    Assertions.assertFalse(player.hasPermission(permission0));
    Assertions.assertFalse(player.hasPermission(permission1));
    Assertions.assertFalse(accesses.contains(permission0));
    Assertions.assertFalse(accesses.contains(permission1));
  }

  @Test
  void givenPlayerAndPermission_whenPlayerHasPermissionAndPlayerIsOffline_thenRemove() {
    final AccessCache accessCache = new AccessCache();
    final PlayerMock player = this.server.addPlayer();
    final OfflinePlayer offlinePlayer = this.server.getOfflinePlayer(player.getUniqueId());
    final String permission = PermissionConstant.TEAM_LEADER.toString();

    new AccessService(player).add(permission);

    final Set<String> accesses = accessCache.get(player.getUniqueId()).getAccesses();

    player.disconnect();
    new AccessService(offlinePlayer).removeIfOffline(permission);

    Assertions.assertFalse(player.hasPermission(permission));
    Assertions.assertFalse(accesses.contains(permission));
  }

  @Test
  void
      givenPlayerAndPermission_whenPlayerHasPermissionsAndPlayerIsOfflineAndRemovePermission_thenRemove() {
    final AccessCache accessCache = new AccessCache();
    final PlayerMock player = this.server.addPlayer();
    final OfflinePlayer offlinePlayer = this.server.getOfflinePlayer(player.getUniqueId());
    final AccessService playerAccessService = new AccessService(player);
    final String permission0 = PermissionConstant.TEAM_LEADER.toString();
    final String permission1 = PermissionConstant.TEAM_MEMBER.toString();

    playerAccessService.add(permission0);
    playerAccessService.add(permission1);

    final Set<String> accesses = accessCache.get(player.getUniqueId()).getAccesses();

    player.disconnect();
    new AccessService(offlinePlayer).removeIfOffline(permission0);

    Assertions.assertFalse(accesses.contains(permission0));
    Assertions.assertTrue(accesses.contains(permission1));
  }

  @Test
  void
      givenPlayerAndPermission_whenPlayerHasPermissionsAndPlayerIsOfflineAndRemovePermissions_thenRemove() {
    final AccessCache accessCache = new AccessCache();
    final PlayerMock player = this.server.addPlayer();
    final OfflinePlayer offlinePlayer = this.server.getOfflinePlayer(player.getUniqueId());
    final AccessService playerAccessService = new AccessService(player);
    final AccessService offlinePlayerAccessService = new AccessService(offlinePlayer);
    final String permission0 = PermissionConstant.TEAM_LEADER.toString();
    final String permission1 = PermissionConstant.TEAM_MEMBER.toString();

    playerAccessService.add(permission0);
    playerAccessService.add(permission1);

    final Set<String> accesses = accessCache.get(player.getUniqueId()).getAccesses();

    player.disconnect();
    offlinePlayerAccessService.removeIfOffline(permission0);
    offlinePlayerAccessService.removeIfOffline(permission1);

    Assertions.assertFalse(accesses.contains(permission0));
    Assertions.assertFalse(accesses.contains(permission1));
  }
}
