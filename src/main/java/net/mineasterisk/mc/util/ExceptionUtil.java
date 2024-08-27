package net.mineasterisk.mc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.exception.ValidationException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExceptionUtil {
  public static void handleCommand(
      final @NotNull Exception exception,
      final @NotNull CommandSender sender,
      final @NotNull String command) {
    String clientMessage = "Encountered error";

    if (exception instanceof ValidationException) {
      clientMessage = ((ValidationException) exception).getClientMessage();
    }

    if (sender instanceof Player player) {
      player.sendMessage(Component.text(clientMessage).color(NamedTextColor.RED));
    }

    PluginUtil.getLogger()
        .severe(
            String.format("Encountered error while executing %s command: %s", command, exception));
  }
}
