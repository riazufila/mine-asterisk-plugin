package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.option.attribute.PlayerRepositoryOptionAttribute;
import net.mineasterisk.mc.repository.option.forcefetch.PlayerRepositoryOptionForceFetch;
import net.mineasterisk.mc.util.HibernateUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerRepository {
  public static <T> @NotNull CompletableFuture<@Nullable PlayerModel> get(
      final @NotNull PlayerRepositoryOptionAttribute attribute, final @NotNull T value) {
    return PlayerRepository.get(attribute, value, null);
  }

  public static <T> @NotNull CompletableFuture<@Nullable PlayerModel> get(
      final @NotNull PlayerRepositoryOptionAttribute attribute,
      final @NotNull T value,
      final @Nullable Set<@NotNull PlayerRepositoryOptionForceFetch> forceFetches) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<PlayerModel> query = builder.createQuery(PlayerModel.class);
            Root<PlayerModel> root = query.from(PlayerModel.class);

            if (forceFetches != null) {
              if (forceFetches.contains(PlayerRepositoryOptionForceFetch.GUILD)) {
                root.fetch("guild");
              }
            }

            query.select(root);
            query.where(builder.equal(root.get(attribute.getAttribute()), value));

            return session.createQuery(query).getSingleResult();
          } catch (NoResultException exception) {
            PluginUtil.getLogger().info("Unable to get Player: No result found.");

            return null;
          }
        });
  }

  public static @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull PlayerModel playerToAdd) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.persist(playerToAdd)));
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull PlayerModel updatedPlayer) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.merge(updatedPlayer)));
  }
}
