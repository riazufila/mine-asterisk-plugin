package net.mineasterisk.mc.util;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.mineasterisk.mc.command.GuildCommand;
import net.mineasterisk.mc.command.HelpCommand;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.help.HelpHandler;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.NotNull;

public class CommandUtil {
  public static void register() {
    PaperCommandManager<CommandSourceStack> manager = CommandUtil.getCommandManager();
    HelpHandler<CommandSourceStack> help = CommandUtil.getHelp(manager);

    new GuildCommand(manager).build().forEach(manager::command);
    new HelpCommand(manager, help).build().forEach(manager::command);
  }

  private static @NotNull PaperCommandManager<@NotNull CommandSourceStack> getCommandManager() {
    return PaperCommandManager.builder()
        .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
        .buildOnEnable(PluginUtil.get());
  }

  private static @NotNull HelpHandler<@NotNull CommandSourceStack> getHelp(
      @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager) {
    return manager.createHelpHandler();
  }
}
