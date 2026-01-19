package io.github.lazyimmortal.sesame.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.*;

import io.github.lazyimmortal.sesame.hook.ApplicationHook;
import io.github.lazyimmortal.sesame.model.normal.base.BaseModel;

/*
 *  ⭐为了保障您的操作安全，请进行验证后继续：
 *    com.alipay.sportshealth.biz.rpc.SportsHealthCoinTaskRpc.queryCoinTaskPanel
 *    com.alipay.sportshealth.biz.rpc.sportsHealthHomeRpc.queryEnergyBubbleModule
 *    com.alipay.sportshealth.biz.rpc.SportsHealthCoinTaskRpc.completeTask
 *    com.alipay.antfarm.doFarmTask
 *  ⭐访问被拒绝：
 *    method: alipay.mobile.ipsponsorprod.consume.gold.task.signin.calendar
 *  ⭐人气太旺啦，请稍后再试：
 *    alipay.antmember.biz.rpc.membertask.h5.signPageTaskList
 *  ⭐反射了不存在的方法
 *    ERROR: AntSports, java.lang.NoSuchMethodError: com.alibaba.health.pedometer.intergation.rpc.RpcManager#a()
 *  ⭐当前网络不可用，请稍后重试
 *    alipay.antforest.forest.h5.queryTaskList
 *  ⭐广告请求错误 this is a cheating traffic   作弊流量
 *    com.alipay.adexchange.ad.facade.xlightPlugin
 */

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

  public static final boolean _关闭人气太旺 = true;
  public static final boolean _关闭不存在的方法调用 = true;
  public static final boolean _关闭首页弹窗 = true;
  public static final boolean _关闭必弹验证1 = true;
  public static final boolean _关闭必弹验证2 = true;
  public static final boolean _关闭必弹验证3 = true;
  public static final boolean _关闭必弹验证4 = true;
  public static final boolean _关闭作弊广告流量 = true;
  public static final boolean _关闭不支持RPC1 = true;
  public static final boolean _关闭不支持RPC2 = true;
  public static final String _访问被拒绝1 = "alipay.mrchservbase.mrchbusiness.sign.transcode.check_1";
  public static final String _访问被拒绝2 = "alipay.mobile.ipsponsorprod.consume.gold.task.signin.calendar_2";
  public static final String _访问被拒绝3 = "alipay.mobile.ipsponsorprod.consume.gold.task.signin.calendar_3";
  public static final String _系统出错正在排查1 = "alipay.mrchservbase.zcj.taskList.query.v2_1";

  //不想每次更新版本都去执行访问被拒绝和系统出错的访问，就关闭监测(第一次打开还是会执行)
  private static String get功能异常Key(@NonNull String key) {
    if (BaseModel.getNewModelCheckError().getValue()) {
      return key + "_" + ApplicationHook.getModelVersion();
    } else {
      return key;
    }
  }

  public static boolean getSp功能异常(@NonNull String key) {
    SharedPreferences sp = getMySp();
    if (sp == null) return true;
    return sp.getBoolean(get功能异常Key(key), false);
  }

  public static void setSp功能异常(@NonNull String key, JSONObject jo) {
    if (jo == null) return;
    //{"error":1009,"errorMessage":"访问被拒绝","errorNo":3,"errorTip":"1009"}
    //{"error":3000,"errorMessage":"系统出错，正在排查","errorNo":3,"errorTip":"3000"}
    String errorMessage = jo.optString("errorMessage", "");
    boolean isError = false;
    if (errorMessage.contains("访问被拒绝")) {
      isError = true;
    } else if (errorMessage.contains("系统出错")) {
      isError = true;
    }
    if (isError) {
      SharedPreferences sp = getMySp();
      if (sp != null) {
        sp.edit()
            .putBoolean(get功能异常Key(key), true)
            .apply();
      }
    }
  }

  //是否关闭验证(拼手速、派遣动物、能量雨、赠送道具、部分蚂蚁积分任务、消费金签到 -> 目前发现这些操作会触发验证)
  public static boolean closeVerification() {
    return BaseModel.getCloseVerification().getValue();
  }

  //关闭可能异常的功能：
  //黄金票 com.alipay.wealthgoldtwa.goldbill.v2.index.collect  系统出错，正在排查
  //文体中心走路 alipay.tiyubiz.wenti.walk.participate 系统出错，正在排查
  public static boolean closeErrorFunction() {
    return BaseModel.getCloseErrorFunction().getValue();
  }

  //关闭"不支持rpc完成的任务"
  public static boolean closeUnRpc() {
    return BaseModel.getCloseUnRPC().getValue();
  }

  private static @Nullable SharedPreferences getMySp() {
    Context context = ApplicationHook.getContext();
    if (context == null) return null;
    if (mSP == null) mSP = context.getSharedPreferences("XQE_UID", Context.MODE_PRIVATE);
    return mSP;
  }

  //打印用户切换
  public static String recordUserName(@Nullable String uid) {
    SharedPreferences sp = getMySp();
    if (sp == null) return "";
    if (TextUtils.isEmpty(uid)) return "";
    String name = mUidMap.get(uid);
    if (!TextUtils.isEmpty(name)) {
      sp.edit().putString(uid, name).apply();//保存以便下次访问
      return ":" + name;
    } else {
      String spName = sp.getString(uid, "");
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

  public static final String _OPT_TASKLIST = "taskList";
  public static final String _OPT_USER_EXCHANGE_RECORDS = "userExchangeRecords";
  public static final String _OPT_ANIMALS = "animals";
  public static final String _OPT_NOW = "now";
  public static final String _OPT_SLEEP_NOTIFY_INFO = "sleepNotifyInfo";
  public static final String _NO_SUPPORT_ANTFARM_CHOUCHOULECHOUKUAN = "_chouchoulechoukuan";
  public static final String _NO_SUPPORT_ANTFARM_TAO_GOLDEN_V2 = "TAO_GOLDEN_V2";
  public static final String _NO_SUPPORT_ANTMEMBER_NGFE_TAG__PTR3O4ERIU = "ngfe_tag__ptr3o4eriu";

  //加密
  public static String encryptData(String data) {
    return data;
  }

  //解密
  public static String decryptData(String data) {
    return data;
  }

  //APP名称后缀
  public static String getAppTitleExt(@Nullable Context context) {
    if (context == null) return "";
    if (context.getPackageName().startsWith("kt")) {
      return "GR";
    }
    return "";
  }

  public static Calendar getInstance() {
    return Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
  }

  /**
   * @noinspection CallToPrintStackTrace
   */
  //去除SO调用
  //https://github.com/Fansirsqi/Sesame-TK/blob/main/app/src/main/java/fansirsqi/xposed/sesame/task/antFarm/AntFarm.java
  //private void doFarmDailyTask()
  public static boolean libraryDoFarmTask(JSONObject jo) {
    try {
      String title = jo.optString("title", "");
      String bizKey = jo.optString("bizKey", "");
      String taskId = jo.optString("taskId", "");
      String taskMode = jo.optString("taskMode", "");
      String desc = jo.optString("desc", "");
      boolean canDoTask = TextUtils.equals("VIEW", taskMode);
      if (!canDoTask) canDoTask = TextUtils.equals("COUNT_DOWN", taskMode);
      if (!canDoTask) canDoTask = !TextUtils.isEmpty(taskId) && TextUtils.equals("TRIGGER", taskMode) && TextUtils.equals(taskId, bizKey);
      if (TextUtils.equals("ONLINE_PAY", bizKey) || TextUtils.equals("OFFLINE_PAY", bizKey)) {//线上和线下支付
        canDoTask = false;//不执行支付任务
      } else if (title.contains("付款") || title.contains("买") || desc.contains("付款") || (desc.contains("付") && desc.contains("元"))) {//额外判断支付
        canDoTask = false;//不执行支付任务
      } else if (bizKey.startsWith("HEART_DONAT")) {//2025-08-09 捐赠任务不让执行了
        canDoTask = false;//不执行捐赠任务
      } else if ((desc.contains("捐") && desc.contains("元")) || (desc.contains("捐") && desc.contains("金额"))) {//额外判断捐赠
        canDoTask = false;//不执行捐赠任务
      } else if (bizKey.toLowerCase().contains("xiadan") && !bizKey.equals("LSHS_xiadan_202509")) {
        canDoTask = false;//不执行下单任务
      }
      if (canDoTask) {
        jo = new JSONObject(doFarmTask(bizKey));
        if ("SUCCESS".equals(jo.optString("memo"))) {
          int awardCount = jo.optInt("awardCount");
          if (TextUtils.equals("HEART_DONATION_ADVANCED_FOOD_V2", bizKey)) {
            Log.farm("KT-庄园任务♥️[" + title + "]#获得爱心美食*" + awardCount);
          } else {
            Log.farm("KT-庄园任务🧾[" + title + "]#获得饲料" + jo.optString("awardCount", "0") + "g");
          }
          return true;
        } else {
          Log.record(jo.optString("memo"));
          Log.i(jo.toString());
          return false;
        }
      } else {
        Log.farm("KT-庄园任务🈲[" + title + "]，taskMode=" + taskMode + ",bizKey=" + bizKey);
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