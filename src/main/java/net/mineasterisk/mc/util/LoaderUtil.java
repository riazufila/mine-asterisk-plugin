package net.mineasterisk.mc.util;

public class LoaderUtil {
  public static boolean startup() {
    try {
      HibernateUtil.loadSessionFactory();
      CommandUtil.register();
      EventUtil.register();

      return true;
    } catch (Exception exception) {
      PluginUtil.getLogger()
          .severe(String.format("Unable to load startup configurations: %s", exception));

      return false;
    }
  }
}
