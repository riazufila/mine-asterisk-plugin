package net.mineasterisk.mc.cache;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface Cache<T, U extends Cacheable> {
  @NotNull
  U get(final @NotNull T key);

  ConcurrentHashMap<@NotNull T, @NotNull U> getAll();

  HashMap<@NotNull T, @NotNull U> getAllDirty();

  void setDirty(final boolean isDirty, final @NotNull List<@NotNull T> dirtyEntries);

  void put(final @NotNull T key, final @NotNull U value);

  int putAll(@NotNull HashMap<@NotNull T, @NotNull U> cache);

  void remove(final @NotNull T key, final @NotNull U value);

  void removeAll();
}
