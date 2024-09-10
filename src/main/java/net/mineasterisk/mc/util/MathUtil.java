package net.mineasterisk.mc.util;

import java.util.Random;

public class MathUtil {
  public static double getRandomDouble(double minimum, double maximum) {
    return minimum + (maximum - minimum) * new Random().nextDouble();
  }
}
