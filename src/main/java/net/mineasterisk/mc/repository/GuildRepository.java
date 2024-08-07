package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.GuildAttribute;
import net.mineasterisk.mc.constant.forcefetch.GuildForceFetch;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.util.HibernateUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildRepository {
  public static <T> @NotNull CompletableFuture<@Nullable GuildModel> get(
      final @NotNull GuildAttribute attribute, final @NotNull T value) {
    return GuildRepository.get(attribute, value, null);
  }

  public static <T> @NotNull CompletableFuture<@Nullable GuildModel> get(
      final @NotNull GuildAttribute attribute,
      final @NotNull T value,
      final @Nullable Set<@NotNull GuildForceFetch> forceFetches) {
    return CompletableFuture.supplyAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .fromSession(
                    session -> {
                      try {
                        final char alias = 'g';
                        StringJoiner query =
                            new StringJoiner(" ")
                                .add(String.format("from %s %c", GuildModel.entity, alias));

                        if (forceFetches != null) {
                          if (forceFetches.contains(GuildForceFetch.CREATED_BY)) {
                            query.add(
                                String.format(
                                    "join fetch %c.%s",
                                    alias, GuildAttribute.CREATED_BY.getAttribute()));
                          }

                          if (forceFetches.contains(GuildForceFetch.OWNER)) {
                            query.add(
                                String.format(
                                    "join fetch %c.%s",
                                    alias, GuildAttribute.OWNER.getAttribute()));
                          }

                          if (forceFetches.contains(GuildForceFetch.PLAYERS)) {
                            query.add(
                                String.format(
                                    "left join fetch %c.%s",
                                    alias, GuildAttribute.PLAYERS.getAttribute()));
                          }
                        }

                        query.add(
                            String.format("where %c.%s = :value", alias, attribute.getAttribute()));

                        return session
                            .createSelectionQuery(query.toString(), GuildModel.class)
                            .setParameter("value", value)
                            .getSingleResult();
                      } catch (NoResultException exception) {
                        PluginUtil.getLogger().info("Unable to get Guild: No result found");

                        return null;
                      }
                    }));
  }

  public static @NotNull CompletableFuture<@NotNull Void> add(@NotNull GuildModel guildToAdd) {
    return CompletableFuture.runAsync(
        () -> {
          try {
            HibernateUtil.getSessionFactory().inTransaction(session -> session.persist(guildToAdd));
          } catch (Exception exception) {
            PluginUtil.getLogger().severe(String.format("Unable to persist Guild: %s", exception));

            throw new RuntimeException(exception);
          }
        });
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull GuildModel updatedGuild) {
    return CompletableFuture.runAsync(
        () -> {
          try {
            HibernateUtil.getSessionFactory().inTransaction(session -> session.merge(updatedGuild));
          } catch (Exception exception) {
            PluginUtil.getLogger().severe(String.format("Unable to merge Guild: %s", exception));

            throw new RuntimeException(exception);
          }
        });
  }
}
