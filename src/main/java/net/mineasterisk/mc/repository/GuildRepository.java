package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.repository.option.attribute.GuildRepositoryOptionAttribute;
import net.mineasterisk.mc.repository.option.forcefetch.GuildRepositoryOptionForceFetch;
import net.mineasterisk.mc.util.HibernateUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildRepository {
  public static <T> @NotNull CompletableFuture<@Nullable GuildModel> get(
      final @NotNull GuildRepositoryOptionAttribute attribute, final @NotNull T value) {
    return GuildRepository.get(attribute, value, null);
  }

  public static <T> @NotNull CompletableFuture<@Nullable GuildModel> get(
      final @NotNull GuildRepositoryOptionAttribute attribute,
      final @NotNull T value,
      final @Nullable Set<@NotNull GuildRepositoryOptionForceFetch> forceFetches) {
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
            PluginUtil.getLogger().info("Unable to get Guild: No result found");

            return null;
          }
        });
  }

  public static @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull GuildModel guildToAdd) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.persist(guildToAdd)));
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull GuildModel updatedGuild) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.merge(updatedGuild)));
  }
}
