package net.mineasterisk.mc.repository;

import java.sql.Connection;
import org.jetbrains.annotations.NotNull;

public class Repository {
  private final @NotNull Connection connection;

  public Repository(final @NotNull Connection connection) {
    this.connection = connection;
  }

  public @NotNull Connection getConnection() {
    return this.connection;
  }
}
