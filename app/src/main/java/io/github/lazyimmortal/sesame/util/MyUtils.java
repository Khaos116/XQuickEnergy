package io.github.lazyimmortal.sesame.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import io.github.lazyimmortal.sesame.hook.ApplicationHook;
import io.github.lazyimmortal.sesame.model.normal.base.BaseModel;

/**
 * Date:2024/12/3
 * Time:9:09
 */
public class MyUtils {
  //接口调用版本
  private static final String VERSION = "1.8.2302070202.46";
  //用户id、昵称对应表
  public static final HashMap<String, String> mUidMap = new HashMap<>();
  private static SharedPreferences mSP = null;
  //修改参数，不让小鸡自动睡觉
  public static final String NO_SLEEP = "canSleepXXX";

  //是否显示首页弹窗
  public static boolean showHomeDialog() {
    return false;
  }

  //是否关闭验证(拼手速、派遣动物、能量雨、赠送道具、部分蚂蚁积分任务、消费金签到 -> 目前发现这些操作会触发验证)
  public static boolean closeVerification() {
    return BaseModel.getCloseVerification().getValue();
  }

  //关闭可能异常的功能：
  //com.alipay.wealthgoldtwa.goldbill.v2.index.collect  系统出错，正在排查
  public static boolean closeErrorFunction() {
    return BaseModel.getCloseErrorFunction().getValue();
  }

