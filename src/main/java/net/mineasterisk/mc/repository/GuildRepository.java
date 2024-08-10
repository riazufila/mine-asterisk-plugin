package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.GuildAttribute;
import net.mineasterisk.mc.constant.forcefetch.GuildForceFetch;
import net.mineasterisk.mc.model.GuildModel;
import net.mineasterisk.mc.util.PluginUtil;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildRepository extends Repository<GuildModel, GuildAttribute, GuildForceFetch> {
  public GuildRepository(final @NotNull Session session) {
    super(session);
  }

  public @NotNull CompletableFuture<@Nullable GuildModel> get(
      final @NotNull GuildAttribute attribute, final @NotNull Object value) {
    return this.get(attribute, value, null);
  }

  public @NotNull CompletableFuture<@Nullable GuildModel> get(
      final @NotNull GuildAttribute attribute,
      final @NotNull Object value,
      final @Nullable Set<@NotNull GuildForceFetch> forceFetches) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            final char alias = 'g';
            StringJoiner query =
                new StringJoiner(" ").add(String.format("from %s %c", GuildModel.entity, alias));

            if (forceFetches != null) {
              if (forceFetches.contains(GuildForceFetch.CREATED_BY)) {
                query.add(
                    String.format(
                        "join fetch %c.%s", alias, GuildAttribute.CREATED_BY.getAttribute()));
              }

              if (forceFetches.contains(GuildForceFetch.OWNER)) {
                query.add(
                    String.format("join fetch %c.%s", alias, GuildAttribute.OWNER.getAttribute()));
              }

              if (forceFetches.contains(GuildForceFetch.PLAYERS)) {
                query.add(
                    String.format(
                        "left join fetch %c.%s", alias, GuildAttribute.PLAYERS.getAttribute()));
              }
            }

            query.add(String.format("where %c.%s = :value", alias, attribute.getAttribute()));

            GuildModel guild =
                this.getSession()
                    .createSelectionQuery(query.toString(), GuildModel.class)
                    .setParameter("value", value)
                    .getSingleResult();

            this.getSession().evict(guild);

            return guild;
          } catch (NoResultException exception) {
            PluginUtil.getLogger().info("Unable to get Guild: No result found");

            return null;
          }
        });
  }

  public @NotNull CompletableFuture<@NotNull Void> add(final @NotNull GuildModel guildToAdd) {
    return CompletableFuture.runAsync(() -> this.getSession().persist(guildToAdd));
  }

  public @NotNull CompletableFuture<@NotNull Void> update(final @NotNull GuildModel guildToUpdate) {
    return CompletableFuture.runAsync(() -> this.getSession().merge(guildToUpdate));
  }
}
