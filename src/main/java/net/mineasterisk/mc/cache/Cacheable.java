package net.mineasterisk.mc.cache;

public class Cacheable {
  private boolean dirty = false;

  public boolean isDirty() {
    return this.dirty;
  }

  protected void setDirty() {
    this.dirty = true;
  }
}
