package net.mineasterisk.mc.util;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.mineasterisk.mc.command.HelpCommand;
import net.mineasterisk.mc.command.TeamCommand;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("UnstableApiUsage")
class CommandUtil {
  protected static void register() {
    final LifecycleEventManager<Plugin> manager = PluginUtil.getLifecycleManager();

    manager.registerEventHandler(
        LifecycleEvents.COMMANDS,
        event -> {
          final Commands commands = event.registrar();

          commands.register(new TeamCommand().build());
          commands.register(new HelpCommand(commands.getDispatcher()).build());
        });

    PluginUtil.getLogger().info("Registered command(s)");
  }
}
