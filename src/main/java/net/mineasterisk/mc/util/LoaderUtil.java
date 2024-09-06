package net.mineasterisk.mc.util;

import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.MineAsteriskBootstrap;

public class LoaderUtil {
  @SuppressWarnings("UnstableApiUsage")
  public static void bootstrapLoad() {
    EnchantmentUtil.register();

    MineAsteriskBootstrap.getContext()
        .getLogger()
        .info("Bootstrap loaded configuration(s) and resource(s)");
  }

  public static void preLoad() {
    DatabaseUtil.initialize();
    CommandUtil.register();
    EventUtil.register();
    CacheUtil.load();

    MineAsterisk.getInstance().getLogger().info("Pre-loaded configuration(s) and resource(s)");
  }

  public static void postLoad() {
    CacheUtil.finishAllSyncer();
    CacheUtil.persist(false);

    MineAsterisk.getInstance().getLogger().info("Post-loaded configuration(s) and resource(s)");
  }
}
