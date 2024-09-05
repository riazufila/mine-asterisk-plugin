package net.mineasterisk.mc.service.team.invitation;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public record Invitation(@NotNull Team team, @NotNull Player inviter, @NotNull Player invitee) {}
