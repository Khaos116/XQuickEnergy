package io.github.lazyimmortal.sesame.model.task.antFarm;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.lazyimmortal.sesame.util.*;
import io.github.lazyimmortal.sesame.util.idMap.UserIdMap;

/**
 * https://github.com/acooler15/Sesame-TK/blob/develop/app/src/main/java/fansirsqi/xposed/sesame/task/antFarm/chouChouLe.java
 *
 * @author Byseven
 * @date 2025/1/30
 * @apiNote
 */
public class ChouChouLe {
  private static final String TAG = ChouChouLe.class.getSimpleName();

  public enum TaskStatus {
    TODO, FINISHED, RECEIVED, DONATION
  }

  /* 抽抽乐 */
  void chouchoule() {
    try {
      String s = AntFarmRpcCall.queryLoveCabin(UserIdMap.getCurrentUid());
      JSONObject jo = new JSONObject(s);
      if ("SUCCESS".equals(jo.getString("memo"))) {
        //boolean drawActivityOpen = jo.optBoolean("drawActivityOpen",false); //尚不清楚该值是表示所有抽抽乐还是IP抽抽乐
        jo = jo.optJSONObject("drawMachineInfo");
        if (jo == null) {
          Log.farm("抽抽乐🎁[获取抽抽乐活动信息失败]");
          return;
        }
        if (jo.has("dailyDrawMachineActivityId")) {
          doChouchoule("dailyDraw");
        }
        if (jo.has("ipDrawMachineActivityId")) {
          doChouchoule("ipDraw");
        }
      }
    } catch (Throwable t) {
      handleException("chouchoule err:", t);
    }
  }

