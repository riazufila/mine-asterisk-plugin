package net.mineasterisk.mc.util;

import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.model.PlayerModel;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.NotNull;


public final class HibernateUtil {
    private static SessionFactory sessionFactory = null;

    public static void loadSessionFactory() {
        sessionFactory = new Configuration()
                .addAnnotatedClass(PlayerModel.class)
                .addAnnotatedClass(GuildModel.class)
                .buildSessionFactory();
    }

    public static @NotNull SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            HibernateUtil.loadSessionFactory();
        }

        return sessionFactory;
    }
}
