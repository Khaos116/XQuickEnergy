package io.github.lazyimmortal.sesame.util;

import org.json.JSONObject;

/**
 * Author:XX
 * Date:2024/12/2
 * Time:15:44
 */
public class LibraryUtil {
  private static native boolean libraryCheckFarmTaskStatus(JSONObject task);
  private static native boolean libraryDoFarmTask(JSONObject task);
  private static native boolean libraryDoFarmDrawTimesTask(JSONObject task);

  public static Boolean checkFarmTaskStatus(JSONObject task) {
    return libraryCheckFarmTaskStatus(task); // 注释此行，重写实现
  }

  public static Boolean doFarmTask(JSONObject task) {
    return libraryDoFarmTask(task);
  }

  public static Boolean doFarmDrawTimesTask(JSONObject task) {
    return libraryDoFarmDrawTimesTask(task);
  }
}
