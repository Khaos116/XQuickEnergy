package io.github.lazyimmortal.sesame.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import io.github.lazyimmortal.sesame.hook.ApplicationHook;

/**
 * Date:2024/12/3
 * Time:9:09
 */
public class MyUtils {
  private final static List<String> bizKeyList = Arrays.asList(
      "ADD_GONGGE_NEW",
      "USER_STARVE_PUSH",
      "YEB_PURCHASE",
      "WIDGET_addzujian",
      "HIRE_LOW_ACTIVITY",
      "DIANTAOHUANDUAN",
      "TAO_GOLDEN_V2",
      "SHANGYEHUA_90_1",
      "TAOBAO_tab2gzy",
      "YITAO_appgyg",
      "ANTFARM_chouchoule",
      "TB_qiandao2023",
      "BABAFARM_TB",
      "TB_chongzhi",
      "ALIPAIMAI_gygzy",
      "BABA_FARM_SPREAD_MANURE",
      "ELM_hudong2024",
      "2024XIANYU_huanduan",
      "JINGTAN_FEED_FISH",
      "UC_gygzy",
      "TAOBAO_renshenggyg",
      "SLEEP",
      "HEART_DONATION_ADVANCED_FOOD_V2",
      "xincun2023",
      "BBNC_gyg",
      "XJLYKBX1_sl90",
      "2023ZMF_gyg_v2",
      "FAMILY_COOPERATE_TASK",
      "TOUTIAO_daoduan",
      "KUAISHOU_DAODUAN2023",
      "ANTMEMBER_RICHANGQIANDAO",
      "HUABEI2023",
      "KUAISHOU_daoduanv2",
      "alty_leyoujigyg"
  );
  //用户id、昵称对应表
  public static final HashMap<String, String> mUidMap = new HashMap<>();
  private static SharedPreferences mSP = null;
  //修改参数，不让小鸡自动睡觉
  public static final String NO_SLEEP = "canSleepXXX";
  //关闭H5的新设置页面
  public static final boolean closeNewSetting = true;

  //是否显示首页弹窗
  public static boolean showHomeDialog() {
    return false;
  }

  //打印用户切换
  public static void recordUserName(@Nullable Context context, @Nullable String uid) {
    if (context == null) return;
    if (TextUtils.isEmpty(uid)) return;
    String name = mUidMap.get(uid);
    if (mSP == null) mSP = context.getSharedPreferences("XQE_UID", Context.MODE_PRIVATE);
    if (!TextUtils.isEmpty(name)) {
      mSP.edit().putString(uid, name).apply();//保存以便下次访问
      Log.record("加载用户:" + name);
    } else {
      String spName = mSP.getString(uid, "");
      if (TextUtils.isEmpty(spName)) {
        Log.record("加载用户ID:" + uid);
      } else {
        mUidMap.put(uid, spName);
        Log.record("加载用户:" + spName);
      }
    }
  }

  //首页显示全部记录，菜单显示其他记录
  public static boolean showHomeAllLog() {
    return true;
  }

  //处理可能为空
  public static @Nullable JSONArray antSportTaskListMaybeNull(JSONObject jo) {
    return jo.optJSONArray("taskList");
  }

  //处理可能为空
  public static @Nullable JSONArray antSportUserExchangeRecordsMaybeNull(JSONObject jo) {
    return jo.optJSONArray("userExchangeRecords");
  }

  //处理可能为空

  public static @Nullable JSONArray antFarmAnimalsMaybeNull(JSONObject jo) {
    return jo.optJSONArray("animals");
  }

  //处理可能为空
  public static String antFarmGroupIdMaybeEmpty(JSONObject jo) {
    return jo.optString("groupId");
  }

  //处理可能为空
  public static @Nullable JSONObject antFarmSleepNotifyInfoMaybeNull(JSONObject jo) {
    return jo.optJSONObject("sleepNotifyInfo");
  }

