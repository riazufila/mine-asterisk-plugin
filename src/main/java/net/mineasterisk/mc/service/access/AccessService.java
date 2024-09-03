package net.mineasterisk.mc.service.access;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.cache.access.AccessCache;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccessService implements Listener {
  private static final @NotNull HashMap<@NotNull UUID, @NotNull PermissionAttachment>
      PERMISSION_ATTACHMENTS = new HashMap<>();

  private @Nullable Player player = null;
  private @Nullable OfflinePlayer offlinePlayer = null;

  public AccessService() {}

  public AccessService(final @NotNull Player player) {
    this.player = player;
  }

  public AccessService(final @NotNull OfflinePlayer offlinePlayer) {
    this.offlinePlayer = offlinePlayer;
  }

  private PermissionAttachment get() {
    if (this.player == null) {
      throw new IllegalStateException("Player is supposed to be initialized");
    }

    return AccessService.PERMISSION_ATTACHMENTS.computeIfAbsent(
        this.player.getUniqueId(), key -> this.player.addAttachment(MineAsterisk.getInstance()));
  }

  public void add(final @NotNull String permission) {
    final PermissionAttachment permissionAttachment = this.get();
    final AccessCache accessCache = new AccessCache();

    if (this.player == null) {
      throw new IllegalStateException("Player is supposed to be initialized");
    }

    final UUID uuid = this.player.getUniqueId();

    if (this.player.hasPermission(permission)) {
      return;
    }

    permissionAttachment.setPermission(permission, true);
    this.player.updateCommands();

    accessCache.get(uuid).addAccess(permission);
  }

  public void remove(final @NotNull String permission) {
    final PermissionAttachment permissionAttachment = this.get();
    final AccessCache accessCache = new AccessCache();

    if (this.player == null) {
      throw new IllegalStateException("Player is supposed to be initialized");
    }

    final UUID uuid = this.player.getUniqueId();

    if (!this.player.hasPermission(permission)) {
      return;
    }

    permissionAttachment.unsetPermission(permission);
    this.player.updateCommands();

    accessCache.get(uuid).removeAccess(permission);
  }

  public void removeIfOffline(final @NotNull String permission) {
    final AccessCache accessCache = new AccessCache();

    if (this.offlinePlayer == null) {
      throw new IllegalStateException("Offline Player is supposed to be initialized");
    }

    accessCache.get(this.offlinePlayer.getUniqueId()).removeAccess(permission);
  }

  @EventHandler
  public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
    this.player = event.getPlayer();
    final AccessCache accessCache = new AccessCache();
    final Set<String> permissionsCached = accessCache.get(this.player.getUniqueId()).getAccesses();

    if (permissionsCached.isEmpty()) {
      return;
    }

    final PermissionAttachment permissionAttachment = this.get();

    permissionsCached.forEach(
        permissionCached -> permissionAttachment.setPermission(permissionCached, true));

    this.player.updateCommands();

    MineAsterisk.getInstance()
        .getLogger()
        .info(
            String.format(
                "Attached Permission(s) to Player %s %s",
                this.player.getName(), this.player.getUniqueId()));
  }

  @EventHandler
  public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    this.player = event.getPlayer();

    AccessService.PERMISSION_ATTACHMENTS.remove(this.player.getUniqueId());
  }
}
