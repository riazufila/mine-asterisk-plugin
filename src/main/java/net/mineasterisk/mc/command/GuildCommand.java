package net.mineasterisk.mc.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Set;
import org.incendo.cloud.Command.Builder;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

public class GuildCommand {
  private static final @NotNull String rootCommandName = "guild";

  private static Builder<CommandSourceStack> createCommand(
      PaperCommandManager<CommandSourceStack> manager) {
    return manager
        .commandBuilder(GuildCommand.rootCommandName)
        .literal("create")
        .required("name", StringParser.greedyStringParser())
        .handler(
            context -> {
              // TODO: Create Guild.
            });
  }

  private static Builder<CommandSourceStack> disbandCommand(
      PaperCommandManager<CommandSourceStack> manager) {
    return manager
        .commandBuilder(GuildCommand.rootCommandName)
        .literal("disband")
        .handler(
            context -> {
              // TODO: Disband Guild.
            });
  }

  private static Builder<CommandSourceStack> sendInviteCommand(
      PaperCommandManager<CommandSourceStack> manager) {
    return manager
        .commandBuilder(GuildCommand.rootCommandName)
        .literal("invite")
        .literal("send")
        .required("player", StringParser.stringParser())
        .handler(
            context -> {
              // TODO: Send Guild invite.
            });
  }

  private static Builder<CommandSourceStack> inviteRemovalCommand(
      PaperCommandManager<CommandSourceStack> manager) {
    return manager
        .commandBuilder(GuildCommand.rootCommandName)
        .literal("invite")
        .literal("remove")
        .required("player", StringParser.stringParser())
        .handler(
            context -> {
              // TODO: Remove Guild invite.
            });
  }

  private static Builder<CommandSourceStack> kickCommand(
      PaperCommandManager<CommandSourceStack> manager) {
    return manager
        .commandBuilder(GuildCommand.rootCommandName)
        .literal("kick")
        .required("player", StringParser.stringParser())
        .handler(
            context -> {
              // TODO: Guild kick.
            });
  }

  public static Set<Builder<CommandSourceStack>> build(
      PaperCommandManager<CommandSourceStack> manager) {
    return Set.of(
        GuildCommand.createCommand(manager),
        GuildCommand.disbandCommand(manager),
        GuildCommand.sendInviteCommand(manager),
        GuildCommand.inviteRemovalCommand(manager),
        GuildCommand.kickCommand(manager));
  }
}
