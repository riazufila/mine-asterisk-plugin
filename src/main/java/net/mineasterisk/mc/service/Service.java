package net.mineasterisk.mc.service;

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
}
