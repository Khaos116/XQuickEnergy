package tkaxv7s.xposed.sesame.util;

import android.util.Pair;

import androidx.annotation.Nullable;

import java.util.*;

import tkaxv7s.xposed.sesame.model.task.antFarm.AntFarm;

/**
 * 重置配置，删除：/storage/emulated/0/Android/media/com.eg.android.AlipayGphone/sesame文件夹
 * 在IDEA中添加@Getter注解后报红问题解决方案：需要在IDEA中安装lombok插件，并引入 lombok的依赖即可
 * Author:XX
 * Date:2024/6/20
 * Time:10:57
 */
public class MyChangeUtils {
  //<editor-fold defaultstate="collapsed" desc="默认配置修改">
  private static final String sC176 = "2088702045701743";
  private static final String sC158 = "2088122949590991";
  private static final String sC199 = "2088432300394451";
  private static final String sC84 = "2088642642387040";
  private static final String sC886 = "2088732041012552";

  //修改默认模块开关
  public static boolean getDefaultModelSwitch(String modelName) {
    return modelName.contains("模块")
        || modelName.contains("森林")
        || modelName.contains("庄园")
        || modelName.contains("农场")
        || modelName.contains("运动")
        || modelName.contains("会员")
        || modelName.contains("答题");
  }

  //修改默认配置用户
  public static @Nullable Pair<LinkedHashMap<String, Integer>, Boolean> getDefaultSelectModelField(String code) {
    LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<>();
    Boolean open = null;
    switch (code) {
      //森林 AntForestV2
      case "waterFriendList"://浇水 | 好友列表
        linkedHashMap.put(MyChangeUtils.sC176, 3);
        linkedHashMap.put(MyChangeUtils.sC158, 3);
        open = true;
        break;
      case "giveEnergyRainList"://赠送能量雨列表
        linkedHashMap.put(MyChangeUtils.sC176, 0);
        linkedHashMap.put(MyChangeUtils.sC158, 0);
        linkedHashMap.put(MyChangeUtils.sC199, 0);
        linkedHashMap.put(MyChangeUtils.sC84, 0);
        linkedHashMap.put(MyChangeUtils.sC886, 0);
        open = false;
        break;
      //庄园 AntFarm
      case "feedFriendAnimalList"://喂好友小鸡列表
      case "visitFriendList"://送麦子名单
        linkedHashMap.put(MyChangeUtils.sC176, 3);
        linkedHashMap.put(MyChangeUtils.sC158, 3);
        open = true;
        break;
      default:
        break;
    }
    if (open == null) {
      return null;
    } else {
      return new Pair<>(linkedHashMap, open);
    }
  }

  //修改默认配置时间(当前GMT+7)
  public static List<String> getDefaultListModelField(String code) {
    List<String> newList = new ArrayList<>();
    switch (code) {
      case "farmGameTime"://小鸡游戏时间
        newList.add("0830-1100");
        newList.add("2300-2400");
        break;
      case "doubleCardTime"://双击卡 | 使用时间
        newList.add("0600-0630");
        break;
      case "execAtTimeList"://定时执行
        newList.add("055530");
        newList.add("2259");
        newList.add("23");
        break;
      case "wakenAtTimeList"://定时唤醒
        newList.add("0550");
        newList.add("2250");
        break;
      case "energyTime"://只收能量时间(范围)
        newList.add("0600-0631");
        break;
      default:
        break;
    }
    return newList;
  }

  //修改默认配置简单信息
  public static @Nullable Integer getDefaultIntegerModelField(String code) {
    Integer newValue = null;
    switch (code) {
      case "toastOffsetY"://气泡纵向偏移【Android API 30及其以上，Toast.setGravity无效】
        newValue = -200;
        break;
      case "orchardSpreadManureCount"://农场每日施肥次数
        newValue = 1;
        break;
      case "minExchangeCount"://最小捐步步数
        newValue = 19000;
        break;
      case "latestExchangeTime"://最晚捐步时间(24小时制)
        newValue = 20;
        break;
      default:
        break;
    }
    return newValue;
  }

  //修改默认配置简单信息
  public static @Nullable String getDefaultStringModelField(String code) {
    String newValue = null;
    switch (code) {
      case "wakeUpTime"://小鸡起床时间(关闭:-1)
        newValue = "0555";
        break;
      case "sleepTime"://小鸡睡觉时间(关闭:-1)
        newValue = "2359";
        break;
      default:
        break;
    }
    return newValue;
  }

  //捐蛋、召回等
  public static @Nullable Integer getDefaultChoiceModelField(String code) {
    Integer newValue = null;
    switch (code) {
      case "donationCount"://捐蛋 | 次数
        newValue = AntFarm.DonationCount.ONE;
        break;
      case "recallAnimalType"://召回小鸡
        newValue = AntFarm.RecallAnimalType.ALWAYS;
        break;
      default:
        break;
    }
    return newValue;
  }

