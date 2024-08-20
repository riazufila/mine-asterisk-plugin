package net.mineasterisk.mc.util;

import java.util.concurrent.CompletionException;
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
    String message = "Encountered error";
    Throwable throwable = exception;

    if (exception instanceof CompletionException) {
      throwable = exception.getCause();
    }

    if (throwable instanceof ValidationException) {
      message = ((ValidationException) throwable).getClientMessage();
    }

    PluginUtil.getLogger()
        .severe(
            String.format("Encountered error while executing %s command: %s", command, exception));

    if (sender instanceof Player performedBy) {
      performedBy.sendMessage(Component.text(message).color(NamedTextColor.RED));
    }
  }
}
