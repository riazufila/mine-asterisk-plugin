package net.mineasterisk.mc.service.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mineasterisk.mc.MineAsterisk;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class PlayerService {
  private final @NotNull Player player;

  public PlayerService(final @NotNull Player player) {
    this.player = player;
  }

  public void message(final @NotNull Player recipient, final @NotNull String message) {
    final ScoreboardManager manager = MineAsterisk.getInstance().getServer().getScoreboardManager();
    final Scoreboard scoreboard = manager.getMainScoreboard();
    final Team team = scoreboard.getEntityTeam(this.player);

    final Component messageComponent =
        Component.textOfChildren(
            Component.text("<"),
            team != null
                ? Component.textOfChildren(
                    Component.text(team.getName()).color(NamedTextColor.GRAY),
                    Component.text('.').color(NamedTextColor.GRAY),
                    Component.text(this.player.getName()))
                : Component.text(this.player.getName()),
            Component.text(">"),
            Component.space(),
            Component.text("whispers,"),
            Component.space(),
            Component.text("\""),
            Component.text(message),
            Component.text("\""));

    recipient.sendMessage(messageComponent.color(NamedTextColor.LIGHT_PURPLE));
  }
}
