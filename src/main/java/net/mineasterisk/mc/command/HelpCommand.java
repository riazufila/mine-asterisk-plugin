package net.mineasterisk.mc.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.Command.Builder;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.help.HelpHandler;
import org.incendo.cloud.help.HelpQuery;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.help.result.HelpQueryResult;
import org.incendo.cloud.help.result.IndexCommandResult;
import org.incendo.cloud.help.result.MultipleCommandResult;
import org.incendo.cloud.help.result.VerboseCommandResult;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;

public class HelpCommand {
  private static final @NotNull String rootCommandName = "help";

  private static void printNoResultHelp(
      @NotNull CommandSourceStack sender, @NotNull String command) {
    sender
        .getSender()
        .sendMessage(
            Component.text(String.format("Command %s is not found.", command))
                .color(NamedTextColor.RED));
  }

  private static void printIndexHelp(
      @NotNull CommandSourceStack sender,
      @NotNull String command,
      @NotNull IndexCommandResult<CommandSourceStack> result) {
    if (result.isEmpty()) {
      HelpCommand.printNoResultHelp(sender, command);
      return;
    }

    List<TextComponent> components =
        result.entries().stream()
            .map(
                entry -> {
                  String syntax = entry.syntax();
                  return Component.text(String.format("/%s", syntax));
                })
            .toList();

    sender
        .getSender()
        .sendMessage(
            Component.join(JoinConfiguration.builder().separator(Component.newline()), components));
  }

  private static void printMultipleHelp(
      @NotNull CommandSourceStack sender,
      @NotNull String command,
      @NotNull MultipleCommandResult<CommandSourceStack> result) {
    if (result.childSuggestions().isEmpty()) {
      HelpCommand.printNoResultHelp(sender, command);
      return;
    }

    List<TextComponent> components =
        result.childSuggestions().stream()
            .map(child -> Component.text(String.format("/%s", child)))
            .toList();

    sender
        .getSender()
        .sendMessage(
            Component.join(JoinConfiguration.builder().separator(Component.newline()), components));
  }

  private static void printVerboseHelp(
      @NotNull CommandSourceStack sender,
      @NotNull VerboseCommandResult<CommandSourceStack> result) {
    sender.getSender().sendMessage(Component.text(String.format("/%s", result.entry().syntax())));
  }

  private static void printHelp(
      @NotNull CommandSourceStack sender,
      @NotNull String command,
      @NotNull HelpQueryResult<CommandSourceStack> result) {
    if (result instanceof IndexCommandResult) {
      HelpCommand.printIndexHelp(sender, command, (IndexCommandResult<CommandSourceStack>) result);
    } else if (result instanceof MultipleCommandResult) {
      HelpCommand.printMultipleHelp(
          sender, command, (MultipleCommandResult<CommandSourceStack>) result);
    } else if (result instanceof VerboseCommandResult) {
      HelpCommand.printVerboseHelp(sender, (VerboseCommandResult<CommandSourceStack>) result);
    } else {
      throw new IllegalArgumentException("Unable to display help: Unknown help query result type.");
    }
  }

  private static Builder<CommandSourceStack> helpCommand(
      PaperCommandManager<CommandSourceStack> manager, HelpHandler<CommandSourceStack> help) {
    return manager
        .commandBuilder(HelpCommand.rootCommandName)
        .optional(
            "command",
            StringParser.greedyStringParser(),
            DefaultValue.constant(""),
            (context, input) ->
                CompletableFuture.supplyAsync(
                    () ->
                        help.queryRootIndex(context.sender()).entries().stream()
                            .map(CommandEntry::syntax)
                            .map(Suggestion::suggestion)
                            .collect(Collectors.toList())))
        .handler(
            context -> {
              HelpQueryResult<CommandSourceStack> result =
                  help.query(HelpQuery.of(context.sender(), context.get("command")));

              HelpCommand.printHelp(context.sender(), context.get("command"), result);
            });
  }

  public static Set<Builder<CommandSourceStack>> build(
      PaperCommandManager<CommandSourceStack> manager, HelpHandler<CommandSourceStack> help) {
    return Set.of(HelpCommand.helpCommand(manager, help));
  }
}
