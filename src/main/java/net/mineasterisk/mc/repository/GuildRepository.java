package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.MineAsterisk;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.repository.option.attribute.GuildRepositoryOptionAttribute;
import net.mineasterisk.mc.repository.option.forcefetch.GuildRepositoryOptionForceFetch;
import net.mineasterisk.mc.util.HibernateUtil;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GuildRepository {
  public static <T> @NotNull CompletableFuture<@Nullable GuildModel> get(
      @NotNull GuildRepositoryOptionAttribute attribute, @NotNull T value) {
    return GuildRepository.get(attribute, value, null);
  }

  public static <T> @NotNull CompletableFuture<@Nullable GuildModel> get(
      @NotNull GuildRepositoryOptionAttribute attribute,
      @NotNull T value,
      @Nullable Set<GuildRepositoryOptionForceFetch> forceFetches) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<GuildModel> query = builder.createQuery(GuildModel.class);
            Root<GuildModel> root = query.from(GuildModel.class);

            if (forceFetches != null) {
              if (forceFetches.contains(GuildRepositoryOptionForceFetch.CREATED_BY)) {
                root.fetch("createdBy");
              }

              if (forceFetches.contains(GuildRepositoryOptionForceFetch.OWNER)) {
                root.fetch("owner");
              }

              if (forceFetches.contains(GuildRepositoryOptionForceFetch.PLAYERS)) {
                root.fetch("players");
              }
            }

            query.select(root);
            query.where(builder.equal(root.get(attribute.getAttribute()), value));

            return session.createQuery(query).getSingleResult();
          } catch (NoResultException exception) {
            MineAsterisk.getPluginLogger().info("Unable to get Guild: No result found.");

            return null;
          }
        });
  }

  public static @NotNull CompletableFuture<@NotNull Void> add(@NotNull GuildModel guildToAdd) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.persist(guildToAdd)));
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(@NotNull GuildModel updatedGuild) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.merge(updatedGuild)));
  }
}
