package net.mineasterisk.mc.util;

public class LoaderUtil {
  public static void preLoad() {
    CommandUtil.register();
    EventUtil.register();
    CacheUtil.load();

    PluginUtil.getLogger().info("Pre-loaded configuration(s) and resource(s)");
  }

  public static void postLoad() {
    CacheUtil.persist();

    PluginUtil.getLogger().info("Post-loaded configuration(s) and resource(s)");
  }
}
