package net.mineasterisk.mc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LoggerUtil {
  private final @NotNull Player player;
  private final @NotNull String serverSuccessTitle;
  private final @NotNull String serverErrorTitle;

  public LoggerUtil(
      final @NotNull Player player,
      final @NotNull String serverSuccessTitle,
      final @NotNull String serverErrorTitle) {
    this.player = player;
    this.serverSuccessTitle = serverSuccessTitle;
    this.serverErrorTitle = serverErrorTitle;
  }

  public void success(final @NotNull String playerLog, final @NotNull String serverDescription) {
    PluginUtil.getLogger().warning(String.format("%s: %s", serverSuccessTitle, serverDescription));
    player.sendMessage(Component.text(playerLog).color(NamedTextColor.GREEN));
  }

  public void warn(final @NotNull String playerMessage, final @NotNull String serverDescription) {
    PluginUtil.getLogger().warning(String.format("%s: %s", serverErrorTitle, serverDescription));
    player.sendMessage(Component.text(playerMessage).color(NamedTextColor.RED));
  }

  public void error(final @NotNull String playerMessage, final @NotNull String serverDescription) {
    PluginUtil.getLogger().severe(String.format("%s: %s", serverErrorTitle, serverDescription));
    player.sendMessage(Component.text(playerMessage).color(NamedTextColor.RED));
  }
}
