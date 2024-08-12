package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.InvitationAttribute;
import net.mineasterisk.mc.constant.forcefetch.InvitationForceFetch;
import net.mineasterisk.mc.model.InvitationModel;
import net.mineasterisk.mc.util.PluginUtil;
import org.hibernate.StatelessSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InvitationRepository
    extends Repository<InvitationModel, InvitationAttribute, InvitationForceFetch> {
  public InvitationRepository(final @NotNull StatelessSession statelessSession) {
    super(statelessSession);
  }

  public @NotNull CompletableFuture<@Nullable InvitationModel> get(
      final @NotNull InvitationAttribute attribute, final @NotNull Object value) {
    return this.get(attribute, value, null);
  }

  public @NotNull CompletableFuture<@Nullable InvitationModel> get(
      final @NotNull InvitationAttribute attribute,
      final @NotNull Object value,
      final @Nullable Set<@NotNull InvitationForceFetch> forceFetches) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            final char alias = 'i';
            final StringJoiner query =
                new StringJoiner(" ")
                    .add(String.format("from %s %c", InvitationModel.entity, alias));

            if (forceFetches != null) {
              if (forceFetches.contains(InvitationForceFetch.INVITER)) {
                query.add(
                    String.format(
                        "join fetch %c.%s", alias, InvitationAttribute.INVITER.getAttribute()));
              }

              if (forceFetches.contains(InvitationForceFetch.GUILD)) {
                query.add(
                    String.format(
                        "join fetch %c.%s", alias, InvitationAttribute.GUILD.getAttribute()));
              }
            }

            query.add(String.format("where %c.%s = :value", alias, attribute.getAttribute()));

            return this.getStatelessSession()
                .createSelectionQuery(query.toString(), InvitationModel.class)
                .setParameter("value", value)
                .getSingleResult();
          } catch (NoResultException exception) {
            PluginUtil.getLogger().info("Unable to get Guild invitation: No result found");

            return null;
          }
        });
  }

  public @NotNull CompletableFuture<@Nullable Void> add(
      final @NotNull InvitationModel invitationToAdd) {
    return CompletableFuture.runAsync(() -> this.getStatelessSession().insert(invitationToAdd));
  }

  public @NotNull CompletableFuture<@Nullable Void> update(
      final @NotNull InvitationModel invitationToUpdate) {
    return CompletableFuture.runAsync(() -> this.getStatelessSession().update(invitationToUpdate));
  }
}
