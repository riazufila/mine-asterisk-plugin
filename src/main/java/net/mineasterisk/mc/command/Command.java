package net.mineasterisk.mc.command;

import java.util.concurrent.CompletionException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.exception.MissingEntityException;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

public abstract class Command {
  protected void exceptionHandler(
      final @NotNull CommandSender sender,
      final @NotNull String command,
      final @NotNull Session session,
      final @NotNull Exception exception) {
    String message = "Encountered error";
    Throwable throwable = exception;

    if (exception instanceof CompletionException) {
      throwable = exception.getCause();
    }

    if (throwable instanceof MissingEntityException) {
      message = ((MissingEntityException) throwable).getClientMessage();
    } else if (throwable instanceof ValidationException) {
      message = ((ValidationException) throwable).getClientMessage();
    }

    session.getTransaction().rollback();

    PluginUtil.getLogger()
        .severe(String.format("Unable to execute %s command: %s", command, exception));

    if (sender instanceof Player performedBy) {
      performedBy.sendMessage(Component.text(message).color(NamedTextColor.RED));
    }
  }
}
