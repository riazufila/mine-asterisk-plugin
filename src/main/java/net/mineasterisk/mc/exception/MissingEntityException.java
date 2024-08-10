package net.mineasterisk.mc.exception;

import org.jetbrains.annotations.NotNull;

public class MissingEntityException extends ValidationException {
  private final Class<?> clazz;

  public MissingEntityException(
      final @NotNull String clientMessage,
      final @NotNull String message,
      final @NotNull Class<?> clazz) {
    super(clientMessage, message);
    this.clazz = clazz;
  }

  public @NotNull Class<?> getClazz() {
    return this.clazz;
  }
}
