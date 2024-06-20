package pansong291.xposed.quickenergy.util;

import android.app.Notification;
import android.app.NotificationManager;

import java.util.*;

import pansong291.xposed.quickenergy.data.ConfigV2;

/**
 * 在IDEA中添加@Getter注解后报红问题解决方案：需要在IDEA中安装lombok插件，并引入 lombok的依赖即可
 * Author:XX
 * Date:2024/6/20
 * Time:10:57
 */
public class MyChangeUtils {
  //取消通知栏折叠
  public static void innerSetContentText(
      Long nextExecTime,
      Notification.Builder builder,
      CharSequence contentText,
      NotificationManager mNotifyManager
  ) {
    String preContent = (nextExecTime > 0) ? "下次扫描" + TimeUtil.getTimeStr(nextExecTime) + "\n" : "";
    builder.setContentTitle("芝麻粒 " + preContent);
    builder.setContentText(contentText);
    if (mNotifyManager != null) {
      mNotifyManager.notify(NotificationUtil.NOTIFICATION_ID, builder.build());
    }
  }

  //设置时区为GMT+8
  public static Calendar getInstance() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 设置时区为东八区（北京时间）
    return calendar;
  }

  //关闭一键捉鸡
  public static void batchHireAnimalRecommend() {

  }

  //我自己的默认配置参数1
  public static void useMyConfig(Config c) {
    c.immediateEffect = true;//立即生效
    c.recordLog = true;//记录日志
    c.showToast = true;//气泡提示
    c.toastOffsetY = -200;
    c.stayAwake = true;//保持唤醒
    c.timeoutRestart = true;//超时重启
    c.startAt7 = true;//7点前启动
    c.enableOnGoing = true;//开启状态栏禁删
    c.languageSimplifiedChinese = true;//界面始终使用中文

    c.collectEnergy = true;//收集能量
    c.collectWateringBubble = true;//收金球
    c.batchRobEnergy = true;//一键收能量
    c.collectProp = true;//收集道具
    c.limitCollect = true;//限制收取
    c.limitCount = 50;//限制每分钟收取个数
    c.doubleCard = true;//使用双击卡
    c.helpFriendCollect = true;//复活好友能量
    c.receiveForestTaskAward = true;//收取森林任务奖励
    c.waterFriendCount = 66;//每次浇水克数
    c.cooperateWater = false;//合种
    c.ancientTree = false;//保护古树
    c.reserve = false;//每日兑换保护地
    c.beach = true;//保护海洋
    c.energyRain = true;//能量雨
    c.exchangeEnergyDoubleClick = false;//活力值兑换限时双击卡
    c.exchangeEnergyDoubleClickCount = 6;
    c.ancientTreeOnlyWeek = true;//仅星期一、三、五运行保护古树
    c.antdodoCollect = true;//神奇物种开卡
    c.antOcean = true;//神奇海洋
    c.userPatrol = true;//巡护森林
    c.animalConsumeProp = true;//派遣动物
    c.collectGiftBox = true;//领取礼盒
    c.totalCertCount = false;//记录证书总数

    c.enableFarm = true;//开启庄园
    c.rewardFriend = false;//打赏好友
    c.sendBackAnimal = false;//遣返小鸡
    c.receiveFarmToolReward = true;//收取道具奖励
    c.recordFarmGame = true;//游戏改分
    c.kitchen = true;//小鸡厨房
    c.useSpecialFood = true;//使用特殊食品
    c.useNewEggTool = false;//使用新蛋卡
    c.harvestProduce = true;//收获爱心鸡蛋
    c.donation = true;//捐赠爱心鸡蛋
    c.answerQuestion = true;//回答问题
    c.receiveFarmTaskAward = true;//收取饲料奖励
    c.feedAnimal = true;//喂小鸡
    c.useAccelerateTool = true;//使用加速卡
    c.farmGameTime = new ArrayList<>();//小鸡游戏时间
    c.farmGameTime.add("0830-1100");
    c.farmGameTime.add("2300-2400");
    c.animalSleepTime = new ArrayList<>();
    c.animalSleepTime.add("0200-0300");
    c.animalSleepTime.add("0400-0559");
    c.notifyFriend = false;//通知好友赶鸡
    c.acceptGift = true;//收麦子
    c.chickenDiary = true;//小鸡日记
    c.antOrchard = true;//农场
    c.receiveOrchardTaskAward = true;//收取农场任务奖励
    c.orchardSpreadManureCount = 1;//农场每日施肥次数

    c.enableStall = false;//新村
    c.stallAutoClose = false;//村自动收摊
    c.stallAutoOpen = false;//新村自动摆摊
    c.stallAutoTask = false;//新村自动任务
    c.stallReceiveAward = false;//新村自动领奖
    c.stallOpenType = true;//摊位类型(打开:摆摊列表/关闭:不摆列表)
    c.stallDonate = false;//新村自动捐赠
    c.stallInviteRegister = false;//邀请好友开通新村
    c.stallThrowManure = false;//新村丢肥料

    c.receivePoint = false;//积分
    c.openTreasureBox = false;//开启运动宝箱
    c.receiveCoinAsset = false;//收集运动币
    c.donateCharityCoin = false;//捐运动币
    c.kbSignIn = true;//口碑签到
    c.ecoLifeTick = true;//绿色行动打卡
    c.tiyubiz = true;//文体中心
    c.insBlueBeanExchange = false;//安心豆兑换时光加速器
    c.collectSesame = false;//收芝麻粒
    c.zcjSignIn = false;//招财金签到
    c.merchantKmdk = false;//商户开门打卡
    c.greenFinance = true;//开启绿色经营
    c.antBookRead = true;//读书听书
    c.consumeGold = false;//支付宝消费金
    c.omegakoiTown = false;//支小镇
  }

  //修改版本基础配置
  public static ConfigV2 useMyConfigV2(ConfigV2 c) {
    c.immediateEffect = true;//立即生效
    c.recordLog = true;//记录日志
    c.showToast = true;//气泡提示
    c.toastOffsetY = -200;//气泡纵向偏移
    c.stayAwake = true;//保持唤醒
    c.timeoutRestart = true;//超时重启
    c.startAt0 = true;//0点整执行
    c.startAt7 = true;//7点前启动
    c.enableOnGoing = true;//开启状态栏禁删
    c.batteryPerm = true;//为支付宝申请后台运行权限
    c.newRpc = true;//使用新接口
    c.debugMode = false;//开启抓包
    c.languageSimplifiedChinese = true;//界面始终使用中文
    return c;
  }

  public static Double fixAntOrchardParseDouble(String s) {
    try {
      return Double.parseDouble(s.replace("再施", ""));
    } catch (Exception e) {
      Log.record(e.getMessage());
      return 0.0;
    }
  }

  public static boolean fixCalendarHasNull(Calendar c1, Calendar c2, Calendar c3) {
    return c1 == null || c2 == null || c3 == null;
  }
}
