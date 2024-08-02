package net.mineasterisk.mc.repository.option.attribute;

import org.jetbrains.annotations.NotNull;

public enum GuildRepositoryOptionAttribute {
  ID("id"),
  CREATED_AT("createdAt"),
  CREATED_BY("createdBy"),
  NAME("name"),
  OWNER("owner"),
  GUILD_STATUS("status");

  private final @NotNull String attribute;

  GuildRepositoryOptionAttribute(final @NotNull String attribute) {
    this.attribute = attribute;
  }

  public @NotNull String getAttribute() {
    return attribute;
  }
}
