package net.mineasterisk.mc.cache;

import java.util.HashMap;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface Cache<T, U> {
  @NotNull
  U get(final @NotNull T key);

  HashMap<@NotNull T, @NotNull U> getAll();

  HashMap<@NotNull T, @NotNull U> getAllDirty();

  void put(final @NotNull T key, final @NotNull U value);

  void putAll(@NotNull HashMap<@NotNull T, @NotNull U> cache);

  void remove(final @NotNull T key, final @NotNull U value);

  void removeAll();
}
