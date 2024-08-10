package net.mineasterisk.mc.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.constant.attribute.GuildAttribute;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.forcefetch.GuildForceFetch;
import net.mineasterisk.mc.constant.forcefetch.PlayerForceFetch;
import net.mineasterisk.mc.constant.status.GuildStatus;
import net.mineasterisk.mc.exception.MissingEntityException;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.GuildRepository;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.service.GuildService;
import net.mineasterisk.mc.util.HibernateUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.hibernate.Session;
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
    Session session = HibernateUtil.getSessionFactory().openSession();
    session.getTransaction().begin();

    try {
      final CommandSender sender = context.sender().getSender();
      final String name = context.get(nameArgument);
      final Instant now = Instant.now();

      if (!(sender instanceof Player performedBy)) {
        throw new RuntimeException(String.format("Sender %s is not a Player", sender.getName()));
      }

      PlayerRepository playerRepository = new PlayerRepository(session);
      PlayerModel player =
          playerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId()).join();

      if (player == null) {
        throw new ValidationException(
            "Encountered error",
            String.format("Player %s is not initialized", performedBy.getUniqueId()));
      }

      GuildService guildService = new GuildService(session);
      final GuildModel guild =
          new GuildModel(
              now, player, null, null, name, player, GuildStatus.ACTIVE, new HashSet<>());

      guildService.add(performedBy, guild).join();
      guild.addPlayer(player);
      guildService.update(performedBy, guild).join();

      Scoreboard scoreboard = PluginUtil.getMainScoreboard();
      Team team = scoreboard.registerNewTeam(name);

      team.addEntity(performedBy);
      team.displayName(Component.text(name));
      team.prefix(
          Component.textOfChildren(
              Component.text(name).color(NamedTextColor.GRAY),
              Component.text('.').color(NamedTextColor.GRAY)));

      session.getTransaction().commit();

      PluginUtil.getLogger()
          .info(
              String.format(
                  "Player %s created Guild %s", performedBy.getUniqueId(), guild.getName()));

      performedBy.sendMessage(Component.text("Created Guild").color(NamedTextColor.GREEN));
    } catch (CompletionException exception) {
      final CommandSender sender = context.sender().getSender();
      String message = "Encountered error";
      final Throwable cause = exception.getCause();

      if (cause instanceof MissingEntityException) {
        message = ((MissingEntityException) cause).getClientMessage();
      } else if (cause instanceof ValidationException) {
        message = ((ValidationException) cause).getClientMessage();
      }

      session.getTransaction().rollback();

      PluginUtil.getLogger()
          .severe(String.format("Unable to execute Guild create command: %s", exception));

      if (sender instanceof Player performedBy) {
        performedBy.sendMessage(Component.text(message).color(NamedTextColor.RED));
      }
    } catch (Exception exception) {
      final CommandSender sender = context.sender().getSender();

      session.getTransaction().rollback();

      PluginUtil.getLogger()
          .severe(String.format("Unable to execute Guild create command: %s", exception));

      if (sender instanceof Player performedBy) {
        performedBy.sendMessage(Component.text("Encountered error").color(NamedTextColor.RED));
      }
    } finally {
      session.close();
    }
  }

  private @NotNull Builder<@NotNull CommandSourceStack> disbandCommand() {
    return manager.commandBuilder(this.rootCommandName).literal("disband").handler(this::disband);
  }

  private void disband(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    session.getTransaction().begin();

    try {
      final CommandSender sender = context.sender().getSender();
      final Instant now = Instant.now();

      if (!(sender instanceof Player performedBy)) {
        throw new RuntimeException(String.format("Sender %s is not a Player", sender.getName()));
      }

      PlayerRepository playerRepository = new PlayerRepository(session);
      PlayerModel player =
          playerRepository
              .get(PlayerAttribute.UUID, performedBy.getUniqueId(), Set.of(PlayerForceFetch.GUILD))
              .join();

      if (player == null) {
        throw new ValidationException(
            "Encountered error",
            String.format("Player %s is not initialized", performedBy.getUniqueId()));
      }

      if (player.getGuild() == null) {
        throw new ValidationException(
            "Doesn't have a Guild",
            String.format("Player %s doesn't have a Guild", performedBy.getUniqueId()));
      }

      GuildModel guild =
          new GuildRepository(session)
              .get(GuildAttribute.ID, player.getGuild().getId(), Set.of(GuildForceFetch.PLAYERS))
              .join();

      if (guild == null) {
        throw new ValidationException(
            "Doesn't have a Guild",
            String.format("Player %s doesn't have a Guild", performedBy.getUniqueId()));
      }

      guild.setUpdatedAt(now);
      guild.setUpdatedBy(player);
      guild.setStatus(GuildStatus.INACTIVE);
      guild.clearPlayers();

      GuildService guildService = new GuildService(session);

      guildService.update(performedBy, guild).join();

      Scoreboard scoreboard = PluginUtil.getMainScoreboard();
      Team team = scoreboard.getTeam(guild.getName());

      if (team == null) {
        throw new ValidationException(
            "Encountered error",
            String.format(
                "Guild %s doesn't have a Team %s applied", guild.getName(), guild.getName()));
      }

      team.unregister();
      session.getTransaction().commit();

      PluginUtil.getLogger()
          .info(
              String.format(
                  "Player %s disbanded Guild %s", performedBy.getUniqueId(), guild.getName()));

      performedBy.sendMessage(Component.text("Disbanded Guild").color(NamedTextColor.GREEN));
    } catch (CompletionException exception) {
      final CommandSender sender = context.sender().getSender();
      String message = "Encountered error";
      final Throwable cause = exception.getCause();

      if (cause instanceof MissingEntityException) {
        message = ((MissingEntityException) cause).getClientMessage();
      } else if (cause instanceof ValidationException) {
        message = ((ValidationException) cause).getClientMessage();
      }

      session.getTransaction().rollback();

      PluginUtil.getLogger()
          .severe(String.format("Unable to execute Guild disband command: %s", exception));

      if (sender instanceof Player performedBy) {
        performedBy.sendMessage(Component.text(message).color(NamedTextColor.RED));
      }
    } catch (Exception exception) {
      final CommandSender sender = context.sender().getSender();

      session.getTransaction().rollback();

      PluginUtil.getLogger()
          .severe(String.format("Unable to execute Guild disband command: %s", exception));

      if (sender instanceof Player performedBy) {
        performedBy.sendMessage(Component.text("Encountered error").color(NamedTextColor.RED));
      }
    } finally {
      session.close();
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
