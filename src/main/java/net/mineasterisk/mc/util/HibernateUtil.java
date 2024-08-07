package net.mineasterisk.mc.util;

import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.model.InvitationModel;
import net.mineasterisk.mc.model.PlayerModel;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HibernateUtil {
  private static @Nullable SessionFactory sessionFactory = null;

  public static void loadSessionFactory() {
    sessionFactory =
        new Configuration()
            .addAnnotatedClass(PlayerModel.class)
            .addAnnotatedClass(GuildModel.class)
            .addAnnotatedClass(InvitationModel.class)
            .buildSessionFactory();
  }

  public static @NotNull SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
      HibernateUtil.loadSessionFactory();
    }

    return sessionFactory;
  }
}
