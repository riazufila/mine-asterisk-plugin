package net.mineasterisk.mc.service.access;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import net.mineasterisk.mc.cache.access.AccessCache;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;

public class AccessService implements Listener {
  private static final @NotNull HashMap<@NotNull UUID, @NotNull PermissionAttachment>
      PERMISSION_ATTACHMENTS = new HashMap<>();

  private PermissionAttachment get(final @NotNull Player player) {
    return AccessService.PERMISSION_ATTACHMENTS.computeIfAbsent(
        player.getUniqueId(), key -> player.addAttachment(PluginUtil.get()));
  }

  public void add(final @NotNull Player player, final @NotNull String permission) {
    final PermissionAttachment permissionAttachment = this.get(player);
    final AccessCache accessCache = new AccessCache();
    final UUID uuid = player.getUniqueId();

    if (player.hasPermission(permission)) {
      return;
    }

    permissionAttachment.setPermission(permission, true);
    player.updateCommands();

    accessCache.get(uuid).addAccess(permission);
  }

  public void remove(final @NotNull Player player, final @NotNull String permission) {
    final PermissionAttachment permissionAttachment = this.get(player);
    final AccessCache accessCache = new AccessCache();
    final UUID uuid = player.getUniqueId();

    if (!player.hasPermission(permission)) {
      return;
    }

    permissionAttachment.unsetPermission(permission);
    player.updateCommands();

    accessCache.get(uuid).removeAccess(permission);
  }

  public void removeIfOffline(final @NotNull UUID uuid, final @NotNull String permission) {
    final AccessCache accessCache = new AccessCache();

    accessCache.get(uuid).removeAccess(permission);
  }

  private void attach(final @NotNull Player player) {
    final AccessCache accessCache = new AccessCache();
    final Set<String> permissionsCached = accessCache.get(player.getUniqueId()).getAccesses();

    if (permissionsCached.isEmpty()) {
      return;
    }

    final PermissionAttachment permissionAttachment = this.get(player);

    permissionsCached.forEach(
        permissionCached -> permissionAttachment.setPermission(permissionCached, true));

    player.updateCommands();

    PluginUtil.getLogger()
        .info(
            String.format(
                "Attached Permission(s) to Player %s %s", player.getName(), player.getUniqueId()));
  }

  private void detach(final @NotNull Player player) {
    AccessService.PERMISSION_ATTACHMENTS.remove(player.getUniqueId());
  }

  @EventHandler
  public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
    this.attach(event.getPlayer());
  }

  @EventHandler
  public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
    this.detach(event.getPlayer());
  }
}
