package net.mineasterisk.mc.constant.attribute;

import org.jetbrains.annotations.NotNull;

public enum PlayerAttribute {
  ID("id"),
  CREATED_AT("createdAt"),
  UUID("uuid"),
  GUILD("guild");

  private final @NotNull String attribute;

  PlayerAttribute(final @NotNull String attribute) {
    this.attribute = attribute;
  }

  public @NotNull String getAttribute() {
    return this.attribute;
  }
}
