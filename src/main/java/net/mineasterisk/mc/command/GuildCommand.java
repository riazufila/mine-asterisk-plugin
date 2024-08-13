package net.mineasterisk.mc.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.service.GuildService;
import net.mineasterisk.mc.util.HibernateUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.incendo.cloud.Command.Builder;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.jetbrains.annotations.NotNull;

public class GuildCommand extends Command {
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
        .handler(context -> this.create(context.sender().getSender(), context.get(nameArgument)));
  }

  @SuppressWarnings("TryFinallyCanBeTryWithResources")
  private void create(final @NotNull CommandSender sender, final @NotNull String name) {
    final StatelessSession statelessSession = HibernateUtil.getStatelessSession();
    final Transaction transaction = statelessSession.beginTransaction();

    try {
      final GuildService guildService = new GuildService(statelessSession);

      if (!(sender instanceof Player performedBy)) {
        throw new RuntimeException(String.format("Sender %s isn't a Player", sender.getName()));
      }

      guildService.create(performedBy, name).join();
      transaction.commit();

      PluginUtil.getLogger()
          .info(String.format("Player %s created Guild %s", performedBy.getUniqueId(), name));

      performedBy.sendMessage(Component.text("Created Guild").color(NamedTextColor.GREEN));
    } catch (Exception exception) {
      transaction.rollback();
      this.exceptionHandler(exception, sender, "create Guild");
    } finally {
      statelessSession.close();
    }
  }

  private @NotNull Builder<@NotNull CommandSourceStack> disbandCommand() {
    return manager
        .commandBuilder(this.rootCommandName)
        .literal("disband")
        .handler(context -> this.disband(context.sender().getSender()));
  }

  @SuppressWarnings("TryFinallyCanBeTryWithResources")
  private void disband(final @NotNull CommandSender sender) {
    final StatelessSession statelessSession = HibernateUtil.getStatelessSession();
    final Transaction transaction = statelessSession.beginTransaction();

    try {
      final GuildService guildService = new GuildService(statelessSession);

      if (!(sender instanceof Player performedBy)) {
        throw new RuntimeException(String.format("Sender %s isn't a Player", sender.getName()));
      }

      guildService.disband(performedBy).join();
      transaction.commit();

      PluginUtil.getLogger()
          .info(String.format("Player %s disbanded its Guild", performedBy.getUniqueId()));

      performedBy.sendMessage(Component.text("Disbanded Guild").color(NamedTextColor.GREEN));
    } catch (Exception exception) {
      transaction.rollback();
      this.exceptionHandler(exception, sender, "disband Guild");
    } finally {
      statelessSession.close();
    }
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