  public static @Nullable Boolean getDefaultBooleanModelField(String code) {
    Boolean newValue = null;
    switch (code) {
      //【关闭的配置】
      //基础配置
      case "debugMode"://开启抓包
      case "batteryPerm"://为支付宝申请后台运行权限
        //古树 AncientTree
      case "ancientTreeOnlyWeek"://仅星期一、三、五运行保护古树
        //森林 AntForestV2
      case "exchangeEnergyDoubleClick"://活力值 | 兑换限时双击卡
      case "exchangeEnergyDoubleClickLongTime"://活力值 | 兑换永久双击卡
      case "totalCertCount"://记录证书总数
        //合种 AntCooperate
      case "cooperateWater"://合种浇水
        //庄园 AntFarm
      case "sendBackAnimal"://遣返 | 开启
      case "useNewEggTool"://使用新蛋卡
      case "notifyFriend"://赶鸡 | 通知好友
        //会员 AntMember
      case "collectSecurityFund"://芝麻粒坚持攒保障金(可开启持续做)
      case "promiseSportsRoute"://芝麻粒坚持锻炼，走运动路线(只自动加入任务)
      case "zcjSignIn"://招财金签到
      case "merchantKmdk"://商户开门打卡
        //农场 AntOrchard
      case "batchHireAnimal"://一键捉鸡除草
        //运动 AntSports
      case "battleForFriends"://抢好友大战
        //新村 AntStall
      case "stallAutoOpen"://新村自动摆摊
      case "stallAutoClose"://新村自动收摊
      case "stallAutoTicket"://新村自动贴罚单
      case "stallAutoTask"://新村自动任务
      case "stallReceiveAward"://新村自动领奖
      case "stallOpenType"://摊位类型(打开:摆摊列表/关闭:不摆列表)
      case "stallDonate"://新村自动捐赠
      case "stallInviteRegister"://邀请 | 邀请好友开通新村
      case "stallThrowManure"://新村丢肥料
        //绿色经营 GreenFinance
      case "greenFinanceLsxd"://打卡 | 绿色行动
      case "greenFinanceLscg"://打卡 | 绿色采购
      case "greenFinanceLsbg"://打卡 | 绿色办公
      case "greenFinanceWdxd"://打卡 | 绿色销售
      case "greenFinanceLswl"://打卡 | 绿色物流
      case "greenFinancePointFriend"://收取 | 好友金币
      case "greenFinanceDonation"://捐助 | 快过期金币
        newValue = false;
        break;
      //【开启的配置】
      //基础配置
      case "stayAwake"://保持唤醒
      case "timeoutRestart"://超时重启
      case "newRpc"://使用新接口
      case "recordLog"://记录日志
      case "showToast"://气泡提示
      case "languageSimplifiedChinese"://只显示中文并设置时区
      case "enableOnGoing"://开启状态栏禁删
        //森林 AntForestV2
      case "collectEnergy"://收集能量
      case "batchRobEnergy"://一键收取
      case "balanceNetworkDelay"://平衡网络延迟
      case "doubleCard"://双击卡 | 使用
      case "helpFriendCollect"://复活能量 | 开启
      case "helpFriendCollectType"://复活能量 | 动作(复活:开，不复活:关)",
      case "collectProp"://收集道具
      case "collectWateringBubble"://收金球
      case "energyRain"://能量雨
      case "animalConsumeProp"://派遣动物
      case "userPatrol"://巡护森林
      case "receiveForestTaskAward"://收取森林任务奖励
      case "antdodoCollect"://神奇物种开卡
      case "collectGiftBox"://领取礼盒
      case "medicalHealthFeeds"://健康医疗能量
      case "sendEnergyByAction"://森林集市
      case "ecoLifeTick"://绿色 | 行动打卡
      case "ecoLifeOpen"://绿色 | 自动开通
      case "photoGuangPan"://绿色 | 光盘行动
        //庄园 AntFarm
      case "rewardFriend"://打赏好友
      case "feedAnimal"://自动喂小鸡
      case "acceptGift"://收麦子
      case "donation"://捐蛋 | 开启
      case "hireAnimal"://雇佣小鸡 | 开启
      case "useAccelerateTool"://使用加速卡
      case "useSpecialFood"://使用特殊食品
      case "receiveFarmTaskAward"://收取饲料奖励
      case "receiveFarmToolReward"://收取道具奖励
      case "harvestProduce"://收获爱心鸡蛋
      case "kitchen"://小鸡厨房
      case "chickenDiary"://小鸡日记
      case "enableChouchoule"://开启小鸡抽抽乐
      case "listOrnaments"://小鸡每日换装
      case "enableDdrawGameCenterAward"://开宝箱
      case "answerQuestion"://开启答题
      case "recordFarmGame"://游戏改分
        //海洋 AntOcean
      case "protectOcean"://保护 | 开启
        //会员 AntMember
      case "memberSign"://会员签到
      case "collectSesame"://芝麻粒领取
      case "enableKb"://口碑签到
      case "enableGoldTicket"://黄金票签到
      case "enableGameCenter"://游戏中心签到
        //农场 AntOrchard
      case "receiveOrchardTaskAward"://收取农场任务奖励
        //运动 AntSports
      case "openTreasureBox"://开启宝箱
      case "receiveCoinAsset"://收运动币
      case "donateCharityCoin"://捐运动币
      case "tiyubiz"://文体中心
        //AI答题
      case "useGeminiAI"://GeminiAI | 使用答题
        newValue = true;
        break;
      default:
        break;
    }
    return newValue;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="解决看到的空指针">
  public static boolean fixCalendarHasNull(Calendar c1, Calendar c2, Calendar c3) {
    return c1 == null || c2 == null || c3 == null;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="由于已经将默认的时间配置都改为GMT+7了，所以不再调用这个方法">
  //设置时区为GMT+8
  public static Calendar getInstance() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 设置时区为东八区（北京时间）
    return calendar;
  }
  //</editor-fold>
}
