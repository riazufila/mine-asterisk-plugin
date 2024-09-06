package net.mineasterisk.mc;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.mineasterisk.mc.util.LoaderUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class MineAsteriskBootstrap implements PluginBootstrap {
  private static @Nullable BootstrapContext context;

  public static @NotNull String getNamespace() {
    return MineAsteriskBootstrap.getContext().getPluginMeta().getName().toLowerCase();
  }

  public static @NotNull BootstrapContext getContext() {
    if (MineAsteriskBootstrap.context == null) {
      throw new IllegalStateException("Bootstrap context is not initialized");
    }

    return MineAsteriskBootstrap.context;
  }

  @Override
  public void bootstrap(@NotNull BootstrapContext context) {
    MineAsteriskBootstrap.context = context;

    try {
      LoaderUtil.bootstrapLoad();

      context.getLogger().info("Bootstrap finished");
    } catch (Exception exception) {
      context
          .getLogger()
          .error(String.format("Encountered error while in bootstrap phase: %s", exception));
    }
  }
}
