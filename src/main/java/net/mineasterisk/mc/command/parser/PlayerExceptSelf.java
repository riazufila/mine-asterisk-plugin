package net.mineasterisk.mc.command.parser;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class PlayerExceptSelf
    implements CustomArgumentType<PlayerSelectorArgumentResolver, PlayerSelectorArgumentResolver> {
  @Override
  public @NotNull PlayerSelectorArgumentResolver parse(final @NotNull StringReader reader)
      throws CommandSyntaxException {
    final PlayerSelectorArgumentResolver resolver = ArgumentTypes.player().parse(reader);

    return source -> {
      final List<Player> players = resolver.resolve(source);
      final Optional<Player> self =
          players.stream()
              .filter(
                  resolvedPlayer -> resolvedPlayer.getName().equals(source.getSender().getName()))
              .findFirst();

      if (self.isPresent()) {
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .dispatcherUnknownArgument()
            .createWithContext(reader);
      }

      return players;
    };
  }

  @Override
  public @NotNull ArgumentType<@NotNull PlayerSelectorArgumentResolver> getNativeType() {
    return ArgumentTypes.player();
  }

  @Override
  public @NotNull <S> CompletableFuture<@NotNull Suggestions> listSuggestions(
      final @NotNull CommandContext<@NotNull S> context,
      final @NotNull SuggestionsBuilder builder) {
    final CommandSourceStack source = (CommandSourceStack) context.getSource();
    final CommandSender sender = source.getSender();

    PluginUtil.getServer().getOnlinePlayers().stream()
        .filter(player -> !player.getName().equals(sender.getName()))
        .forEach(player -> builder.suggest(player.getName()));

    return builder.buildFuture();
  }
}
