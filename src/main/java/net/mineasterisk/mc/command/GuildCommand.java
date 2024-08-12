package net.mineasterisk.mc.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.constant.attribute.GuildAttribute;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.forcefetch.GuildForceFetch;
import net.mineasterisk.mc.constant.forcefetch.PlayerForceFetch;
import net.mineasterisk.mc.constant.status.GuildStatus;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.GuildRepository;
import net.mineasterisk.mc.repository.PlayerRepository;
import net.mineasterisk.mc.service.GuildService;
import net.mineasterisk.mc.service.PlayerService;
import net.mineasterisk.mc.util.HibernateUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.incendo.cloud.Command.Builder;
import org.incendo.cloud.context.CommandContext;
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
        .handler(context -> this.create(context, nameArgument));
  }

  private void create(
      final @NotNull CommandContext<@NotNull CommandSourceStack> context,
      final @NotNull String nameArgument) {
    final StatelessSession statelessSession = HibernateUtil.getStatelessSession();
    final Transaction transaction = statelessSession.beginTransaction();

    try (statelessSession) {
      final CommandSender sender = context.sender().getSender();
      final String name = context.get(nameArgument);
      final Instant now = Instant.now();
      final PlayerRepository playerRepository = new PlayerRepository(statelessSession);
      final PlayerService playerService = new PlayerService(statelessSession);
      final GuildService guildService = new GuildService(statelessSession);

      if (!(sender instanceof Player performedBy)) {
        throw new RuntimeException(String.format("Sender %s is not a Player", sender.getName()));
      }

      final PlayerModel player =
          playerRepository.get(PlayerAttribute.UUID, performedBy.getUniqueId()).join();

      if (player == null) {
        throw new ValidationException(
            "Encountered error",
            String.format("Player %s is not initialized", performedBy.getUniqueId()));
      }

      final GuildModel guild =
          new GuildModel(
              now, player, null, null, name, player, GuildStatus.ACTIVE, Collections.emptySet());

      guildService.add(performedBy, guild).join();
      player.setGuild(guild);
      playerService.update(performedBy, player).join();

      final Scoreboard scoreboard = PluginUtil.getMainScoreboard();
      final Team team = scoreboard.registerNewTeam(name);

      team.addEntity(performedBy);
      team.displayName(Component.text(name));
      team.prefix(
          Component.textOfChildren(
              Component.text(name).color(NamedTextColor.GRAY),
              Component.text('.').color(NamedTextColor.GRAY)));

      transaction.commit();

      PluginUtil.getLogger()
          .info(
              String.format(
                  "Player %s created Guild %s", performedBy.getUniqueId(), guild.getName()));

      performedBy.sendMessage(Component.text("Created Guild").color(NamedTextColor.GREEN));
    } catch (Exception exception) {
      transaction.rollback();
      this.exceptionHandler(exception, context.sender().getSender(), "create Guild");
    }
  }

  private @NotNull Builder<@NotNull CommandSourceStack> disbandCommand() {
    return manager.commandBuilder(this.rootCommandName).literal("disband").handler(this::disband);
  }

  private void disband(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final StatelessSession statelessSession = HibernateUtil.getStatelessSession();
    final Transaction transaction = statelessSession.beginTransaction();

    try (statelessSession) {
      final CommandSender sender = context.sender().getSender();
      final Instant now = Instant.now();
      final PlayerRepository playerRepository = new PlayerRepository(statelessSession);
      final GuildRepository guildRepository = new GuildRepository(statelessSession);
      final PlayerService playerService = new PlayerService(statelessSession);
      final GuildService guildService = new GuildService(statelessSession);

      if (!(sender instanceof Player performedBy)) {
        throw new RuntimeException(String.format("Sender %s is not a Player", sender.getName()));
      }

      final PlayerModel player =
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

      final GuildModel guild =
          guildRepository
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

      for (PlayerModel playerInGuild : guild.getPlayers()) {
        playerInGuild.setGuild(null);
        playerService.update(performedBy, playerInGuild);
      }

      guildService.update(performedBy, guild).join();

      final Scoreboard scoreboard = PluginUtil.getMainScoreboard();
      final Team team = scoreboard.getTeam(guild.getName());

      if (team == null) {
        throw new ValidationException(
            "Encountered error",
            String.format(
                "Guild %s doesn't have a Team %s applied", guild.getName(), guild.getName()));
      }

      team.unregister();
      transaction.commit();

      PluginUtil.getLogger()
          .info(
              String.format(
                  "Player %s disbanded Guild %s", performedBy.getUniqueId(), guild.getName()));

      performedBy.sendMessage(Component.text("Disbanded Guild").color(NamedTextColor.GREEN));
    } catch (Exception exception) {
      transaction.rollback();
      this.exceptionHandler(exception, context.sender().getSender(), "disband Guild");
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
