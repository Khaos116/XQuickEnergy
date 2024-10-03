package io.github.lazyimmortal.sesame.model.task.antForest;

import android.util.Log;

/**
 * Author:XX
 * Date:2024/10/2
 * Time:15:40
 */
public class AntForestRpcCall {
  private static native boolean initOver();

  public void test() {
    Log.e("懒真人", "initOver=" + initOver());
  }
}
