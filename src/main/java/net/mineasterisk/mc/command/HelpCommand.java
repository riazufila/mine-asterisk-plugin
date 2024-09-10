package net.mineasterisk.mc.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.util.CommandUtil;
import net.mineasterisk.mc.util.ExceptionUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class HelpCommand implements net.mineasterisk.mc.command.Command {
  private final @NotNull CommandDispatcher<@NotNull CommandSourceStack> dispatcher;

  public HelpCommand(final @NotNull CommandDispatcher<@NotNull CommandSourceStack> dispatcher) {
    this.dispatcher = dispatcher;
  }

  @Override
  public @NotNull LiteralCommandNode<@NotNull CommandSourceStack> build() {
    return Commands.literal("help")
        .then(
            Commands.argument("command", StringArgumentType.greedyString())
                .suggests(this::filteredHelpSuggestions)
                .executes(this::filteredHelp))
        .executes(this::generalHelp)
        .build();
  }

  @SuppressWarnings("SameReturnValue")
  private int generalHelp(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();
    final Entity executor = source.getExecutor();

    try {
      final Player player = CommandUtil.getPlayer(sender, executor);
      final List<String> usages =
          this.getAllUsagesExceptHelp(this.dispatcher.getRoot(), source, null, true);

      if (!usages.isEmpty()) {
        player.sendMessage(Component.text(String.join("\n", usages)));
        MineAsterisk.getInstance()
            .getLogger()
            .info(
                String.format(
                    "Player %s (%s) retrieved general help",
                    player.getName(), player.getUniqueId()));
      } else {
        player.sendMessage(Component.text("No commands to display").color(NamedTextColor.RED));
        MineAsterisk.getInstance()
            .getLogger()
            .info(
                String.format(
                    "Player %s (%s) retrieved general help but there's nothing to display",
                    player.getName(), player.getUniqueId()));
      }
    } catch (final Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "general help");
    }

    return Command.SINGLE_SUCCESS;
  }

  private @NotNull CompletableFuture<@NotNull Suggestions> filteredHelpSuggestions(
      final @NotNull CommandContext<@NotNull CommandSourceStack> context,
      final @NotNull SuggestionsBuilder builder) {
    final CommandSourceStack source = context.getSource();
    final List<String> usages =
        this.getAllUsagesExceptHelp(this.dispatcher.getRoot(), source, null, false);

    usages.stream()
        .filter(usage -> usage.startsWith(builder.getRemaining()))
        .forEach(builder::suggest);

    return builder.buildFuture();
  }

  @SuppressWarnings("SameReturnValue")
  private int filteredHelp(final @NotNull CommandContext<@NotNull CommandSourceStack> context) {
    final CommandSourceStack source = context.getSource();
    final CommandSender sender = source.getSender();
    final Entity executor = source.getExecutor();

    try {
      final Player player = CommandUtil.getPlayer(sender, executor);
      final String COMMAND = context.getArgument("command", String.class);
      final List<String> usages =
          this.getAllUsagesExceptHelp(this.dispatcher.getRoot(), source, COMMAND, true);

      if (!usages.isEmpty()) {
        player.sendMessage(Component.text(String.join("\n", usages)));
        MineAsterisk.getInstance()
            .getLogger()
            .info(
                String.format(
                    "Player %s (%s) retrieved filtered help",
                    player.getName(), player.getUniqueId()));
      } else {
        player.sendMessage(Component.text("No commands to display").color(NamedTextColor.RED));
        MineAsterisk.getInstance()
            .getLogger()
            .info(
                String.format(
                    "Player %s (%s) retrieved filtered help but there's nothing to display",
                    player.getName(), player.getUniqueId()));
      }
    } catch (final Exception exception) {
      ExceptionUtil.handleCommand(exception, sender, "filtered help");
    }

    return Command.SINGLE_SUCCESS;
  }

  private @NotNull List<@NotNull String> getAllUsagesExceptHelp(
      final @NotNull RootCommandNode<@NotNull CommandSourceStack> root,
      final @NotNull CommandSourceStack source,
      final @Nullable String command,
      final boolean isStrict) {
    final List<String> results = new ArrayList<>();
    final String[] usages = this.dispatcher.getAllUsage(root, source, true);
    final String NAMESPACE = MineAsterisk.getNamespace();

    Arrays.stream(usages)
        .filter(
            usage ->
                usage.startsWith(NAMESPACE)
                    && !usage.startsWith(String.format("%s:%s", NAMESPACE, "help")))
        .forEach(
            usage -> {
              final String STRING_TO_REPLACE = String.format("%s:", NAMESPACE);
              final String REPLACED_USAGE = usage.replaceFirst(STRING_TO_REPLACE, "");
              final String FORMATTED_USAGE = String.format("/%s", REPLACED_USAGE);

              if (command != null) {
                final String[] commandNodes = command.split("\\s+");
                final String[] replacedUsageNodes = REPLACED_USAGE.split("\\s+");

                for (int i = 0; i < commandNodes.length && i < replacedUsageNodes.length; i++) {
                  if (!commandNodes[i].equals(replacedUsageNodes[i])) {
                    break;
                  }

                  if (i == commandNodes.length - 1) {
                    results.add(isStrict ? FORMATTED_USAGE : REPLACED_USAGE);
                  }
                }

                return;
              }

              results.add(isStrict ? FORMATTED_USAGE : REPLACED_USAGE);
            });

    return results;
  }
}
