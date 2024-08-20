package net.mineasterisk.mc.constant;

import org.jetbrains.annotations.NotNull;

public enum PermissionConstant {
  TEAM_OWNER("team.owner"),
  TEAM_MEMBER("team.member");

  private final String value;

  PermissionConstant(final @NotNull String value) {
    this.value = value;
  }

  @Override
  public @NotNull String toString() {
    return this.value;
  }
}
