package net.mineasterisk.mc.exception;

import org.jetbrains.annotations.NotNull;

public class ValidationException extends RuntimeException {
  private final @NotNull String clientMessage;

  public ValidationException(final @NotNull String clientMessage, final @NotNull String message) {
    super(message);
    this.clientMessage = clientMessage;
  }

  public @NotNull String getClientMessage() {
    return this.clientMessage;
  }
}
