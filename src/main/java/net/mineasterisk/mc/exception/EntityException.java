package net.mineasterisk.mc.exception;

import org.jetbrains.annotations.NotNull;

public class EntityException extends RuntimeException {
  public EntityException(final @NotNull String message) {
    super(message);
  }
}
