package net.mineasterisk.mc.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class AccessCache implements Cache<@NotNull UUID, @NotNull Set<@NotNull String>> {
  private static final @NotNull Set<@NotNull UUID> DIRTY = new HashSet<>();
  private static final @NotNull HashMap<@NotNull UUID, @NotNull Set<@NotNull String>> CACHE =
      new HashMap<>();

  public @NotNull Set<@NotNull String> get(final @NotNull UUID key) {
    return AccessCache.CACHE.getOrDefault(key, new HashSet<>());
  }

  @Override
  public @NotNull HashMap<@NotNull UUID, @NotNull Set<@NotNull String>> getDirtyEntries() {
    final HashMap<UUID, Set<String>> dirtyEntries = new HashMap<>();

    for (final UUID key : AccessCache.DIRTY) {
      if (AccessCache.CACHE.containsKey(key)) {
        dirtyEntries.put(key, AccessCache.CACHE.get(key));
      }
    }

    return dirtyEntries;
  }

  public void put(final @NotNull UUID key, final @NotNull String value) {
    final Set<String> permissions = this.get(key);

    if (permissions.contains(value)) {
      return;
    }

    permissions.add(value);
    AccessCache.CACHE.put(key, permissions);
    AccessCache.DIRTY.add(key);
  }

  @Override
  public void putAll(final @NotNull HashMap<@NotNull UUID, @NotNull Set<@NotNull String>> cache) {
    AccessCache.CACHE.putAll(cache);
  }

  public void remove(final @NotNull UUID key, final @NotNull String value) {
    final Set<String> permissions = this.get(key);

    if (!permissions.contains(value)) {
      return;
    }

    permissions.remove(value);
    AccessCache.CACHE.put(key, permissions);
    AccessCache.DIRTY.add(key);
  }
}
