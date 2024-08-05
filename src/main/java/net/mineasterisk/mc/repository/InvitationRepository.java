package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.InvitationAttribute;
import net.mineasterisk.mc.constant.forcefetch.InvitationForceFetch;
import net.mineasterisk.mc.model.InvitationModel;
import net.mineasterisk.mc.util.HibernateUtil;
import net.mineasterisk.mc.util.PluginUtil;
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
        () ->
            HibernateUtil.getSessionFactory()
                .fromSession(
                    session -> {
                      try {
                        final char alias = 'i';
                        StringJoiner query =
                            new StringJoiner(" ")
                                .add(String.format("from %s %c", InvitationModel.entity, alias));

                        if (forceFetches != null) {
                          if (forceFetches.contains(InvitationForceFetch.INVITER)) {
                            query.add(
                                String.format(
                                    "join fetch %c.%s",
                                    alias, InvitationAttribute.INVITER.getAttribute()));
                          }

                          if (forceFetches.contains(InvitationForceFetch.GUILD)) {
                            query.add(
                                String.format(
                                    "join fetch %c.%s",
                                    alias, InvitationAttribute.GUILD.getAttribute()));
                          }
                        }

                        query.add(
                            String.format("where %c.%s = :value", alias, attribute.getAttribute()));

                        return session
                            .createSelectionQuery(query.toString(), InvitationModel.class)
                            .setParameter("value", value)
                            .getSingleResult();
                      } catch (NoResultException exception) {
                        PluginUtil.getLogger()
                            .info("Unable to get Guild invitation: No result found");

                        return null;
                      }
                    }));
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
