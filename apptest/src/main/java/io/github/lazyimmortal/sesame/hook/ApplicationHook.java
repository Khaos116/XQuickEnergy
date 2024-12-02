package io.github.lazyimmortal.sesame.hook;

/**
 * Author:XX
 * Date:2024/12/2
 * Time:15:57
 */
public class ApplicationHook {
  //抽抽乐任务列表com.alipay.antfarm.listFarmTask+ANTFARM_DRAW_TIMES_TASK
  //庄园任务列表com.alipay.antfarm.listFarmTask
  private final static String responseFarm = "{\"memo\":\"SUCCESS\",\"awardCount\":\"90\"}";
  //{"memo":"SUCCESS","awardCount":"90"}
  private final static String responseChouChouLe = "{\"success\":false}";
  //{"success":false}

  public static String requestString(String method, String data) {
    if (data.contains("ANTFARM_DRAW_TIMES_TASK")) {
      return responseChouChouLe;
    } else {
      return responseFarm;
    }
  }
}
