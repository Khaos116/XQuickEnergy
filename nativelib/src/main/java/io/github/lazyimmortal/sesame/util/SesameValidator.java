package io.github.lazyimmortal.sesame.util;

import android.content.Context;
import android.util.Log;

/**
 * Author:XX
 * Date:2024/10/2
 * Time:15:38
 */
public class SesameValidator {
  private static native String show(Context context);

  public static native boolean runBefore();

  public void test(Context context) {
    Log.e("真刀人", "show=" + show(context));
    Log.e("真刀人", "runBefore=" + runBefore());
  }
}
