package net.mineasterisk.mc.util;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.command.HelpCommand;
import net.mineasterisk.mc.command.MessageCommand;
import net.mineasterisk.mc.command.TeamCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class CommandUtil {
  protected static void register() {
    final LifecycleEventManager<Plugin> manager = MineAsterisk.getInstance().getLifecycleManager();

    manager.registerEventHandler(
        LifecycleEvents.COMMANDS,
        event -> {
          final Commands commands = event.registrar();

          commands.register(new TeamCommand().build());
          commands.register(new MessageCommand().build());
          commands.register(new HelpCommand(commands.getDispatcher()).build());
        });

    MineAsterisk.getInstance().getLogger().info("Registered command(s)");
  }

  public static @NotNull Player getPlayer(
      final @NotNull CommandSender sender, final @Nullable Entity executor) {
    if (executor == null) {
      throw new IllegalStateException(
          "Executor isn't initialized and tries to execute command that requires execution as Player");
    }

    if (!(sender.getName().equals(executor.getName())) && sender.isOp()) {
      return (Player) executor;
    }

    if (!(sender.getName().equals(executor.getName())) && !sender.isOp()) {
      throw new IllegalStateException(
          String.format(
              "Sender %s and Executor %s (%s) is different and Sender %s is not doesn't have authorization",
              sender.getName(), executor.getName(), executor.getUniqueId(), sender.getName()));
    }

    if (!(executor instanceof Player)) {
      throw new IllegalStateException(
          String.format(
              "Executor %s (%s) isn't a Player and tries to execute command that requires execution as Player",
              executor.getName(), executor.getUniqueId()));
    }

    return (Player) executor;
  }
}
