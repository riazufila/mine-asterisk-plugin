package net.mineasterisk.mc.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Set;
import org.incendo.cloud.Command.Builder;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

public class GuildCommand {
  private final @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager;
  private final @NotNull String rootCommandName = "guild";

  public GuildCommand(final @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager) {
    this.manager = manager;
  }

  public @NotNull Set<@NotNull Builder<@NotNull CommandSourceStack>> build() {
    return Set.of(
        this.createCommand(),
        this.disbandCommand(),
        this.sendInviteCommand(),
        this.inviteRemovalCommand(),
        this.kickCommand());
  }

  private @NotNull Builder<@NotNull CommandSourceStack> createCommand() {
    final String nameArgument = "name";

    return manager
        .commandBuilder(this.rootCommandName)
        .literal("create")
        .required(nameArgument, StringParser.greedyStringParser())
        .handler(
            context -> {
              // TODO: Create Guild.
            });
  }

  private @NotNull Builder<@NotNull CommandSourceStack> disbandCommand() {
    return manager
        .commandBuilder(this.rootCommandName)
        .literal("disband")
        .handler(
            context -> {
              // TODO: Disband Guild.
            });
  }

  private @NotNull Builder<@NotNull CommandSourceStack> sendInviteCommand() {
    final String playerArgument = "player";

    return manager
        .commandBuilder(this.rootCommandName)
        .literal("invite")
        .literal("send")
        .required(playerArgument, StringParser.stringParser())
        .handler(
            context -> {
              // TODO: Send Guild invite.
            });
  }

  private @NotNull Builder<@NotNull CommandSourceStack> inviteRemovalCommand() {
    final String playerArgument = "player";

    return manager
        .commandBuilder(this.rootCommandName)
        .literal("invite")
        .literal("remove")
        .required(playerArgument, StringParser.stringParser())
        .handler(
            context -> {
              // TODO: Remove Guild invite.
            });
  }

  private @NotNull Builder<@NotNull CommandSourceStack> kickCommand() {
    final String playerArgument = "player";

    return manager
        .commandBuilder(this.rootCommandName)
        .literal("kick")
        .required(playerArgument, StringParser.stringParser())
        .handler(
            context -> {
              // TODO: Guild kick.
            });
  }
}
