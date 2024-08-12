package net.mineasterisk.mc.service;

import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.hibernate.StatelessSession;
import org.jetbrains.annotations.NotNull;

public abstract class Service<T> {
  private final @NotNull StatelessSession statelessSession;

  public Service(final @NotNull StatelessSession statelessSession) {
    this.statelessSession = statelessSession;
  }

  public @NotNull StatelessSession getStatelessSession() {
    return this.statelessSession;
  }

  public abstract @NotNull CompletableFuture<@NotNull Void> add(
      final @NotNull Player performedBy, final @NotNull T entityToAdd);

  public abstract @NotNull CompletableFuture<@NotNull Void> update(
      final @NotNull Player performedBy, final @NotNull T entityToUpdate);
}
