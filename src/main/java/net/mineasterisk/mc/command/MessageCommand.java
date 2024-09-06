package net.mineasterisk.mc.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.mineasterisk.mc.command.parser.PlayerExceptSelf;
import net.mineasterisk.mc.service.player.PlayerService;
import net.mineasterisk.mc.util.ExceptionUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class MessageCommand implements net.mineasterisk.mc.command.Command {
  @Override
  public @NotNull LiteralCommandNode<@NotNull CommandSourceStack> build() {
    return Commands.literal("message")
        .then(
            Commands.argument("player", new PlayerExceptSelf())
                .then(
                    Commands.argument("message", StringArgumentType.greedyString())
                        .executes(this::message)))
        .build();
  }

  @SuppressWarnings("SameReturnValue")
  private int message(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();

    try {
      if (!(sender instanceof Player player)) {
        throw new IllegalStateException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final String MESSAGE = context.getArgument("message", String.class);
      final PlayerService playerService = new PlayerService(player);
      final Player recipient =
          context
              .getArgument("player", PlayerSelectorArgumentResolver.class)
              .resolve(source)
              .getFirst();

      playerService.message(recipient, MESSAGE);
    } catch (Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "message Player");
    }

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }
}
