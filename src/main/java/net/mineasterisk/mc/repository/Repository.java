package net.mineasterisk.mc.repository;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Repository<T, U, V> {
  private final @NotNull Session session;

  public Repository(final @NotNull Session session) {
    this.session = session;
  }

  public @NotNull Session getSession() {
    return this.session;
  }

  public abstract @NotNull CompletableFuture<@Nullable T> get(
      final @NotNull U attribute, final @NotNull Object value);

  public abstract @NotNull CompletableFuture<@Nullable T> get(
      final @NotNull U attribute,
      final @NotNull Object value,
      final @Nullable Set<@NotNull V> forceFetches);

  public abstract @NotNull CompletableFuture<@NotNull Void> add(final @NotNull T entityToAdd);

  public abstract @NotNull CompletableFuture<@NotNull Void> update(final @NotNull T entityToUpdate);
}
