package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.InvitationAttribute;
import net.mineasterisk.mc.constant.forcefetch.InvitationForceFetch;
import net.mineasterisk.mc.model.InvitationModel;
import net.mineasterisk.mc.util.HibernateUtil;
import net.mineasterisk.mc.util.PluginUtil;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InvitationRepository {
  public static <T> @NotNull CompletableFuture<@Nullable InvitationModel> get(
      final @NotNull InvitationAttribute attribute, final @NotNull T value) {
    return InvitationRepository.get(attribute, value, null);
  }

  public static <T> @NotNull CompletableFuture<@Nullable InvitationModel> get(
      final @NotNull InvitationAttribute attribute,
      final @NotNull T value,
      final @Nullable Set<@NotNull InvitationForceFetch> forceFetches) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<InvitationModel> query = builder.createQuery(InvitationModel.class);
            jakarta.persistence.criteria.Root<InvitationModel> root =
                query.from(InvitationModel.class);

            if (forceFetches != null) {
              if (forceFetches.contains(InvitationForceFetch.CREATED_BY)) {
                root.fetch(InvitationAttribute.CREATED_BY.getAttribute());
              }

              if (forceFetches.contains(InvitationForceFetch.GUILD)) {
                root.fetch(InvitationAttribute.GUILD.getAttribute());
              }
            }

            query.select(root);
            query.where(builder.equal(root.get(attribute.getAttribute()), value));

            return session.createQuery(query).getSingleResult();
          } catch (NoResultException exception) {
            PluginUtil.getLogger().info("Unable to get Guild invitation: No result found");

            return null;
          }
        });
  }

  public static @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull InvitationModel invitationToAdd) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.persist(invitationToAdd)));
  }

  public static @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull InvitationModel updatedInvitation) {
    return CompletableFuture.runAsync(
        () ->
            HibernateUtil.getSessionFactory()
                .inTransaction(session -> session.merge(updatedInvitation)));
  }
}
