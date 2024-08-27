package net.mineasterisk.mc.cache;

public class Cacheable {
  private boolean isDirty = false;

  public boolean isDirty() {
    return this.isDirty;
  }

  protected void setDirty() {
    this.isDirty = true;
  }
}
