package net.mineasterisk.mc.cache.access;

import java.util.concurrent.atomic.AtomicBoolean;
import net.mineasterisk.mc.util.CacheUtil;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class AccessCacheRunnable extends BukkitRunnable {
  private final @NotNull AtomicBoolean isRunning = new AtomicBoolean(false);

  @Override
  public void run() {
    if (!isRunning.get()) {
      CacheUtil.persist(true);
      isRunning.set(false);
    }
  }
}