  //处理可能为空
  public static long antForestV2NowMaybeNull(JSONObject jo) {
    long time = jo.optLong("now");
    if (time == 0) {
      return System.currentTimeMillis();
    } else {
      return time;
    }
  }

  //发现有不支持的BizSubType
  public static boolean antMemberUnsSupportBizSubType(String subType) {
    if (TextUtils.isEmpty(subType)) return false;
    return !TextUtils.equals(subType, "ngfe_tag__ptr3o4eriu");
  }

  public static String encryptData(String data) {
    return data;
  }

  public static String decryptData(String data) {
    return data;
  }

  public static boolean libraryCheckFarmTaskStatus(JSONObject task) {
    return true;
  }

  /**
   * @noinspection CallToPrintStackTrace
   */
  //https://github.com/Fansirsqi/Sesame-TK/blob/main/app/src/main/java/fansirsqi/xposed/sesame/task/antFarm/AntFarm.java
  //private void doFarmDailyTask()
  public static boolean libraryDoFarmTask(JSONObject jo) {
    try {
      String title = jo.optString("title");
      String bizKey = jo.optString("bizKey");
      if ("VIEW".equals(jo.optString("taskMode")) || bizKeyList.contains(bizKey)) {
        jo = new JSONObject(doFarmTask(bizKey));
        if ("SUCCESS".equals(jo.optString("memo"))) {
          Log.farm("KT-庄园任务🧾[" + title + "]#获得饲料" + jo.optString("awardCount") + "g");
          return true;
        } else {
          Log.record(jo.optString("memo"));
          Log.i(jo.toString());
          return false;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      Log.printStackTrace("KT-DoFarmTask", e);
    }
    return false;
  }

  //https://github.com/Fansirsqi/Sesame-TK/blob/main/app/src/main/java/fansirsqi/xposed/sesame/task/antFarm/AntFarm.java
  //private void chouchoule()
  public static boolean libraryDoFarmDrawTimesTask(JSONObject job) {
    String title = job.optString("title");
    String taskId = job.optString("taskId");
    int rightsTimes = job.optInt("rightsTimes", 0);
    int rightsTimesLimit = job.optInt("rightsTimesLimit", 0);
    int times = rightsTimesLimit - rightsTimes;
    int sucCount = 0;
    try {
      for (int i = 0; i < times; i++) {
        String s = chouchouleDoFarmTask(taskId);
        JSONObject jo = new JSONObject(s);
        if (jo.optBoolean("success", false)) {
          Log.farm("KT-庄园小鸡🧾️[完成:抽抽乐" + title + "]");
          sucCount++;
        }
      }
      return sucCount == times;
    } catch (Exception e) {
      Log.i("KT-DoFarmDrawTimes", "chouchouleDoFarmTask err:");
      Log.printStackTrace("KT-DoFarmDrawTimes", e);
      return false;
    }
  }

  private static final String VERSION = "1.8.2302070202.46";

  //https://github.com/Fansirsqi/Sesame-TK/blob/main/app/src/main/java/fansirsqi/xposed/sesame/task/antFarm/AntFarmRpcCall.java
  private static String doFarmTask(String bizKey) {
    return ApplicationHook.requestString("com.alipay.antfarm.doFarmTask",
        "[{\"bizKey\":\"" + bizKey
            + "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
            + VERSION + "\"}]");
  }

  //https://github.com/Fansirsqi/Sesame-TK/blob/main/app/src/main/java/fansirsqi/xposed/sesame/task/antFarm/AntFarmRpcCall.java
  private static String chouchouleDoFarmTask(String bizKey) {
    return ApplicationHook.requestString("com.alipay.antfarm.doFarmTask",
        "[{\"bizKey\":\"" + bizKey + "\",\"requestType\":\"RPC\",\"sceneCode\":\"ANTFARM\",\"source\":\"chouchoule\",\"taskSceneCode\":\"ANTFARM_DRAW_TIMES_TASK\"}]");
  }
}
