package net.mineasterisk.mc.util;

import net.mineasterisk.mc.MineAsterisk;

public class LoaderUtil {
  public static boolean startup() {
    try {
      HibernateUtil.loadSessionFactory();

      return true;
    } catch (Exception exception) {
      MineAsterisk.getPluginLogger()
          .severe(String.format("Unable to load startup configurations: %s", exception));

      return false;
    }
  }
}
