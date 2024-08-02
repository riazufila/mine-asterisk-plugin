package net.mineasterisk.mc.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Set;
import org.incendo.cloud.Command.Builder;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

public class GuildCommand {
  private static final @NotNull String rootCommandName = "guild";

  public static @NotNull Set<@NotNull Builder<@NotNull CommandSourceStack>> build(
      final @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager) {
    return Set.of(
        GuildCommand.createCommand(manager),
        GuildCommand.disbandCommand(manager),
        GuildCommand.sendInviteCommand(manager),
        GuildCommand.inviteRemovalCommand(manager),
        GuildCommand.kickCommand(manager));
  }

  private static @NotNull Builder<@NotNull CommandSourceStack> createCommand(
      final @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager) {
    return manager
        .commandBuilder(GuildCommand.rootCommandName)
        .literal("create")
        .required("name", StringParser.greedyStringParser())
        .handler(
            context -> {
              // TODO: Create Guild.
            });
  }

  private static @NotNull Builder<@NotNull CommandSourceStack> disbandCommand(
      final @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager) {
    return manager
        .commandBuilder(GuildCommand.rootCommandName)
        .literal("disband")
        .handler(
            context -> {
              // TODO: Disband Guild.
            });
  }

  private static @NotNull Builder<@NotNull CommandSourceStack> sendInviteCommand(
      final @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager) {
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

  private static @NotNull Builder<@NotNull CommandSourceStack> inviteRemovalCommand(
      final @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager) {
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

  private static @NotNull Builder<@NotNull CommandSourceStack> kickCommand(
      final @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager) {
    return manager
        .commandBuilder(GuildCommand.rootCommandName)
        .literal("kick")
        .required("player", StringParser.stringParser())
        .handler(
            context -> {
              // TODO: Guild kick.
            });
  }
}
