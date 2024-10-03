package io.github.lazyimmortal.sesame.hook;

import android.util.Log;

/**
 * Author:XX
 * Date:2024/10/2
 * Time:15:39
 */
public class ApplicationHook {
  static {
    System.loadLibrary("sesame");
  }

  private native boolean isRun();

  public void test() {
    Log.e("懒真人", "isRun=" + isRun());
  }
}
