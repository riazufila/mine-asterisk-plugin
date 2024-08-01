package net.mineasterisk.mc.util;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.mineasterisk.mc.command.GuildCommand;
import net.mineasterisk.mc.command.HelpCommand;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.help.HelpHandler;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.NotNull;

public class CommandUtil {
  private static @NotNull PaperCommandManager<@NotNull CommandSourceStack> getCommandManager() {
    return PaperCommandManager.builder()
        .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
        .buildOnEnable(PluginUtil.get());
  }

  private static @NotNull HelpHandler<@NotNull CommandSourceStack> getHelp(
      @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager) {
    return manager.createHelpHandler();
  }

  public static void register() {
    PaperCommandManager<CommandSourceStack> manager = CommandUtil.getCommandManager();
    HelpHandler<CommandSourceStack> help = CommandUtil.getHelp(manager);

    GuildCommand.build(manager).forEach(manager::command);
    HelpCommand.build(manager, help).forEach(manager::command);
  }
}
