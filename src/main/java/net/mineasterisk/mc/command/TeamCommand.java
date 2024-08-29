package net.mineasterisk.mc.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.SelectorArgumentResolver;
import java.util.List;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.command.parser.OfflinePlayerInTeamExceptSelf;
import net.mineasterisk.mc.command.parser.PlayerExceptSelf;
import net.mineasterisk.mc.constant.PermissionConstant;
import net.mineasterisk.mc.service.team.TeamMember;
import net.mineasterisk.mc.service.team.TeamService;
import net.mineasterisk.mc.util.ExceptionUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class TeamCommand implements net.mineasterisk.mc.command.Command {
  @Override
  public @NotNull LiteralCommandNode<@NotNull CommandSourceStack> build() {
    return Commands.literal("team")
        .then(Commands.literal("information").requires(this::canRead).executes(this::information))
        .then(
            Commands.literal("create")
                .then(
                    Commands.argument("name", StringArgumentType.greedyString())
                        .executes(this::create))
                .requires(this::canCreate))
        .then(Commands.literal("disband").requires(this::canDelete).executes(this::disband))
        .then(
            Commands.literal("invite")
                .then(
                    Commands.literal("send")
                        .then(
                            Commands.argument("player", new PlayerExceptSelf())
                                .executes(this::sendInvite))
                        .requires(this::canUpdate))
                .then(
                    Commands.literal("accept")
                        .then(
                            Commands.argument("player", new PlayerExceptSelf())
                                .executes(this::acceptInvite))
                        .requires(this::canCreate))
                .then(
                    Commands.literal("remove")
                        .then(
                            Commands.argument("player", new PlayerExceptSelf())
                                .executes(this::removeInvite))
                        .requires(this::canUpdate))
                .requires(source -> this.canUpdate(source) || this.canCreate(source)))
        .then(
            Commands.literal("kick")
                .then(
                    Commands.argument("player", new OfflinePlayerInTeamExceptSelf())
                        .executes(this::kick))
                .requires(this::canUpdate))
        .then(Commands.literal("leave").requires(this::canLeave).executes(this::leave))
        .requires(
            source ->
                this.canCreate(source)
                    || this.canRead(source)
                    || this.canUpdate(source)
                    || this.canDelete(source))
        .build();
  }

  private boolean canCreate(final @NotNull CommandSourceStack source) {
    CommandSender sender = source.getSender();

    return !(sender.hasPermission(PermissionConstant.TEAM_LEADER.toString())
        || sender.hasPermission(PermissionConstant.TEAM_MEMBER.toString()));
  }

  private boolean canRead(final @NotNull CommandSourceStack source) {
    CommandSender sender = source.getSender();

    return sender.hasPermission(PermissionConstant.TEAM_LEADER.toString())
        || sender.hasPermission(PermissionConstant.TEAM_MEMBER.toString());
  }

  private boolean canUpdate(final @NotNull CommandSourceStack source) {
    return source.getSender().hasPermission(PermissionConstant.TEAM_LEADER.toString());
  }

  private boolean canDelete(final @NotNull CommandSourceStack source) {
    return source.getSender().hasPermission(PermissionConstant.TEAM_LEADER.toString());
  }

  private boolean canLeave(final @NotNull CommandSourceStack source) {
    return !source.getSender().hasPermission(PermissionConstant.TEAM_LEADER.toString())
        && source.getSender().hasPermission(PermissionConstant.TEAM_MEMBER.toString());
  }

  @SuppressWarnings("SameReturnValue")
  private int information(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player player)) {
        throw new IllegalStateException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();
      final List<TeamMember> members = teamService.getMembers(player);
      final String leaderName =
          members.stream()
              .filter(TeamMember::isLeader)
              .findFirst()
              .map(TeamMember::name)
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          String.format(
                              "Encountered error while Player %s (%s) is getting Team information",
                              player.getName(), player.getUniqueId())));

      final List<String> membersNames =
          members.stream().filter(member -> !member.isLeader()).map(TeamMember::name).toList();

      final TextComponent leaderComponent = Component.text(String.format("Leader: %s", leaderName));
      final TextComponent membersComponent =
          Component.text(
              String.format(
                  "Member(s): %s",
                  !membersNames.isEmpty() ? String.join(", ", membersNames) : '-'));

      player.sendMessage(
          Component.join(JoinConfiguration.newlines(), leaderComponent, membersComponent)
              .color(NamedTextColor.YELLOW));

      MineAsterisk.getInstance()
          .getLogger()
          .info(
              String.format(
                  "Player %s (%s) retrieved its Team information",
                  player.getName(), player.getUniqueId()));
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "Team information");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int create(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player player)) {
        throw new IllegalStateException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();
      final String NAME = context.getArgument("name", String.class);

      teamService.create(player, NAME);

      player.sendMessage(
          Component.text(String.format("Team %s created", NAME)).color(NamedTextColor.GREEN));

      MineAsterisk.getInstance()
          .getLogger()
          .info(
              String.format(
                  "Player %s (%s) created Team %s", player.getName(), player.getUniqueId(), NAME));
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "create Team");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int disband(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player player)) {
        throw new IllegalStateException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();
      final List<Player> members = teamService.disband(player);
      final Component component = Component.text("Team disbanded");

      player.sendMessage(component.color(NamedTextColor.GREEN));
      Audience.audience(members).sendMessage(component.color(NamedTextColor.YELLOW));

      MineAsterisk.getInstance()
          .getLogger()
          .info(
              String.format(
                  "Player %s (%s) disbanded its Team", player.getName(), player.getUniqueId()));
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "disband Team");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int sendInvite(final @NotNull CommandContext<@NotNull CommandSourceStack> context)
      throws CommandSyntaxException {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player inviter)) {
        throw new IllegalStateException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();
      final Player invitee =
          context
              .getArgument("player", PlayerSelectorArgumentResolver.class)
              .resolve(source)
              .getFirst();

      teamService.sendInvitation(inviter, invitee);

      inviter.sendMessage(
          Component.text(String.format("Sent Team invitation to %s", invitee.getName()))
              .color(NamedTextColor.YELLOW));

      invitee.sendMessage(
          Component.text(String.format("Received Team invitation from %s", inviter.getName()))
              .color(NamedTextColor.YELLOW));

      MineAsterisk.getInstance()
          .getLogger()
          .info(
              String.format(
                  "Player %s (%s) invited Player %s (%s) to its Team",
                  inviter.getName(),
                  inviter.getUniqueId(),
                  invitee.getName(),
                  invitee.getUniqueId()));
    } catch (CommandSyntaxException exception) {
      throw exception;
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "send Team invitation");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int acceptInvite(final @NotNull CommandContext<@NotNull CommandSourceStack> context)
      throws CommandSyntaxException {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player invitee)) {
        throw new IllegalStateException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();
      final Player inviter =
          context
              .getArgument("player", PlayerSelectorArgumentResolver.class)
              .resolve(source)
              .getFirst();

      teamService.acceptInvitation(inviter, invitee);

      invitee.sendMessage(
          Component.text(String.format("Accepted Team invitation from %s", inviter.getName()))
              .color(NamedTextColor.GREEN));

      inviter.sendMessage(
          Component.text(String.format("Team invitation to %s is accepted", invitee.getName()))
              .color(NamedTextColor.GREEN));

      MineAsterisk.getInstance()
          .getLogger()
          .info(
              String.format(
                  "Player %s (%s) accepted Team invitation from Player %s (%s)",
                  invitee.getName(),
                  invitee.getUniqueId(),
                  inviter.getName(),
                  inviter.getUniqueId()));
    } catch (CommandSyntaxException exception) {
      throw exception;
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "accept Team invitation");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int removeInvite(final @NotNull CommandContext<@NotNull CommandSourceStack> context)
      throws CommandSyntaxException {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player inviter)) {
        throw new IllegalStateException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();
      final Player invitee =
          context
              .getArgument("player", PlayerSelectorArgumentResolver.class)
              .resolve(source)
              .getFirst();

      teamService.removeInvitation(inviter, invitee);

      inviter.sendMessage(
          Component.text(String.format("Removed Team invitation to %s", invitee.getName()))
              .color(NamedTextColor.GREEN));

      invitee.sendMessage(
          Component.text(String.format("Team invitation from %s is removed", inviter.getName()))
              .color(NamedTextColor.RED));

      MineAsterisk.getInstance()
          .getLogger()
          .info(
              String.format(
                  "Player %s (%s) removed Team invitation for Player %s (%s)",
                  inviter.getName(),
                  inviter.getUniqueId(),
                  invitee.getName(),
                  invitee.getUniqueId()));
    } catch (CommandSyntaxException exception) {
      throw exception;
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "remove Team invitation");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int kick(final @NotNull CommandContext<@NotNull CommandSourceStack> context)
      throws CommandSyntaxException {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player kicker)) {
        throw new IllegalStateException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();

      @SuppressWarnings("unchecked")
      final OfflinePlayer offlineKicked =
          context
              .getArgument(
                  "player",
                  (Class<SelectorArgumentResolver<OfflinePlayer>>)
                      (Class<?>) SelectorArgumentResolver.class)
              .resolve(source);

      teamService.kick(kicker, offlineKicked);

      kicker.sendMessage(
          Component.text(String.format("Kicked %s from the Team", offlineKicked.getName()))
              .color(NamedTextColor.GREEN));

      if (offlineKicked.getPlayer() != null) {
        offlineKicked
            .getPlayer()
            .sendMessage(
                Component.text(String.format("%s kicked you from the Team", kicker.getName()))
                    .color(NamedTextColor.RED));
      }

      MineAsterisk.getInstance()
          .getLogger()
          .info(
              String.format(
                  "Player %s (%s) kicked Player %s (%s) from the Team",
                  kicker.getName(),
                  kicker.getUniqueId(),
                  offlineKicked.getName(),
                  offlineKicked.getUniqueId()));
    } catch (CommandSyntaxException exception) {
      throw exception;
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "Team kick");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int leave(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player player)) {
        throw new IllegalStateException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();
      final Team team = teamService.leave(player);

      player.sendMessage(Component.text("Left Team").color(NamedTextColor.GREEN));

      team.sendMessage(
          Component.text(String.format("%s left the Team", player.getName()))
              .color(NamedTextColor.YELLOW));

      MineAsterisk.getInstance()
          .getLogger()
          .info(
              String.format(
                  "Player %s (%s) left its Team", player.getName(), player.getUniqueId()));
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "leave Team");
    }

    return Command.SINGLE_SUCCESS;
  }
}