  //打印用户切换
  public static String recordUserName(@Nullable Context context, @Nullable String uid) {
    if (context == null) return "";
    if (TextUtils.isEmpty(uid)) return "";
    String name = mUidMap.get(uid);
    if (mSP == null) mSP = context.getSharedPreferences("XQE_UID", Context.MODE_PRIVATE);
    if (!TextUtils.isEmpty(name)) {
      mSP.edit().putString(uid, name).apply();//保存以便下次访问
      return ":" + name;
    } else {
      String spName = mSP.getString(uid, "");
      if (TextUtils.isEmpty(spName)) {
        return ":" + uid;
      } else {
        mUidMap.put(uid, spName);
        return ":" + spName;
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
  public static boolean antMemberSupportBizSubType(String subType) {
    if (TextUtils.isEmpty(subType)) return false;
    return !TextUtils.equals(subType, "ngfe_tag__ptr3o4eriu");
  }

  //发现有不支持的BizKey
  public static boolean antFarmSupportBizKey(String bizKey) {
    if (TextUtils.isEmpty(bizKey)) return false;
    return !bizKey.contains("_chouchoulechoukuan");
  }

  //加密
  public static String encryptData(String data) {
    return data;
  }

  //解密
  public static String decryptData(String data) {
    return data;
  }

  //去除SO调用
  public static boolean libraryCheckFarmTaskStatus(JSONObject task) {
    return true;
  }

  //APP名称后缀
  public static String getAppTitleExt(@Nullable Context context) {
    if (context == null) return "";
    if (context.getPackageName().startsWith("kt")) {
      return "Lazy";
    }
    return "";
  }

  /**
   * @noinspection CallToPrintStackTrace
   */
  //去除SO调用
  //https://github.com/Fansirsqi/Sesame-TK/blob/main/app/src/main/java/fansirsqi/xposed/sesame/task/antFarm/AntFarm.java
  //private void doFarmDailyTask()
  public static boolean libraryDoFarmTask(JSONObject jo) {
    try {
      String title = jo.optString("title");
      String bizKey = jo.optString("bizKey");
      String taskId = jo.optString("taskId");
      String taskMode = jo.optString("taskMode");
      boolean canDoTask = TextUtils.equals("VIEW", taskMode);
      if (!canDoTask) canDoTask = !TextUtils.isEmpty(taskId) && TextUtils.equals("TRIGGER", taskMode) && TextUtils.equals(taskId, bizKey);
      if (TextUtils.equals("ONLINE_PAY", bizKey)) {
        canDoTask = false;//不执行支付任务
      }
      if (canDoTask) {
        jo = new JSONObject(doFarmTask(bizKey));
        if ("SUCCESS".equals(jo.optString("memo"))) {
          int awardCount = jo.optInt("awardCount");
          if (TextUtils.equals("HEART_DONATION_ADVANCED_FOOD_V2", bizKey)) {
            Log.farm("KT-庄园任务♥️[" + title + "]#获得爱心美食*" + awardCount);
          } else {
            Log.farm("KT-庄园任务🧾[" + title + "]#获得饲料" + jo.optString("awardCount") + "g");
          }
          return true;
        } else {
          Log.record(jo.optString("memo"));
          Log.i(jo.toString());
          return false;
        }
      } else {
        Log.farm("KT-庄园任务🈲[" + title + "]");
      }
    } catch (Exception e) {
      e.printStackTrace();
      Log.printStackTrace("KT-DoFarmTask", e);
    }
    return false;
  }

  //去除SO调用
  //https://github.com/Fansirsqi/Sesame-TK/blob/main/app/src/main/java/fansirsqi/xposed/sesame/task/antFarm/AntFarm.java
  //private void chouchoule()
  public static boolean libraryDoFarmDrawTimesTask(JSONObject job) {
    String title = job.optString("title");
    String bizKey = job.optString("bizKey");
    int rightsTimes = job.optInt("rightsTimes", 0);
    int rightsTimesLimit = job.optInt("rightsTimesLimit", 0);
    int times = rightsTimesLimit - rightsTimes;
    int sucCount = 0;
    try {
      for (int i = 0; i < times; i++) {
        String s = chouchouleDoFarmTask(bizKey);
        JSONObject jo = new JSONObject(s);
        if (jo.optBoolean("success", false)) {
          sucCount++;
          Log.farm("KT-庄园小鸡🧾️[完成:抽抽乐" + title + "]*" + sucCount);
        }
      }
      return sucCount == times;
    } catch (Exception e) {
      Log.i("KT-DoFarmDrawTimes", "chouchouleDoFarmTask err:");
      Log.printStackTrace("KT-DoFarmDrawTimes", e);
      return false;
    }
  }

  //https://github.com/Fansirsqi/Sesame-TK/blob/main/app/src/main/java/fansirsqi/xposed/sesame/task/antFarm/AntFarmRpcCall.java
  //农场任务
  private static String doFarmTask(String bizKey) {
    return ApplicationHook.requestString("com.alipay.antfarm.doFarmTask",
        "[{\"bizKey\":\"" + bizKey
            + "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"version\":\""
            + VERSION + "\"}]");
  }

  //https://github.com/Fansirsqi/Sesame-TK/blob/main/app/src/main/java/fansirsqi/xposed/sesame/task/antFarm/AntFarmRpcCall.java
  //抽抽乐任务
  private static String chouchouleDoFarmTask(String bizKey) {
    return ApplicationHook.requestString("com.alipay.antfarm.doFarmTask",
        "[{\"bizKey\":\"" + bizKey + "\",\"requestType\":\"RPC\",\"sceneCode\":\"ANTFARM\",\"source\":\"chouchoule\",\"taskSceneCode\":\"ANTFARM_DRAW_TIMES_TASK\"}]");
  }

  //是否抽奖次数不足
  public static boolean isDrawTimesNotEnough(JSONObject drawMachine) {
    if (drawMachine == null) return false;
    String resultCode = drawMachine.optString("resultCode");
    String memo = drawMachine.optString("memo");
    boolean success = drawMachine.optBoolean("success");
    if (!success && TextUtils.equals("DRAW_MACHINE01", resultCode)) {
      Log.record("IP抽抽乐抽奖失败1:" + memo);
      return true;
    } else if (!success) {
      Log.record("IP抽抽乐抽奖失败2:" + memo);
    }
    return false;
  }
}

//庄园任务
//bizKey taskId taskMode title
//SHH_xiaoxuefuli  SHH_xiaoxuefuli VIEW  小鸡送「大雪福利」啦
//HUABEI2023  HUABEI2023 VIEW  逛逛花呗花花卡
//ANTMEMBER_RICHANGQIANDAO  ANTMEMBER_RICHANGQIANDAO VIEW  去支付宝会员签到
//ZFByundong  ZFByundong VIEW  去支付宝运动逛一逛
//TAOTEapp202304  TAOTEapp202304 VIEW  逛一逛淘宝特价版
//⭐⭐⭐HEART_DONATE  xxx TRIGGER  爱心捐赠
//⭐⭐⭐OFFLINE_PAY  xxx TRIGGER  到店付款
//⭐⭐⭐ONLINE_PAY  xxx TRIGGER  线上支付
//⭐⭐⭐ANTFARM_P2P  30001229221356342088702045701743 TRIGGER  一起拿小鸡饲料
//🔴🔴🔴COOK  COOK TRIGGER  小鸡厨房
//🔴🔴🔴ANSWER  30001935487934202088702045701743 TRIGGER  庄园小课堂
//🔴🔴🔴VIDEO_TASK  VIDEO_TASK TRIGGER  庄园小视频
//ANTFARM_chouchoule  ANTFARM_chouchoule TRIGGER  【抽抽乐】冬日惊喜装扮来啦
//FAMILY_COOPERATE_TASK  FAMILY_COOPERATE_TASK TRIGGER  家庭今日达2人贡献亲密度
//TAOBAO_tab2gzy  TAOBAO_tab2gzy TRIGGER  去逛一逛淘宝视频
//2024XIANYU_huanduan  2024XIANYU_huanduan TRIGGER  去闲鱼逛一逛
//JINGTAN_FEED_FISH  JINGTAN_FEED_FISH TRIGGER  去鲸探喂鱼集福气
//SHANGYEHUA_90_1  SHANGYEHUA_90_1 TRIGGER  去杂货铺逛一逛
//HEART_DONATION_ADVANCED_FOOD_V2  HEART_DONATION_ADVANCED_FOOD_V2 TRIGGER  栗子抱抱卷任务
//HIRE_LOW_ACTIVITY  HIRE_LOW_ACTIVITY TRIGGER  雇佣小鸡拿饲料
//SLEEP  SLEEP TRIGGER  让小鸡去睡觉
//XJLYKBX1_sl90  XJLYKBX1_sl90 TRIGGER  去小鸡乐园开1次宝箱
//UC_gygzy  UC_gygzy TRIGGER  逛一逛UC浏览器
//TOUTIAO_daoduan  TOUTIAO_daoduan TRIGGER  去今日头条极速版逛一逛
//TB_qiandao2023  TB_qiandao2023 TRIGGER  去淘宝签到逛一逛
//BABAFARM_TB  BABAFARM_TB TRIGGER  去逛一逛淘宝芭芭农场
//ELM_hudong2024  ELM_hudong2024 TRIGGER  去饿了么农场逛一逛