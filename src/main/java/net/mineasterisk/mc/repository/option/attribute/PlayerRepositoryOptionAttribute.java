package net.mineasterisk.mc.repository.option.attribute;

import org.jetbrains.annotations.NotNull;

public enum PlayerRepositoryOptionAttribute {
  ID("id"),
  CREATED_AT("createdAt"),
  UUID("uuid"),
  GUILD("guild");

  private final @NotNull String attribute;

  PlayerRepositoryOptionAttribute(final @NotNull String attribute) {
    this.attribute = attribute;
  }

  public @NotNull String getAttribute() {
    return attribute;
  }
}
