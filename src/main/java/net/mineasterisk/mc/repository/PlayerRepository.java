package net.mineasterisk.mc.repository;

import jakarta.persistence.NoResultException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import net.mineasterisk.mc.constant.attribute.PlayerAttribute;
import net.mineasterisk.mc.constant.forcefetch.PlayerForceFetch;
import net.mineasterisk.mc.model.PlayerModel;
import net.mineasterisk.mc.util.PluginUtil;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerRepository extends Repository<PlayerModel, PlayerAttribute, PlayerForceFetch> {
  public PlayerRepository(@NotNull Session session) {
    super(session);
  }

  public @NotNull CompletableFuture<@Nullable PlayerModel> get(
      final @NotNull PlayerAttribute attribute, final @NotNull Object value) {
    return this.get(attribute, value, null);
  }

  public @NotNull CompletableFuture<@Nullable PlayerModel> get(
      final @NotNull PlayerAttribute attribute,
      final @NotNull Object value,
      final @Nullable Set<@NotNull PlayerForceFetch> forceFetches) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            final char alias = 'p';
            StringJoiner query =
                new StringJoiner(" ").add(String.format("from %s %c", PlayerModel.entity, alias));

            if (forceFetches != null) {
              if (forceFetches.contains(PlayerForceFetch.GUILD)) {
                query.add(
                    String.format(
                        "left join fetch %c.%s", alias, PlayerAttribute.GUILD.getAttribute()));
              }
            }

            query.add(String.format("where %c.%s = :value", alias, attribute.getAttribute()));

            PlayerModel player =
                this.getSession()
                    .createSelectionQuery(query.toString(), PlayerModel.class)
                    .setParameter("value", value)
                    .getSingleResult();

            this.getSession().evict(player);

            return player;
          } catch (NoResultException exception) {
            PluginUtil.getLogger().info("Unable to get Player: No result found");

            return null;
          }
        });
  }

  public @NotNull CompletableFuture<@NotNull Void> add(final @NotNull PlayerModel playerToAdd) {
    return CompletableFuture.runAsync(() -> this.getSession().persist(playerToAdd));
  }

  public @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull PlayerModel playerToUpdate) {
    return CompletableFuture.runAsync(() -> this.getSession().merge(playerToUpdate));
  }
}
