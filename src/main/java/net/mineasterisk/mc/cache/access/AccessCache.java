package net.mineasterisk.mc.cache.access;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.mineasterisk.mc.cache.Cache;
import org.jetbrains.annotations.NotNull;

public class AccessCache implements Cache<@NotNull UUID, @NotNull Access> {
  private static final @NotNull ConcurrentHashMap<@NotNull UUID, @NotNull Access> CACHE =
      new ConcurrentHashMap<>();

  @Override
  public @NotNull Access get(final @NotNull UUID key) {
    return AccessCache.CACHE.computeIfAbsent(key, k -> new Access(new HashSet<>()));
  }

  @Override
  public @NotNull ConcurrentHashMap<@NotNull UUID, @NotNull Access> getAll() {
    return AccessCache.CACHE;
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
  public void setDirty(
      final boolean isDirty, final @NotNull HashMap<@NotNull UUID, @NotNull Access> dirtyEntries) {
    for (final Map.Entry<UUID, Access> dirtyEntry : dirtyEntries.entrySet()) {
      final Access access = dirtyEntry.getValue();

      if (access.isDirty()) {
        access.setDirty(isDirty);
      }
    }
  }

  @Override
  public void put(final @NotNull UUID key, final @NotNull Access value) {
    AccessCache.CACHE.put(key, value);
  }

  @Override
  public void putAll(final @NotNull HashMap<@NotNull UUID, @NotNull Access> entries) {
    AccessCache.CACHE.putAll(entries);
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
