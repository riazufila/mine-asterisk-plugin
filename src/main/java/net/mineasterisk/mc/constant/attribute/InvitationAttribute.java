package net.mineasterisk.mc.constant.attribute;

import org.jetbrains.annotations.NotNull;

public enum InvitationAttribute {
  ID("id"),
  CREATED_AT("createdAt"),
  INVITER("inviter"),
  INVITEE("invitee"),
  GUILD("guild"),
  STATUS("status");

  private final @NotNull String attribute;

  InvitationAttribute(final @NotNull String attribute) {
    this.attribute = attribute;
  }

  public @NotNull String getAttribute() {
    return this.attribute;
  }
}
