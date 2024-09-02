package net.mineasterisk.mc.constant;

import org.jetbrains.annotations.NotNull;

public enum PermissionConstant {
  TEAM_LEADER("team.leader"),
  TEAM_MEMBER("team.member");

  private final String name;

  PermissionConstant(final @NotNull String name) {
    this.name = name;
  }

  @Override
  public @NotNull String toString() {
    return this.name;
  }
}
