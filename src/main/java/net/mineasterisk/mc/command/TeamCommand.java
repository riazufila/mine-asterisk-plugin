package net.mineasterisk.mc.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.command.parser.PlayerExceptSelf;
import net.mineasterisk.mc.constant.PermissionConstant;
import net.mineasterisk.mc.exception.EntityException;
import net.mineasterisk.mc.service.team.TeamService;
import net.mineasterisk.mc.util.ExceptionUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class TeamCommand implements net.mineasterisk.mc.command.Command {
  @Override
  public @NotNull LiteralCommandNode<@NotNull CommandSourceStack> build() {
    return Commands.literal("team")
        .then(Commands.literal("info").requires(this::canRead).executes(this::info))
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
                .then(Commands.argument("player", new PlayerExceptSelf()).executes(this::kick))
                .requires(this::canUpdate))
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

  @SuppressWarnings("SameReturnValue")
  private int info(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    context.getSource().getSender().sendPlainMessage("TODO: Team info command");

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int create(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player player)) {
        throw new EntityException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();
      final String NAME = context.getArgument("name", String.class);

      teamService.create(player, NAME);

      player.sendMessage(
          Component.text(String.format("Team %s created", NAME)).color(NamedTextColor.GREEN));

      PluginUtil.getLogger()
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
        throw new EntityException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();

      teamService.disband(player);

      player.sendMessage(Component.text("Team disbanded").color(NamedTextColor.GREEN));
      PluginUtil.getLogger()
          .info(
              String.format(
                  "Player %s (%s) disbanded its Team", player.getName(), player.getUniqueId()));
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "disband Team");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int sendInvite(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player inviter)) {
        throw new EntityException(
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

      PluginUtil.getLogger()
          .info(
              String.format(
                  "Player %s (%s) invited Player %s (%s) to its Team",
                  inviter.getName(),
                  inviter.getUniqueId(),
                  invitee.getName(),
                  invitee.getUniqueId()));
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "send Team invitation");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int acceptInvite(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player invitee)) {
        throw new EntityException(
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

      PluginUtil.getLogger()
          .info(
              String.format(
                  "Player %s (%s) accepted Team invitation from Player %s (%s)",
                  invitee.getName(),
                  invitee.getUniqueId(),
                  inviter.getName(),
                  inviter.getUniqueId()));
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "accept Team invitation");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int removeInvite(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player inviter)) {
        throw new EntityException(
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

      PluginUtil.getLogger()
          .info(
              String.format(
                  "Player %s (%s) removed Team invitation for Player %s (%s)",
                  inviter.getName(),
                  inviter.getUniqueId(),
                  invitee.getName(),
                  invitee.getUniqueId()));
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "remove Team invitation");
    }

    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("SameReturnValue")
  private int kick(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player kicker)) {
        throw new EntityException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final TeamService teamService = new TeamService();
      final Player kicked =
          context
              .getArgument("player", PlayerSelectorArgumentResolver.class)
              .resolve(source)
              .getFirst();

      teamService.kick(kicker, kicked);

      kicker.sendMessage(
          Component.text(String.format("Kicked %s from the Team", kicked.getName()))
              .color(NamedTextColor.GREEN));

      kicked.sendMessage(
          Component.text(String.format("%s kicked you from the Team", kicker.getName()))
              .color(NamedTextColor.RED));

      PluginUtil.getLogger()
          .info(
              String.format(
                  "Player %s (%s) kicked Player %s (%s) from the Team",
                  kicker.getName(), kicker.getUniqueId(), kicked.getName(), kicked.getUniqueId()));
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "Team kick");
    }

    return Command.SINGLE_SUCCESS;
  }
}
