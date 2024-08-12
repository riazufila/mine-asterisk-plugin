package net.mineasterisk.mc.repository;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.hibernate.StatelessSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Repository<T, U, V> {
  private final @NotNull StatelessSession statelessSession;

  public Repository(final @NotNull StatelessSession statelessSession) {
    this.statelessSession = statelessSession;
  }

  public @NotNull StatelessSession getStatelessSession() {
    return this.statelessSession;
  }

  public abstract @NotNull CompletableFuture<@Nullable T> get(
      final @NotNull U attribute, final @NotNull Object value);

  public abstract @NotNull CompletableFuture<@Nullable T> get(
      final @NotNull U attribute,
      final @NotNull Object value,
      final @Nullable Set<@NotNull V> forceFetches);

  public abstract @NotNull CompletableFuture<@Nullable Void> add(final @NotNull T entityToAdd);

  public abstract @NotNull CompletableFuture<@Nullable Void> update(
      final @NotNull T entityToUpdate);
}
