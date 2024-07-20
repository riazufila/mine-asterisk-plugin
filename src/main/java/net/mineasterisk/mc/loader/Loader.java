package net.mineasterisk.mc.loader;

import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.util.HibernateUtil;

public class Loader {
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
