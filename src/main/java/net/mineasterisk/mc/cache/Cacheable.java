package net.mineasterisk.mc.cache;

public class Cacheable {
  private boolean isDirty = false;

  public boolean isDirty() {
    return this.isDirty;
  }

  public void setDirty(final boolean isDirty) {
    this.isDirty = isDirty;
  }
}
