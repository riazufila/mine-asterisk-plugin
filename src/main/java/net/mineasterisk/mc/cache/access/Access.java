package net.mineasterisk.mc.cache.access;

import java.util.Set;
import net.mineasterisk.mc.cache.Cacheable;
import org.jetbrains.annotations.NotNull;

public class Access extends Cacheable {
  private final @NotNull Set<@NotNull String> accesses;

  public Access(final @NotNull Set<@NotNull String> accesses) {
    this.accesses = accesses;
  }

  public @NotNull Set<@NotNull String> getAccesses() {
    return this.accesses;
  }

  public void addAccess(final @NotNull String access) {
    if (this.accesses.contains(access)) {
      return;
    }

    this.accesses.add(access);
    this.setDirty();
  }

  public void removeAccess(final @NotNull String access) {
    if (!this.accesses.contains(access)) {
      return;
    }

    this.accesses.remove(access);
    this.setDirty();
  }
}
