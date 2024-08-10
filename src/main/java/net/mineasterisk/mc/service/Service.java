package net.mineasterisk.mc.service;

import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

public abstract class Service<T> {
  private final @NotNull Session session;

  public Service(final @NotNull Session session) {
    this.session = session;
  }

  public @NotNull Session getSession() {
    return this.session;
  }

  public abstract @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull Player performedBy, final @NotNull T entityToAdd);

  public abstract @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull Player performedBy, final @NotNull T entityToUpdate);
}
