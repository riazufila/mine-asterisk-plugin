package net.mineasterisk.mc.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.status.GuildStatus;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.service.GuildService;
import net.mineasterisk.mc.util.LoggerUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command.Builder;
import org.incendo.cloud.context.CommandContext;
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
        .handler(context -> this.create(context, nameArgument));
  }

  private void create(
      final @NotNull CommandContext<@NotNull CommandSourceStack> context,
      final @NotNull String nameArgument) {
    final CommandSender sender = context.sender().getSender();
    final String name = context.get(nameArgument);
    final Instant now = Instant.now();

    if (!(sender instanceof Player performedBy)) {
      return;
    }

    final LoggerUtil logger =
        new LoggerUtil(
            performedBy, "Executed Guild create command", "Unable to execute Guild create command");

    PlayerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId())
        .thenAccept(
            player -> {
              if (player == null) {
                logger.warn(
                    "Player doesn't exist",
                    String.format("Player %s is not initialized", performedBy.getUniqueId()));

                return;
              }

              final GuildModel guild =
                  new GuildModel(
                      now, player, name, player, GuildStatus.ACTIVE, Collections.emptySet());

              GuildService.add(performedBy, guild)
                  .thenAccept(
                      (guildAdded) -> {
                        if (!guildAdded) {
                          return;
                        }

                        guild.setPlayers(Set.of(player));

                        GuildService.update(performedBy, guild)
                            .thenAccept(
                                guildUpdated -> {
                                  if (!guildUpdated) {
                                    return;
                                  }

                                  logger.success(
                                      "Created Guild",
                                      String.format(
                                          "Player %s created Guild %s",
                                          performedBy.getUniqueId(), guild.getName()));
                                });
                      });
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
