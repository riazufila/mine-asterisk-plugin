package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.repository.option.attribute.PlayerRepositoryOptionAttribute;
import net.mineasterisk.mc.repository.option.forcefetch.PlayerRepositoryOptionForceFetch;
import net.mineasterisk.mc.util.HibernateUtil;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerRepository {
  public static <T> @NotNull CompletableFuture<@Nullable PlayerModel> get(
      @NotNull PlayerRepositoryOptionAttribute attribute, @NotNull T value) {
    return PlayerRepository.get(attribute, value, null);
  }

  public static <T> @NotNull CompletableFuture<@Nullable PlayerModel> get(
      @NotNull PlayerRepositoryOptionAttribute attribute,
      @NotNull T value,
      @Nullable Set<PlayerRepositoryOptionForceFetch> forceFetches) {
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
            MineAsterisk.getPluginLogger().info("Unable to get Player: No result found.");

            return null;
          }
        });
  }

  public static @NotNull CompletableFuture<@NotNull Void> add(@NotNull PlayerModel playerToAdd) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.persist(playerToAdd)));
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      @NotNull PlayerModel updatedPlayer) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.merge(updatedPlayer)));
  }
}
