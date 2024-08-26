package net.mineasterisk.mc.command.parser;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.SelectorArgumentResolver;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.exception.EntityException;
import net.mineasterisk.mc.exception.ValidationException;
import net.mineasterisk.mc.util.PluginUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class OfflinePlayerInTeamExceptSelf
    implements CustomArgumentType<SelectorArgumentResolver<OfflinePlayer>, String> {
  @Override
  public @NotNull SelectorArgumentResolver<OfflinePlayer> parse(
      final @NotNull StringReader reader) {
    final String playerName = reader.readUnquotedString();

    return source -> {
      final CommandSender sender = source.getSender();

      if (!(sender instanceof Player player)) {
        throw new EntityException(
            String.format(
                "Sender %s isn't a Player and tries to execute command", sender.getName()));
      }

      final Scoreboard scoreboard = PluginUtil.getMainScoreboard();
      final Team team = scoreboard.getEntityTeam(player);

      if (team == null) {
        throw new ValidationException(
            "Not in a Team",
            String.format(
                "Player %s (%s) isn't in a Team", player.getName(), player.getUniqueId()));
      }

      //noinspection deprecation
      final Optional<OfflinePlayer> offlinePlayer =
          team.getPlayers().stream()
              .filter(
                  offlinePlayerInTeam ->
                      offlinePlayerInTeam.hasPlayedBefore()
                          && offlinePlayerInTeam.getName() != null
                          && offlinePlayerInTeam.getName().equals(playerName))
              .findFirst();

      if (offlinePlayer.isEmpty()) {
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .dispatcherUnknownArgument()
            .createWithContext(reader);
      }

      return offlinePlayer.get();
    };
  }

  @Override
  public @NotNull ArgumentType<String> getNativeType() {
    return StringArgumentType.greedyString();
  }

  @Override
  public @NotNull <S> CompletableFuture<@NotNull Suggestions> listSuggestions(
      final @NotNull CommandContext<@NotNull S> context,
      final @NotNull SuggestionsBuilder builder) {
    final CommandSourceStack source = (CommandSourceStack) context.getSource();
    final CommandSender sender = source.getSender();

    if (!(sender instanceof Player player)) {
      throw new EntityException(
          String.format("Sender %s isn't a Player and tries to execute command", sender.getName()));
    }

    final Scoreboard scoreboard = PluginUtil.getMainScoreboard();
    final Team team = scoreboard.getEntityTeam(player);

    if (team == null) {
      throw new ValidationException(
          "Not in a Team",
          String.format("Player %s (%s) isn't in a Team", player.getName(), player.getUniqueId()));
    }

    //noinspection deprecation
    team.getPlayers().stream()
        .filter(
            offlinePlayerInTeam ->
                offlinePlayerInTeam.hasPlayedBefore()
                    && offlinePlayerInTeam.getName() != null
                    && !offlinePlayerInTeam.getName().equals(player.getName()))
        .forEach(offlinePlayerInTeam -> builder.suggest(offlinePlayerInTeam.getName()));

    return builder.buildFuture();
  }
}
