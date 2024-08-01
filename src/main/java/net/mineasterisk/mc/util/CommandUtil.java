package net.mineasterisk.mc.util;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.mineasterisk.mc.command.GuildCommand;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.NotNull;

public class CommandUtil {
  private static @NotNull PaperCommandManager<@NotNull CommandSourceStack> getCommandManager() {
    return PaperCommandManager.builder()
        .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
        .buildOnEnable(PluginUtil.get());
  }

  public static void register() {
    PaperCommandManager<CommandSourceStack> manager = CommandUtil.getCommandManager();

    GuildCommand.build(manager).forEach(manager::command);
  }
}
