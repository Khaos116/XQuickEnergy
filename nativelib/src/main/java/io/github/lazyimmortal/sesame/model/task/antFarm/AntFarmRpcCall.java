package io.github.lazyimmortal.sesame.model.task.antFarm;

import android.util.Log;

/**
 * Author:XX
 * Date:2024/10/2
 * Time:15:41
 */
public class AntFarmRpcCall {
  private static native String getBizKeyByChouChouLe(String str);

  private static native String getBizKeyByFarm(String str);

  public void test() {
    Log.e("真刀人", "getBizKeyByChouChouLe=" + getBizKeyByChouChouLe("传入的抽抽乐参数"));
    Log.e("真刀人", "getBizKeyByFarm=" + getBizKeyByFarm("传入的农厂参数"));
  }
}
