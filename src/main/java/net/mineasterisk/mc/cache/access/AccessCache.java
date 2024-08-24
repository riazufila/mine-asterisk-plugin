package net.mineasterisk.mc.cache.access;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import net.mineasterisk.mc.cache.Cache;
import org.jetbrains.annotations.NotNull;

public class AccessCache implements Cache<@NotNull UUID, @NotNull Access> {
  private static final @NotNull HashMap<@NotNull UUID, @NotNull Access> CACHE = new HashMap<>();

  @Override
  public @NotNull Access get(final @NotNull UUID key) {
    return AccessCache.CACHE.computeIfAbsent(key, k -> new Access(new HashSet<>()));
  }

  @Override
  public @NotNull HashMap<@NotNull UUID, @NotNull Access> getAllDirty() {
    final HashMap<UUID, Access> dirtyEntries = new HashMap<>();

    for (final Map.Entry<UUID, Access> entry : AccessCache.CACHE.entrySet()) {
      final UUID uuid = entry.getKey();
      final Access access = entry.getValue();

      if (access.isDirty()) {
        dirtyEntries.put(uuid, AccessCache.CACHE.get(uuid));
      }
    }

    return dirtyEntries;
  }

  @Override
  public void put(final @NotNull UUID key, final @NotNull Access value) {
    AccessCache.CACHE.put(key, value);
  }

  @Override
  public void putAll(final @NotNull HashMap<@NotNull UUID, @NotNull Access> caches) {
    AccessCache.CACHE.putAll(caches);
  }

  @Override
  public void remove(final @NotNull UUID key, final @NotNull Access value) {
    AccessCache.CACHE.put(key, value);
  }

  @Override
  public void removeAll() {
    AccessCache.CACHE.clear();
  }
}
