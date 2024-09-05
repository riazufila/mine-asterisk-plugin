package net.mineasterisk.mc.service.access;

import java.util.HashMap;
import java.util.UUID;
import net.mineasterisk.mc.MineAsterisk;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;

public class AccessServiceManager {
  private static final @NotNull HashMap<@NotNull UUID, @NotNull PermissionAttachment>
      PERMISSION_ATTACHMENTS = new HashMap<>();

  protected static @NotNull PermissionAttachment getPermissionAttachment(
      final @NotNull Player player) {
    return AccessServiceManager.PERMISSION_ATTACHMENTS.computeIfAbsent(
        player.getUniqueId(), key -> player.addAttachment(MineAsterisk.getInstance()));
  }

  protected static void removePermissionAttachment(final @NotNull Player player) {
    AccessServiceManager.PERMISSION_ATTACHMENTS.remove(player.getUniqueId());
  }
}
