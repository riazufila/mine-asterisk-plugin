package net.mineasterisk.mc.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public interface Command {
  @NotNull
  LiteralCommandNode<@NotNull CommandSourceStack> build();
}