  private void doChouchoule(String drawType) {
    boolean doubleCheck;
    do {
      doubleCheck = false;
      try {
        JSONObject jo = new JSONObject(AntFarmRpcCall.chouchouleListFarmTask(drawType));
        if (ResUtil.checkResultCode(TAG, jo)) {
          JSONArray farmTaskList = jo.getJSONArray("farmTaskList");
          for (int i = 0; i < farmTaskList.length(); i++) { // 遍历任务项
            JSONObject taskItem = farmTaskList.getJSONObject(i);
            String taskStatus = taskItem.getString("taskStatus");
            String title = taskItem.getString("title");
            String taskId = taskItem.getString("bizKey");
            String innerAction = taskItem.optString("innerAction");
            int rightsTimes = taskItem.optInt("rightsTimes", 0); // 已执行次数
            int rightsTimesLimit = taskItem.optInt("rightsTimesLimit", 0); // 总次数
            int additionalRightsTimes = rightsTimesLimit - rightsTimes;
            // Log.farm("抽抽乐🧾️[任务: " + title + "]" + " 状态: " + taskStatus + " 已执行: " + rightsTimes + "/" + rightsTimesLimit + " 剩余: " + additionalRightsTimes);
            if (TaskStatus.FINISHED.name().equals(taskStatus)) {
              if (receiveFarmTaskAward(drawType, taskId)) {
                doubleCheck = true;
              }
              if (rightsTimes < rightsTimesLimit) {
                performFarmTask(drawType, taskId, title);
              }
            } else if (TaskStatus.TODO.name().equals(taskStatus)) {
              if (performFarmTask(drawType, taskId, title)) {
                doubleCheck = true;
              }
            }
          }
        } else {
          if ("ipDraw".equals(drawType)) {
            Log.record(TAG, "IP抽抽乐任务列表获取失败: " + jo.getString("memo"));
          } else {
            Log.record(TAG, "抽抽乐任务列表获取失败: " + jo.getString("memo"));
          }
        }
      } catch (Throwable t) {
        handleException("chouchoule err:", t);
      }
    } while (doubleCheck);
    if ("ipDraw".equals(drawType)) {
      try {
        JSONObject jo = new JSONObject(AntFarmRpcCall.queryDrawMachineActivity());
        if (ResUtil.checkResultCode(TAG, jo)) {
          JSONObject drawMachineActivity = jo.getJSONObject("drawMachineActivity");
          long endTime = drawMachineActivity.getLong("endTime");
          if (System.currentTimeMillis() > endTime) {
            Log.record("该[" + drawMachineActivity.optString("activityId") + "]抽奖活动已结束");
            return;
          }
          int drawTimes = jo.optInt("drawTimes", 0);
          for (int ii = 0; ii < drawTimes; ii++) {
            JSONObject drawMachine = new JSONObject(AntFarmRpcCall.drawMachine());
            ThreadUtil.sleep(2000L);
            if (ResUtil.checkResultCode(TAG, drawMachine)) {
              JSONObject drawMachinePrize = drawMachine.optJSONObject("drawMachinePrize");
              if (drawMachinePrize != null) {
                String title = drawMachinePrize.getString("title");
                //int awardCount = drawMachinePrize.optInt("awardCount", 0);
                Log.farm("IP抽抽乐🎁[领取: " + title + "]");
              }
            } else {
              //{"ariverRpcTraceId":"21ba3bd317430576916613654ef50b","memo":"抽奖次数不足","resultCode":"DRAW_MACHINE01","success":false}
              if (MyUtils.isDrawTimesNotEnough(drawMachine)) {
                break;
              }
            }
          }
        }
      } catch (Throwable t) {
        handleException("drawMachine err:", t);
      }
    } else {
      try {
        JSONObject jo = new JSONObject(AntFarmRpcCall.enterDrawMachine());
        if (ResUtil.checkResultCode(TAG, jo)) {
          JSONObject userInfo = jo.getJSONObject("userInfo");
          JSONObject drawActivityInfo = jo.getJSONObject("drawActivityInfo");
          long endTime = drawActivityInfo.getLong("endTime");
          if (System.currentTimeMillis() > endTime) {
            Log.record("该[" + drawActivityInfo.optString("activityId") + "]抽奖活动已结束");
            return;
          }
          int leftDrawTimes = userInfo.optInt("leftDrawTimes", 0);
          String activityId = drawActivityInfo.optString("activityId", "null");
          for (int ii = 0; ii < leftDrawTimes; ii++) {
            JSONObject drawPrizeObj = new JSONObject(!activityId.equals("null") ? AntFarmRpcCall.DrawPrize(activityId) : AntFarmRpcCall.DrawPrize());
            ThreadUtil.sleep(2000L);
            if (drawPrizeObj.optBoolean("success")) {
              String title = drawPrizeObj.getString("title");
              int prizeNum = drawPrizeObj.optInt("prizeNum", 0);
              Log.farm("抽抽乐🎁[领取: " + title + "*" + prizeNum + "]");
            }
          }
        } else {
          Log.record(TAG, "抽奖活动进入失败: " + jo.getString("memo"));
        }
      } catch (Throwable t) {
        handleException("DrawPrize err:", t);
      }
    }
  }

  /**
   * 执行抽抽乐任务
   *
   * @param bizKey 业务ID
   * @param name   任务名称
   * @return 是否成功执行
   */
  private boolean performFarmTask(String drawType, String bizKey, String name) {
    try {
      ThreadUtil.sleep(15000L); // 所有等待15秒
      String s = AntFarmRpcCall.chouchouleDoFarmTask(drawType, bizKey);
      JSONObject jo = new JSONObject(s);
      if (jo.optBoolean("success", false)) {
        if ("ipDraw".equals(drawType)) {
          Log.farm("完成IP抽抽乐🧾️[任务: " + name + "]");
        } else {
          Log.farm("完成抽抽乐🧾️[任务: " + name + "]");
        }
        ThreadUtil.sleep(1000L);
        receiveFarmTaskAward(drawType, bizKey);
        return true;
      }
    } catch (Throwable t) {
      handleException("performFarmTask err:", t);
    }
    return false;
  }

  private boolean receiveFarmTaskAward(String drawType, String taskId) {
    try {
      String s = AntFarmRpcCall.chouchouleReceiveFarmTaskAward(drawType, taskId);
      JSONObject jo = new JSONObject(s);
      return ResUtil.checkResultCode(TAG, jo);
    } catch (Throwable t) {
      handleException("receiveFarmTaskAward err:", t);
    }
    return false;
  }

  private void handleException(String message, Throwable t) {
    Log.i(TAG, message);
    Log.printStackTrace(TAG, t);
  }
}