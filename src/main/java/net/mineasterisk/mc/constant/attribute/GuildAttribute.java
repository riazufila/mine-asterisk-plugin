package net.mineasterisk.mc.constant.attribute;

import org.jetbrains.annotations.NotNull;

public enum GuildAttribute {
  ID("id"),
  CREATED_AT("createdAt"),
  CREATED_BY("createdBy"),
  NAME("name"),
  OWNER("owner"),
  GUILD_STATUS("status"),
  PLAYERS("players");

  private final @NotNull String attribute;

  GuildAttribute(final @NotNull String attribute) {
    this.attribute = attribute;
  }

  public @NotNull String getAttribute() {
    return attribute;
  }
}
