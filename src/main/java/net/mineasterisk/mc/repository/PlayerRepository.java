package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.forcefetch.PlayerForceFetch;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.util.HibernateUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerRepository {
  public static <T> @NotNull CompletableFuture<@Nullable PlayerModel> get(
      final @NotNull PlayerAttribute attribute, final @NotNull T value) {
    return PlayerRepository.get(attribute, value, null);
  }

  public static <T> @NotNull CompletableFuture<@Nullable PlayerModel> get(
      final @NotNull PlayerAttribute attribute,
      final @NotNull T value,
      final @Nullable Set<@NotNull PlayerForceFetch> forceFetches) {
    return CompletableFuture.supplyAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .fromSession(
                    session -> {
                      try {
                        final char alias = 'p';
                        StringJoiner query =
                            new StringJoiner(" ")
                                .add(String.format("from %s %c", PlayerModel.entity, alias));

                        if (forceFetches != null) {
                          if (forceFetches.contains(PlayerForceFetch.GUILD)) {
                            query.add(
                                String.format(
                                    "left join fetch %c.%s",
                                    alias, PlayerAttribute.GUILD.getAttribute()));
                          }
                        }

                        query.add(
                            String.format("where %c.%s = :value", alias, attribute.getAttribute()));

                        return session
                            .createSelectionQuery(query.toString(), PlayerModel.class)
                            .setParameter("value", value)
                            .getSingleResult();
                      } catch (NoResultException exception) {
                        PluginUtil.getLogger().info("Unable to get Player: No result found");

                        return null;
                      }
                    }));
  }

  public static @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull PlayerModel playerToAdd) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.persist(playerToAdd)));
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull PlayerModel playerToUpdate) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.merge(playerToUpdate)));
  }
}
