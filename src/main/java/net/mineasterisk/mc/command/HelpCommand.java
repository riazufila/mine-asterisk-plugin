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
  private final @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager;
  private final @NotNull HelpHandler<@NotNull CommandSourceStack> help;
  private final @NotNull String rootCommandName = "help";

  public HelpCommand(
      final @NotNull PaperCommandManager<@NotNull CommandSourceStack> manager,
      final @NotNull HelpHandler<@NotNull CommandSourceStack> help) {
    this.manager = manager;
    this.help = help;
  }

  public @NotNull Set<@NotNull Builder<@NotNull CommandSourceStack>> build() {
    return Set.of(this.helpCommand());
  }

  private @NotNull Builder<@NotNull CommandSourceStack> helpCommand() {
    final String commandArgument = "command";

    return manager
        .commandBuilder(this.rootCommandName)
        .optional(
            commandArgument,
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
                  help.query(HelpQuery.of(context.sender(), context.get(commandArgument)));

              this.printHelp(context.sender(), context.get(commandArgument), result);
            });
  }

  private void printHelp(
      final @NotNull CommandSourceStack sender,
      final @NotNull String command,
      final @NotNull HelpQueryResult<@NotNull CommandSourceStack> result) {
    switch (result) {
      case IndexCommandResult<CommandSourceStack> indexCommandResult ->
          this.printIndexHelp(sender, command, indexCommandResult);
      case MultipleCommandResult<CommandSourceStack> multipleCommandResult ->
          this.printMultipleHelp(sender, command, multipleCommandResult);
      case VerboseCommandResult<CommandSourceStack> verboseCommandResult ->
          this.printVerboseHelp(sender, verboseCommandResult);
      default ->
          throw new IllegalArgumentException(
              "Unable to display help: Unknown help query result type");
    }
  }

  private void printNoResultHelp(
      final @NotNull CommandSourceStack sender, final @NotNull String command) {
    sender
        .getSender()
        .sendMessage(
            Component.text(String.format("Command %s isn't found", command))
                .color(NamedTextColor.RED));
  }

  private void printIndexHelp(
      final @NotNull CommandSourceStack sender,
      final @NotNull String command,
      final @NotNull IndexCommandResult<@NotNull CommandSourceStack> result) {
    if (result.isEmpty()) {
      this.printNoResultHelp(sender, command);
      return;
    }

    final List<TextComponent> components =
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

  private void printMultipleHelp(
      final @NotNull CommandSourceStack sender,
      final @NotNull String command,
      final @NotNull MultipleCommandResult<@NotNull CommandSourceStack> result) {
    if (result.childSuggestions().isEmpty()) {
      this.printNoResultHelp(sender, command);
      return;
    }

    final List<TextComponent> components =
        result.childSuggestions().stream()
            .map(child -> Component.text(String.format("/%s", child)))
            .toList();

    sender
        .getSender()
        .sendMessage(
            Component.join(JoinConfiguration.builder().separator(Component.newline()), components));
  }

  private void printVerboseHelp(
      final @NotNull CommandSourceStack sender,
      final @NotNull VerboseCommandResult<@NotNull CommandSourceStack> result) {
    sender.getSender().sendMessage(Component.text(String.format("/%s", result.entry().syntax())));
  }
}
