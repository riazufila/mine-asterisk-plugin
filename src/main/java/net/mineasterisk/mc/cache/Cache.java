package net.mineasterisk.mc.cache;

import java.util.HashMap;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface Cache<T, U> {
  HashMap<@NotNull T, @NotNull U> getDirtyEntries();

  void putAll(@NotNull HashMap<@NotNull T, @NotNull U> cache);
}
