package io.github.lazyimmortal.sesame.util;

import android.util.Log;

/**
 * Author:XX
 * Date:2024/10/2
 * Time:15:37
 */
public class AESUtil {
  public static native String encryptData(String str);
  public static native String decryptData(String str);

  public void test() {
    Log.e("懒真人", "encryptData=" + encryptData("加密参数"));
    Log.e("懒真人", "decryptData=" + decryptData("解密参数"));
  }
}
