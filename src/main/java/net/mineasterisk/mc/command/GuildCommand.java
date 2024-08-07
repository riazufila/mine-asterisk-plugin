package net.mineasterisk.mc.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.status.GuildStatus;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.service.GuildService;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
              final CommandSender sender = context.sender().getSender();
              final String name = context.get(nameArgument);
              final Instant now = Instant.now();

              if (!(sender instanceof Player performedBy)) {
                PluginUtil.getLogger()
                    .warning(
                        "Unable to execute Guild create command: Only Player is allowed to execute this command");

                return;
              }

              PlayerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId())
                  .thenAccept(
                      player -> {
                        if (player == null) {
                          return;
                        }

                        final GuildModel guild =
                            new GuildModel(
                                now,
                                player,
                                name,
                                player,
                                GuildStatus.ACTIVE,
                                Collections.emptySet());

                        GuildService.add(performedBy, guild)
                            .thenRun(
                                () -> {
                                  guild.setPlayers(Set.of(player));
                                  GuildService.update(performedBy, guild)
                                      .thenRun(
                                          () -> {
                                            PluginUtil.getLogger()
                                                .info(
                                                    String.format(
                                                        "Player %s created Guild %s",
                                                        performedBy.getUniqueId(), name));
                                            sender.sendMessage(
                                                Component.text("Created Guild")
                                                    .color(NamedTextColor.GREEN));
                                          });
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
