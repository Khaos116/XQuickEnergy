package io.github.lazyimmortal.sesame.util;

public class Log {
  public static void other(String s) {
    android.util.Log.e("懒真人LogE", "懒真人:" + s);
  }

  public static void record(String s) {
    android.util.Log.i("懒真人LogI", "懒真人:" + s);
  }
}
