package io.github.lazyimmortal.sesame.model.task.antFarm;

import android.util.Log;

/**
 * Author:XX
 * Date:2024/10/2
 * Time:15:40
 */
public class AntFarm {
  private native void checkInnerAction(String str);

  public void test() {
    Log.e("懒真人", "打印InnerAction参数");
    checkInnerAction("这是InnerAction参数");
  }
}
