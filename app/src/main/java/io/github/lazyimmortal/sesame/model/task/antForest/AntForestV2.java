package io.github.lazyimmortal.sesame.model.task.antForest;

import org.json.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XposedHelpers;
import io.github.lazyimmortal.sesame.data.*;
import io.github.lazyimmortal.sesame.data.modelFieldExt.*;
import io.github.lazyimmortal.sesame.data.task.ModelTask;
import io.github.lazyimmortal.sesame.entity.*;
import io.github.lazyimmortal.sesame.hook.ApplicationHook;
import io.github.lazyimmortal.sesame.hook.Toast;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.model.extensions.ExtensionsHandle;
import io.github.lazyimmortal.sesame.model.normal.base.BaseModel;
import io.github.lazyimmortal.sesame.model.task.antFarm.AntFarm.TaskStatus;
import io.github.lazyimmortal.sesame.rpc.intervallimit.FixedOrRangeIntervalLimit;
import io.github.lazyimmortal.sesame.rpc.intervallimit.RpcIntervalLimit;
import io.github.lazyimmortal.sesame.ui.ObjReference;
import io.github.lazyimmortal.sesame.util.*;
import io.github.lazyimmortal.sesame.util.idMap.UserIdMap;
import io.github.lazyimmortal.sesame.util.idMap.VitalityBenefitIdMap;
import lombok.Getter;

/**
 * 蚂蚁森林V2
 */
public class AntForestV2 extends ModelTask {

  private static final String TAG = AntForestV2.class.getSimpleName();

  private static final AverageMath offsetTimeMath = new AverageMath(5);

  private static final Map<String, Long> usingProps = new ConcurrentHashMap<>();

  private static final Map<String, String> dressMap;

  private static final Set<String> AntForestTaskTypeSet;

  static {
    dressMap = new HashMap<>();
    // position To positionType
    dressMap.put("tree__main", "treeMain");
    dressMap.put("bg__sky_0", "bgSky0");
    dressMap.put("bg__sky_cloud", "bgSkyCloud");
    dressMap.put("bg__ground_a", "bgGroundA");
    dressMap.put("bg__ground_b", "bgGroundB");
    dressMap.put("bg__ground_c", "bgGroundC");
    // positionType To position
    dressMap.put("treeMain", "tree__main");
    dressMap.put("bgSky0", "bg__sky_0");
    dressMap.put("bgSkyCloud", "bg__sky_cloud");
    dressMap.put("bgGroundA", "bg__ground_a");
    dressMap.put("bgGroundB", "bg__ground_b");
    dressMap.put("bgGroundC", "bg__ground_c");

    AntForestTaskTypeSet = new HashSet<>();
    AntForestTaskTypeSet.add("VITALITYQIANDAOPUSH"); //
    AntForestTaskTypeSet.add("ONE_CLICK_WATERING_V1");// 给随机好友一键浇水
    AntForestTaskTypeSet.add("GYG_YUEDU_2");// 去森林图书馆逛15s
    AntForestTaskTypeSet.add("GYG_TBRS");// 逛一逛淘宝人生
    AntForestTaskTypeSet.add("TAOBAO_tab2_2023");// 去淘宝看科普视频
    AntForestTaskTypeSet.add("GYG_diantao");// 逛一逛点淘得红包
    AntForestTaskTypeSet.add("GYG-taote");// 逛一逛淘宝特价版
    AntForestTaskTypeSet.add("NONGCHANG_20230818");// 逛一逛淘宝芭芭农场
    // AntForestTaskTypeSet.add("GYG_haoyangmao_20240103");//逛一逛淘宝薅羊毛
    // AntForestTaskTypeSet.add("YAOYIYAO_0815");//去淘宝摇一摇领奖励
    // AntForestTaskTypeSet.add("GYG-TAOCAICAI");//逛一逛淘宝买菜
  }

  private final AtomicInteger taskCount = new AtomicInteger(0);

  private String selfId;

  private Integer tryCountInt;

  private Integer retryIntervalInt;

  private Integer advanceTimeInt;

  private Integer checkIntervalInt;

  private FixedOrRangeIntervalLimit collectIntervalEntity;

  private FixedOrRangeIntervalLimit doubleCollectIntervalEntity;

  private final AverageMath delayTimeMath = new AverageMath(5);

  private final ObjReference<Long> collectEnergyLockLimit = new ObjReference<>(0L);

  private final Object usePropLockObj = new Object();

  private BooleanModelField collectEnergy;
  private BooleanModelField expiredEnergy;
  private BooleanModelField energyRain;
  private IntegerModelField advanceTime;
  private IntegerModelField tryCount;
  private IntegerModelField retryInterval;
  private SelectModelField dontCollectList;
  private BooleanModelField collectWateringBubble;
  private BooleanModelField batchRobEnergy;
  private BooleanModelField balanceNetworkDelay;
  private BooleanModelField closeWhackMole;
  private BooleanModelField collectProp;
  private StringModelField queryInterval;
  private StringModelField collectInterval;
  private StringModelField doubleCollectInterval;
  private ChoiceModelField doubleClickType;
  private ListModelField.ListJoinCommaToStringModelField doubleCardTime;
  @Getter
  private IntegerModelField doubleCountLimit;
  private BooleanModelField doubleCardConstant;
  private ChoiceModelField helpFriendCollectType;
  private SelectModelField helpFriendCollectList;
  private IntegerModelField returnWater33;
  private IntegerModelField returnWater18;
  private IntegerModelField returnWater10;
  private BooleanModelField receiveForestTaskAward;
  private BooleanModelField forestChouChouLe;
  private ChoiceModelField waterFriendType;
  private SelectAndCountModelField waterFriendList;
  private SelectModelField giveEnergyRainList;
  private BooleanModelField vitalityExchangeBenefit;
  private SelectAndCountModelField vitalityExchangeBenefitList;
  private BooleanModelField userPatrol;
  private BooleanModelField collectGiftBox;
  private BooleanModelField medicalHealth;
  private BooleanModelField greenLife;
  private BooleanModelField combineAnimalPiece;
  private ChoiceModelField consumeAnimalPropType;
  private SelectModelField whoYouWantToGiveTo;
  private BooleanModelField ecoLife;
  private SelectModelField ecoLifeOptions;
  private BooleanModelField dress;
  private TextModelField dressDetailList;

  private static int totalCollected = 0;
  private static int totalHelpCollected = 0;
  private static boolean hasErrorWait = false;

  @Getter
  private Set<String> dontCollectMap = new HashSet<>();

  @Override
  public String getName() {
    return "森林";
  }

  @Override
  public ModelGroup getGroup() {
    return ModelGroup.FOREST;
  }

  @Override
  public ModelFields getFields() {
    ModelFields modelFields = new ModelFields();
    modelFields.addField(collectEnergy = new BooleanModelField("collectEnergy", "收集能量", false));
    modelFields.addField(batchRobEnergy = new BooleanModelField("batchRobEnergy", "一键收取", false));
    modelFields.addField(collectWateringBubble = new BooleanModelField("collectWateringBubble", "收取金球", false));
    modelFields.addField(expiredEnergy = new BooleanModelField("expiredEnergy", "收取过期能量", false));
    modelFields.addField(queryInterval = new StringModelField("queryInterval", "查询间隔(毫秒或毫秒范围)", "500-1000"));
    modelFields.addField(collectInterval = new StringModelField("collectInterval", "收取间隔(毫秒或毫秒范围)", "1000-1500"));
    modelFields.addField(doubleCollectInterval = new StringModelField("doubleCollectInterval", "双击间隔(毫秒或毫秒范围)", "50-150"));
    modelFields.addField(balanceNetworkDelay = new BooleanModelField("balanceNetworkDelay", "平衡网络延迟", true));
    modelFields.addField(advanceTime = new IntegerModelField("advanceTime", "提前时间(毫秒)", 0, Integer.MIN_VALUE, 500));
    modelFields.addField(tryCount = new IntegerModelField("tryCount", "尝试收取(次数)", 1, 0, 10));
    modelFields.addField(retryInterval = new IntegerModelField("retryInterval", "重试间隔(毫秒)", 1000, 0, 10000));
    modelFields.addField(dontCollectList = new SelectModelField("dontCollectList", "不收取能量列表", new LinkedHashSet<>(), AlipayUser::getList));
    modelFields.addField(doubleClickType = new ChoiceModelField("doubleClickType", "双击卡 | 自动使用", UsePropType.CLOSE, UsePropType.nickNames));
    modelFields.addField(doubleCountLimit = new IntegerModelField("doubleCountLimit", "双击卡 | 使用次数", 6));
    modelFields.addField(doubleCardTime = new ListModelField.ListJoinCommaToStringModelField("doubleCardTime", "双击卡 | 使用时间(范围)", ListUtil.newArrayList("0700-0730")));
    modelFields.addField(doubleCardConstant = new BooleanModelField("DoubleCardConstant", "双击卡 | 限时双击永动机", false));
    modelFields.addField(returnWater10 = new IntegerModelField("returnWater10", "返水 | 10克需收能量(关闭:0)", 0));
    modelFields.addField(returnWater18 = new IntegerModelField("returnWater18", "返水 | 18克需收能量(关闭:0)", 0));
    modelFields.addField(returnWater33 = new IntegerModelField("returnWater33", "返水 | 33克需收能量(关闭:0)", 0));
    modelFields.addField(waterFriendType = new ChoiceModelField("waterFriendType", "浇水 | 动作", WaterFriendType.WATER_00, WaterFriendType.nickNames));
    modelFields.addField(waterFriendList = new SelectAndCountModelField("waterFriendList", "浇水 | 好友列表", new LinkedHashMap<>(), AlipayUser::getList, "请填写浇水次数(每日)"));
    modelFields.addField(helpFriendCollectType = new ChoiceModelField("helpFriendCollectType", "复活能量 | 动作", HelpFriendCollectType.NONE, HelpFriendCollectType.nickNames));
    modelFields.addField(helpFriendCollectList = new SelectModelField("helpFriendCollectList", "复活能量 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
    modelFields.addField(vitalityExchangeBenefit = new BooleanModelField("vitalityExchangeBenefit", "活力值 | 兑换权益", false));
    modelFields.addField(vitalityExchangeBenefitList = new SelectAndCountModelField("vitalityExchangeBenefitList", "活力值 | 权益列表", new LinkedHashMap<>(), VitalityBenefit::getList, "请填写兑换次数(每日)"));
    modelFields.addField(closeWhackMole = new BooleanModelField("closeWhackMole", "自动关闭6秒拼手速", true));
    modelFields.addField(collectProp = new BooleanModelField("collectProp", "收集道具", false));
    modelFields.addField(whoYouWantToGiveTo = new SelectModelField("whoYouWantToGiveTo", "赠送道具好友列表", new LinkedHashSet<>(), AlipayUser::getList, "会赠送所有可送道具都给已选择的好友"));
    modelFields.addField(energyRain = new BooleanModelField("energyRain", "收集能量雨", false));
    modelFields.addField(giveEnergyRainList = new SelectModelField("giveEnergyRainList", "赠送能量雨好友列表", new LinkedHashSet<>(), AlipayUser::getList));
    modelFields.addField(userPatrol = new BooleanModelField("userPatrol", "保护地巡护", false));
    modelFields.addField(combineAnimalPiece = new BooleanModelField("combineAnimalPiece", "合成动物碎片", false));
    modelFields.addField(consumeAnimalPropType = new ChoiceModelField("consumeAnimalPropType", "派遣动物伙伴", ConsumeAnimalPropType.NONE, ConsumeAnimalPropType.nickNames));
    modelFields.addField(receiveForestTaskAward = new BooleanModelField("receiveForestTaskAward", "森林任务", false));
    modelFields.addField(forestChouChouLe = new BooleanModelField("forestChouChouLe", "森林寻宝任务", false));
    modelFields.addField(collectGiftBox = new BooleanModelField("collectGiftBox", "领取礼盒", false));
    modelFields.addField(medicalHealth = new BooleanModelField("medicalHealth", "医疗健康", false));
    modelFields.addField(greenLife = new BooleanModelField("greenLife", "森林集市", false));
    modelFields.addField(ecoLife = new BooleanModelField("ecoLife", "绿色行动 | 开启", false));
    modelFields.addField(ecoLifeOptions = new SelectModelField("ecoLifeOptions", "绿色行动 | 选项", new LinkedHashSet<>(), CustomOption::getEcoLifeOptions, "光盘行动需要先手动完成一次"));
    modelFields.addField(dress = new BooleanModelField("dress", "装扮保护 | 开启", false));
    modelFields.addField(dressDetailList = new TextModelField("dressDetailList", "装扮保护 | 装扮信息", ""));
    modelFields.addField(new EmptyModelField("dressDetailListClear", "装扮保护 | 装扮信息清除", () -> dressDetailList.reset()));
    return modelFields;
  }

  @Override
  public Boolean check() {
    if (RuntimeInfo.getInstance().getLong(RuntimeInfo.RuntimeInfoKey.ForestPauseTime) > System.currentTimeMillis()) {
      Log.record("异常等待中，暂不执行检测！");
      return false;
    }
    return true;
  }

  @Override
  public Boolean isSync() {
    return true;
  }

  @Override
  public void boot(ClassLoader classLoader) {
    super.boot(classLoader);
    FixedOrRangeIntervalLimit queryIntervalLimit = new FixedOrRangeIntervalLimit(queryInterval.getValue(), 10, 10000);
    RpcIntervalLimit.addIntervalLimit("alipay.antforest.forest.h5.queryHomePage", queryIntervalLimit);
    RpcIntervalLimit.addIntervalLimit("alipay.antforest.forest.h5.queryFriendHomePage", queryIntervalLimit);
    RpcIntervalLimit.addIntervalLimit("alipay.antmember.forest.h5.collectEnergy", 0);
    RpcIntervalLimit.addIntervalLimit("alipay.antmember.forest.h5.queryEnergyRanking", 100);
    RpcIntervalLimit.addIntervalLimit("alipay.antforest.forest.h5.fillUserRobFlag", 500);
    tryCountInt = tryCount.getValue();
    retryIntervalInt = retryInterval.getValue();
    advanceTimeInt = advanceTime.getValue();
    checkIntervalInt = BaseModel.getCheckInterval().getValue();
    dontCollectMap = dontCollectList.getValue();
    collectIntervalEntity = new FixedOrRangeIntervalLimit(collectInterval.getValue(), 50, 10000);
    doubleCollectIntervalEntity = new FixedOrRangeIntervalLimit(doubleCollectInterval.getValue(), 10, 5000);
    delayTimeMath.clear();
    AntForestRpcCall.init();
  }

  @Override
  public void run() {
    try {
      Log.record("执行开始-蚂蚁森林");
      NotificationUtil.setStatusTextExec();

      taskCount.set(0);
      selfId = UserIdMap.getCurrentUid();
      hasErrorWait = false;

      JSONObject selfHomeObject = collectSelfEnergy();
      try {
        JSONObject friendsObject = new JSONObject(AntForestRpcCall.queryEnergyRanking());
        if (MessageUtil.checkResultCode(TAG, friendsObject)) {
          collectFriendsEnergy(friendsObject);
          int pos = 20;
          List<String> idList = new ArrayList<>();
          JSONArray totalDatas = friendsObject.getJSONArray("totalDatas");
          while (pos < totalDatas.length()) {
            JSONObject friend = totalDatas.getJSONObject(pos);
            idList.add(friend.getString("userId"));
            pos++;
            if (pos % 20 == 0) {
              collectFriendsEnergy(idList);
              idList.clear();
            }
          }
          if (!idList.isEmpty()) {
            collectFriendsEnergy(idList);
          }
        }
        selfHomeObject = collectSelfEnergy();
      } catch (Throwable t) {
        Log.i(TAG, "queryEnergyRanking err:");
        Log.printStackTrace(TAG, t);
      }

      if (!TaskCommon.IS_ENERGY_TIME && selfHomeObject != null) {
        String whackMoleStatus = selfHomeObject.optString("whackMoleStatus");
        if (Objects.equals("CAN_PLAY", whackMoleStatus)
            || Objects.equals("CAN_INITIATIVE_PLAY", whackMoleStatus)
            || Objects.equals("NEED_MORE_FRIENDS", whackMoleStatus)) {
          whackMole();
        }
        boolean hasMore = false;
        do {
          if (hasMore) {
            hasMore = false;
            selfHomeObject = querySelfHome();
          }
          if (collectWateringBubble.getValue()) {
            JSONArray wateringBubbles = selfHomeObject.has("wateringBubbles")
                ? selfHomeObject.getJSONArray("wateringBubbles")
                : new JSONArray();
            if (wateringBubbles.length() > 0) {
              int collected = 0;
              for (int i = 0; i < wateringBubbles.length(); i++) {
                JSONObject wateringBubble = wateringBubbles.getJSONObject(i);
                String bizType = wateringBubble.getString("bizType");
                switch (bizType) {
                  case "jiaoshui": {
                    JSONObject joEnergy = new JSONObject(
                        AntForestRpcCall.collectEnergy(
                            bizType,
                            selfId,
                            wateringBubble.getLong("id")
                        )
                    );
                    if (MessageUtil.checkResultCode("收取[我]的浇水金球", joEnergy)) {
                      JSONArray bubbles = joEnergy.getJSONArray("bubbles");
                      for (int j = 0; j < bubbles.length(); j++) {
                        collected = bubbles.getJSONObject(j).getInt("collectedEnergy");
                      }
                      if (collected > 0) {
                        String msg = "收取金球🍯浇水[" + collected + "g]";
                        Log.forest(msg);
                        Toast.show(msg);
                        totalCollected += collected;
                        Statistics.addData(Statistics.DataType.COLLECTED, collected);
                      } else {
                        Log.record("收取[我]的浇水金球失败");
                      }
                    }
                    break;
                  }
                  case "fuhuo": {
                    JSONObject joEnergy = new JSONObject(AntForestRpcCall.collectRebornEnergy());
                    if (MessageUtil.checkResultCode("收取[我]的复活金球", joEnergy)) {
                      collected = joEnergy.getInt("energy");
                      String msg = "收取金球🍯复活[" + collected + "g]";
                      Log.forest(msg);
                      Toast.show(msg);
                      totalCollected += collected;
                      Statistics.addData(Statistics.DataType.COLLECTED, collected);
                    }
                    break;
                  }
                  case "baohuhuizeng": {
                    String friendMaskName = UserIdMap.getMaskName(wateringBubble.getString("userId"));
                    JSONObject joEnergy = new JSONObject(
                        AntForestRpcCall.collectEnergy(
                            bizType,
                            selfId,
                            wateringBubble.getLong("id")
                        )
                    );
                    if (MessageUtil.checkResultCodeString("收取[" + friendMaskName + "]的复活回赠金球", joEnergy)) {
                      JSONArray bubbles = joEnergy.getJSONArray("bubbles");
                      for (int j = 0; j < bubbles.length(); j++) {
                        collected = bubbles.getJSONObject(j).getInt("collectedEnergy");
                      }
                      if (collected > 0) {
                        String msg = "收取金球🍯[" + friendMaskName + "]复活回赠[" + collected + "g]";
                        Log.forest(msg);
                        Toast.show(msg);
                        totalCollected += collected;
                        Statistics.addData(Statistics.DataType.COLLECTED, collected);
                      } else {
                        Log.record("收取[" + friendMaskName + "]的复活回赠金球失败");
                      }
                    }
                    break;
                  }
                }
                TimeUtil.sleep(1000L);
              }
              if (wateringBubbles.length() >= 20) {
                hasMore = true;
              }
            }
          }
          if (collectProp.getValue()) {
            JSONArray givenProps = selfHomeObject.has("givenProps")
                ? selfHomeObject.getJSONArray("givenProps")
                : new JSONArray();
            if (givenProps.length() > 0) {
              for (int i = 0; i < givenProps.length(); i++) {
                JSONObject jo = givenProps.getJSONObject(i);
                String giveConfigId = jo.getString("giveConfigId");
                String giveId = jo.getString("giveId");
                String propName = jo.getJSONObject("propConfig").getString("propName");
                jo = new JSONObject(AntForestRpcCall.collectProp(giveConfigId, giveId));
                if (MessageUtil.checkSuccess(TAG, jo)) {
                  Log.forest("领取道具🎭[" + propName + "]");
                }
                TimeUtil.sleep(1000L);
              }
              if (givenProps.length() >= 20) {
                hasMore = true;
              }
            }
          }
        } while (hasMore);
        JSONArray usingUserProps = selfHomeObject.has("usingUserProps")
            ? selfHomeObject.getJSONArray("usingUserProps")
            : new JSONArray();
        boolean canConsumeAnimalProp = true;
        if (usingUserProps.length() > 0) {
          for (int i = 0; i < usingUserProps.length(); i++) {
            JSONObject jo = usingUserProps.getJSONObject(i);
            if (!Objects.equals("animal", jo.getString("type"))) {
              continue;
            } else {
              canConsumeAnimalProp = false;
            }
            JSONObject extInfo = new JSONObject(jo.getString("extInfo"));
            int energy = extInfo.optInt("energy", 0);
            if (energy > 0 && !extInfo.optBoolean("isCollected")) {
              String propId = jo.getString("propSeq");
              String propType = jo.getString("propType");
              String shortDay = extInfo.getString("shortDay");
              jo = new JSONObject(AntForestRpcCall.collectAnimalRobEnergy(propId, propType, shortDay));
              if (MessageUtil.checkResultCode(TAG, jo)) {
                Log.forest("动物能量🦩[" + energy + "g]");
              }
              TimeUtil.sleep(500);
              break;
            }
          }
        }
        if (userPatrol.getValue()) {
          queryUserPatrol();
        }
        if (combineAnimalPiece.getValue()) {
          queryAnimalAndPiece();
        }
        if (consumeAnimalPropType.getValue() != ConsumeAnimalPropType.NONE) {
          if (!canConsumeAnimalProp) {
            Log.record("已经有动物伙伴在巡护森林");
          } else {
            if (!MyUtils.closeVerification()) queryAnimalPropList();
          }
        }
        if (expiredEnergy.getValue()) {
          popupTask();
        }
        if (energyRain.getValue() && !MyUtils.closeVerification()) {
          energyRain();
        }
        if (receiveForestTaskAward.getValue()) {
          queryTaskList();
        }
        if (ecoLife.getValue()) {
          ecoLife();
        }
        waterFriendEnergy();
        if (!MyUtils.closeVerification()) giveProp();
        if (vitalityExchangeBenefit.getValue()) {
          vitalityExchangeBenefit();
        }
        /* 森林集市 */
        if (greenLife.getValue()) {
          greenLife();
        }

        if (medicalHealth.getValue()) {
          // 医疗健康 绿色医疗 16g*6能量
          queryForestEnergy("FEEDS");
          // 医疗健康 电子小票 4g*10能量
          queryForestEnergy("BILL");
        }
        if (dress.getValue()) {
          dress();
        }
        if (forestChouChouLe.getValue()) {
          ForestChouChouLe chouChouLe = new ForestChouChouLe();
          chouChouLe.chouChouLe();
        }
      }
    } catch (Throwable t) {
      Log.i(TAG, "AntForestV2.run err:");
      Log.printStackTrace(TAG, t);
    } finally {
      try {
        synchronized (AntForestV2.this) {
          int count = taskCount.get();
          if (count > 0) {
            AntForestV2.this.wait(TimeUnit.MINUTES.toMillis(30));
            count = taskCount.get();
          }
          if (count > 0) {
            Log.record("执行超时-蚂蚁森林");
          } else if (count == 0) {
            Log.record("执行结束-蚂蚁森林");
          } else {
            Log.record("执行完成-蚂蚁森林");
          }
        }
      } catch (InterruptedException ie) {
        Log.i(TAG, "执行中断-蚂蚁森林");
      }
      Statistics.save();
      FriendWatch.save();
      NotificationUtil.updateLastExecText("收:" + totalCollected + " 帮:" + totalHelpCollected);
    }
  }

  private void notifyMain() {
    if (taskCount.decrementAndGet() < 1) {
      synchronized (AntForestV2.this) {
        AntForestV2.this.notifyAll();
      }
    }
  }

  private JSONObject querySelfHome() {
    JSONObject userHomeObject = null;
    try {
      long start = System.currentTimeMillis();
      userHomeObject = new JSONObject(AntForestRpcCall.queryHomePage());
      long end = System.currentTimeMillis();
      long serverTime = MyUtils.antForestV2NowMaybeNull(userHomeObject);//CHANGE BY KT
      int offsetTime = offsetTimeMath.nextInteger((int) ((start + end) / 2 - serverTime));
      Log.i("服务器时间：" + serverTime + "，本地与服务器时间差：" + offsetTime);
    } catch (Throwable t) {
      Log.printStackTrace(t);
    }
    return userHomeObject;
  }

  private JSONObject queryFriendHome(String userId) {
    JSONObject userHomeObject = null;
    try {
      long start = System.currentTimeMillis();
      userHomeObject = new JSONObject(AntForestRpcCall.queryFriendHomePage(userId));
      long end = System.currentTimeMillis();
      long serverTime = MyUtils.antForestV2NowMaybeNull(userHomeObject);//CHANGE BY KT
      int offsetTime = offsetTimeMath.nextInteger((int) ((start + end) / 2 - serverTime));
      Log.i("服务器时间：" + serverTime + "，本地与服务器时间差：" + offsetTime);
    } catch (Throwable t) {
      Log.printStackTrace(t);
    }
    return userHomeObject;
  }

  private JSONObject collectSelfEnergy() {
    try {
      JSONObject selfHomeObject = querySelfHome();
      if (selfHomeObject != null) {
        if (closeWhackMole.getValue()) {
          JSONObject propertiesObject = selfHomeObject.optJSONObject("properties");
          if (propertiesObject != null) {
            if (Objects.equals("Y", propertiesObject.optString("whackMole"))) {
              if (closeWhackMole()) {
                Log.record("6秒拼手速关闭成功");
              } else {
                Log.record("6秒拼手速关闭失败");
              }
            }
          }
        }
        String nextAction = selfHomeObject.optString("nextAction");
        if ("WhackMole".equalsIgnoreCase(nextAction)) {
          Log.record("检测到6秒拼手速强制弹窗，先执行拼手速");
          whackMole();
        }
        return collectUserEnergy(UserIdMap.getCurrentUid(), selfHomeObject);
      }
    } catch (Throwable t) {
      Log.printStackTrace(t);
    }
    return null;
  }

  private JSONObject collectFriendEnergy(String userId) {
    if (hasErrorWait) {
      return null;
    }
    try {
      JSONObject userHomeObject = queryFriendHome(userId);
      if (userHomeObject != null) {
        return collectUserEnergy(userId, userHomeObject);
      }
    } catch (Throwable t) {
      Log.printStackTrace(t);
    }
    return null;
  }

  private JSONObject collectUserEnergy(String userId, JSONObject userHomeObject) {
    try {
      if (!MessageUtil.checkResultCode(TAG, userHomeObject)) {
        return userHomeObject;
      }
      long serverTime = userHomeObject.getLong("now");
      boolean isSelf = Objects.equals(userId, selfId);
      String userName = UserIdMap.getMaskName(userId);
      Log.record("进入[" + userName + "]的蚂蚁森林");

      boolean isCollectEnergy = collectEnergy.getValue() && !dontCollectMap.contains(userId);

      if (isSelf) {
        updateUsingPropsEndTime(userHomeObject);
      } else {
        if (isCollectEnergy) {
          JSONArray jaProps = userHomeObject.optJSONArray("usingUserProps");
          if (jaProps != null) {
            for (int i = 0; i < jaProps.length(); i++) {
              JSONObject joProps = jaProps.getJSONObject(i);
              if (Objects.equals("energyShield", joProps.getString("type"))) {
                if (joProps.getLong("endTime") > serverTime) {
                  Log.record("[" + userName + "]被能量罩保护着哟");
                  isCollectEnergy = false;
                  break;
                }
              }
            }
          }
        }
      }

      if (isCollectEnergy) {
        JSONArray jaBubbles = userHomeObject.getJSONArray("bubbles");
        List<Long> bubbleIdList = new ArrayList<>();
        for (int i = 0; i < jaBubbles.length(); i++) {
          JSONObject bubble = jaBubbles.getJSONObject(i);
          long bubbleId = bubble.getLong("id");
          switch (CollectStatus.valueOf(bubble.getString("collectStatus"))) {
            case AVAILABLE:
              bubbleIdList.add(bubbleId);
              break;
            case WAITING:
              long produceTime = bubble.getLong("produceTime");
              if (checkIntervalInt + checkIntervalInt / 2 > produceTime - serverTime) {
                if (hasChildTask(AntForestV2.getBubbleTimerTid(userId, bubbleId))) {
                  break;
                }
                addChildTask(new BubbleTimerTask(userId, bubbleId, produceTime));
                Log.record("添加蹲点收取🪂[" + userName + "]在[" + TimeUtil.getCommonDate(produceTime) + "]执行");
              } else {
                Log.i("用户[" + UserIdMap.getMaskName(userId) + "]能量成熟时间: " + TimeUtil.getCommonDate(produceTime));
              }
              break;
          }
        }
        if (batchRobEnergy.getValue()) {
          Iterator<Long> iterator = bubbleIdList.iterator();
          List<Long> batchBubbleIdList = new ArrayList<>();
          while (iterator.hasNext()) {
            batchBubbleIdList.add(iterator.next());
            if (batchBubbleIdList.size() >= 6) {
              collectEnergy(new CollectEnergyEntity(userId, userHomeObject, AntForestRpcCall.getCollectBatchEnergyRpcEntity(userId, batchBubbleIdList)));
              batchBubbleIdList = new ArrayList<>();
            }
          }
          int size = batchBubbleIdList.size();
          if (size > 0) {
            if (size == 1) {
              collectEnergy(new CollectEnergyEntity(userId, userHomeObject, AntForestRpcCall.getCollectEnergyRpcEntity(null, userId, batchBubbleIdList.get(0))));
            } else {
              collectEnergy(new CollectEnergyEntity(userId, userHomeObject, AntForestRpcCall.getCollectBatchEnergyRpcEntity(userId, batchBubbleIdList)));
            }
          }
        } else {
          for (Long bubbleId : bubbleIdList) {
            collectEnergy(new CollectEnergyEntity(userId, userHomeObject, AntForestRpcCall.getCollectEnergyRpcEntity(null, userId, bubbleId)));
          }
        }
      }
      return userHomeObject;
    } catch (Throwable t) {
      Log.i(TAG, "collectUserEnergy err:");
      Log.printStackTrace(TAG, t);
    }
    return null;
  }

  private void collectFriendsEnergy(List<String> idList) {
    try {
      if (hasErrorWait) {
        return;
      }
      collectFriendsEnergy(new JSONObject(AntForestRpcCall.fillUserRobFlag(new JSONArray(idList).toString())));
    } catch (Exception e) {
      Log.printStackTrace(e);
    }
  }

  private void collectFriendsEnergy(JSONObject friendsObject) {
    if (hasErrorWait) {
      return;
    }
    try {
      JSONArray jaFriendRanking = friendsObject.optJSONArray("friendRanking");
      if (jaFriendRanking == null) {
        return;
      }
      for (int i = 0, len = jaFriendRanking.length(); i < len; i++) {
        try {
          JSONObject friendObject = jaFriendRanking.getJSONObject(i);
          String userId = friendObject.getString("userId");
          if (Objects.equals(userId, selfId)) {
            continue;
          }
          JSONObject userHomeObject = null;
          if (collectEnergy.getValue() && !dontCollectMap.contains(userId)) {
            boolean collectEnergy = true;
            if (!friendObject.optBoolean("canCollectEnergy")) {
              long canCollectLaterTime = friendObject.getLong("canCollectLaterTime");
              if (canCollectLaterTime <= 0 || (canCollectLaterTime - System.currentTimeMillis() > checkIntervalInt)) {
                collectEnergy = false;
              }
            }
            if (collectEnergy) {
              userHomeObject = collectFriendEnergy(userId);
            }/* else {
                            Log.i("不收取[" + UserIdMap.getNameById(userId) + "], userId=" + userId);
                        }*/
          }
          if (helpFriendCollectType.getValue() != HelpFriendCollectType.NONE
              && friendObject.optBoolean("canProtectBubble")
              && !Status.hasFlagToday("forest::protectBubble")) {
            boolean isHelpCollect = helpFriendCollectList.getValue().contains(userId);
            if (helpFriendCollectType.getValue() != HelpFriendCollectType.HELP) {
              isHelpCollect = !isHelpCollect;
            }
            if (isHelpCollect) {
              if (userHomeObject == null) {
                userHomeObject = queryFriendHome(userId);
              }
              if (userHomeObject != null) {
                protectFriendEnergy(userHomeObject);
              }
            }
          }
          if (collectGiftBox.getValue() && friendObject.getBoolean("canCollectGiftBox")) {
            if (userHomeObject == null) {
              userHomeObject = queryFriendHome(userId);
            }
            if (userHomeObject != null) {
              collectGiftBox(userHomeObject);
            }
          }
        } catch (Exception t) {
          Log.i(TAG, "collectFriendEnergy err:");
          Log.printStackTrace(TAG, t);
        }
      }
    } catch (Exception e) {
      Log.printStackTrace(e);
    }
  }

  private void collectGiftBox(JSONObject userHomeObject) {
    try {
      JSONObject giftBoxInfo = userHomeObject.optJSONObject("giftBoxInfo");
      JSONObject userEnergy = userHomeObject.optJSONObject("userEnergy");
      String userId = userEnergy == null ? UserIdMap.getCurrentUid() : userEnergy.optString("userId");
      if (giftBoxInfo != null) {
        JSONArray giftBoxList = giftBoxInfo.optJSONArray("giftBoxList");
        if (giftBoxList != null && giftBoxList.length() > 0) {
          for (int ii = 0; ii < giftBoxList.length(); ii++) {
            try {
              JSONObject giftBox = giftBoxList.getJSONObject(ii);
              String giftBoxId = giftBox.getString("giftBoxId");
              String title = giftBox.getString("title");
              JSONObject giftBoxResult = new JSONObject(AntForestRpcCall.collectFriendGiftBox(giftBoxId, userId));
              if (!MessageUtil.checkResultCode(TAG, giftBoxResult)) {
                continue;
              }
              int energy = giftBoxResult.optInt("energy", 0);
              Log.forest("礼盒能量🎁[" + UserIdMap.getMaskName(userId) + "-" + title + "]#" + energy + "g");
              Statistics.addData(Statistics.DataType.COLLECTED, energy);
            } catch (Throwable t) {
              Log.printStackTrace(t);
              break;
            } finally {
              TimeUtil.sleep(500);
            }
          }
        }
      }
    } catch (Exception e) {
      Log.printStackTrace(e);
    }
  }

  private void protectFriendEnergy(JSONObject userHomeObject) {
    try {
      JSONArray wateringBubbles = userHomeObject.optJSONArray("wateringBubbles");
      JSONObject userEnergy = userHomeObject.optJSONObject("userEnergy");
      String userId = userEnergy == null ? UserIdMap.getCurrentUid() : userEnergy.optString("userId");
      if (wateringBubbles != null && wateringBubbles.length() > 0) {
        for (int j = 0; j < wateringBubbles.length(); j++) {
          try {
            JSONObject wateringBubble = wateringBubbles.getJSONObject(j);
            if (!Objects.equals("fuhuo", wateringBubble.getString("bizType"))) {
              continue;
            }
            if (wateringBubble.getJSONObject("extInfo").optInt("restTimes", 0) == 0) {
              Status.flagToday("forest::protectBubble");
            }
            if (!wateringBubble.getBoolean("canProtect")) {
              continue;
            }
            JSONObject joProtect = new JSONObject(AntForestRpcCall.protectBubble(userId));
            if (!MessageUtil.checkResultCode(TAG, joProtect)) {
              continue;
            }
            int vitalityAmount = joProtect.optInt("vitalityAmount", 0);
            int fullEnergy = wateringBubble.optInt("fullEnergy", 0);
            String str = "复活能量🚑[" + UserIdMap.getMaskName(userId) + "-" + fullEnergy + "g]" + (vitalityAmount > 0 ? "#活力值+" + vitalityAmount : "");
            Log.forest(str);
            totalHelpCollected += fullEnergy;
            Statistics.addData(Statistics.DataType.HELPED, fullEnergy);
            break;
          } catch (Throwable t) {
            Log.printStackTrace(t);
            break;
          } finally {
            TimeUtil.sleep(500);
          }
        }
      }
    } catch (Exception e) {
      Log.printStackTrace(e);
    }
  }

  private void collectEnergy(CollectEnergyEntity collectEnergyEntity) {
    collectEnergy(collectEnergyEntity, false);
  }

  private void collectEnergy(CollectEnergyEntity collectEnergyEntity, Boolean joinThread) {
    if (hasErrorWait) {
      return;
    }
    Runnable runnable = () -> {
      try {
        String userId = collectEnergyEntity.getUserId();
        usePropBeforeCollectEnergy(userId);
        RpcEntity rpcEntity = collectEnergyEntity.getRpcEntity();
        boolean needDouble = collectEnergyEntity.getNeedDouble();
        boolean needRetry = collectEnergyEntity.getNeedRetry();
        int tryCount = collectEnergyEntity.addTryCount();
        int collected = 0;
        long startTime;
        synchronized (collectEnergyLockLimit) {
          long sleep;
          if (needDouble) {
            collectEnergyEntity.unsetNeedDouble();
            sleep = doubleCollectIntervalEntity.getInterval() - System.currentTimeMillis() + collectEnergyLockLimit.get();
          } else if (needRetry) {
            collectEnergyEntity.unsetNeedRetry();
            sleep = retryIntervalInt - System.currentTimeMillis() + collectEnergyLockLimit.get();
          } else {
            sleep = collectIntervalEntity.getInterval() - System.currentTimeMillis() + collectEnergyLockLimit.get();
          }
          if (sleep > 0) {
            TimeUtil.sleep(sleep);
          }
          startTime = System.currentTimeMillis();
          collectEnergyLockLimit.setForce(startTime);
        }
        ApplicationHook.requestObject(rpcEntity, 0, 0);
        long spendTime = System.currentTimeMillis() - startTime;
        if (balanceNetworkDelay.getValue()) {
          delayTimeMath.nextInteger((int) (spendTime / 3));
        }
        if (rpcEntity.getHasError()) {
          String errorCode = (String) XposedHelpers.callMethod(rpcEntity.getResponseObject(), "getString", "error");
          if (Objects.equals("1004", errorCode)) {
            if (BaseModel.getWaitWhenException().getValue() > 0) {
              long waitTime = System.currentTimeMillis() + BaseModel.getWaitWhenException().getValue();
              RuntimeInfo.getInstance().put(RuntimeInfo.RuntimeInfoKey.ForestPauseTime, waitTime);
              NotificationUtil.updateStatusText("异常");
              Log.record("触发异常,等待至" + TimeUtil.getCommonDate(waitTime));
              hasErrorWait = true;
              return;
            }
            TimeUtil.sleep(600 + RandomUtil.delay());
          }
          if (tryCount < tryCountInt) {
            collectEnergyEntity.setNeedRetry();
            collectEnergy(collectEnergyEntity, true);
          }
          return;
        }
        JSONObject jo = new JSONObject(rpcEntity.getResponseString());
        String resultCode = jo.getString("resultCode");
        if (!"SUCCESS".equalsIgnoreCase(resultCode)) {
          if ("PARAM_ILLEGAL2".equals(resultCode)) {
            Log.record("[" + UserIdMap.getMaskName(userId) + "]" + "能量已被收取,取消重试 错误:" + jo.getString("resultDesc"));
            return;
          }
          Log.record("[" + UserIdMap.getMaskName(userId) + "]" + jo.getString("resultDesc"));
          if (tryCount < tryCountInt) {
            collectEnergyEntity.setNeedRetry();
            collectEnergy(collectEnergyEntity, true);
          }
          return;
        }
        JSONArray jaBubbles = jo.getJSONArray("bubbles");
        int jaBubbleLength = jaBubbles.length();
        if (jaBubbleLength > 1) {
          List<Long> newBubbleIdList = new ArrayList<>();
          for (int i = 0; i < jaBubbleLength; i++) {
            JSONObject bubble = jaBubbles.getJSONObject(i);
            if (bubble.getBoolean("canBeRobbedAgain")) {
              newBubbleIdList.add(bubble.getLong("id"));
            }
            collected += bubble.getInt("collectedEnergy");
          }
          if (collected > 0) {
            FriendWatch.friendWatch(userId, collected);
            String str = "一键收取🪂[" + UserIdMap.getMaskName(userId) + "]#" + collected + "g";
            if (needDouble) {
              Log.forest(str + "耗时[" + spendTime + "]ms[双击]");
              Toast.show(str + "[双击]");
            } else {
              Log.forest(str + "耗时[" + spendTime + "]ms");
              Toast.show(str);
            }
            totalCollected += collected;
            Statistics.addData(Statistics.DataType.COLLECTED, collected);
          } else {
            Log.record("一键收取[" + UserIdMap.getMaskName(userId) + "]的能量失败" + " " + "，UserID：" + userId + "，BubbleId：" + newBubbleIdList);
          }
          if (!newBubbleIdList.isEmpty()) {
            collectEnergyEntity.setRpcEntity(AntForestRpcCall.getCollectBatchEnergyRpcEntity(userId, newBubbleIdList));
            collectEnergyEntity.setNeedDouble();
            collectEnergyEntity.resetTryCount();
            collectEnergy(collectEnergyEntity, true);
            return;
          }
        } else if (jaBubbleLength == 1) {
          JSONObject bubble = jaBubbles.getJSONObject(0);
          collected += bubble.getInt("collectedEnergy");
          FriendWatch.friendWatch(userId, collected);
          if (collected > 0) {
            String str = "收取能量🪂[" + UserIdMap.getMaskName(userId) + "]#" + collected + "g";
            if (needDouble) {
              Log.forest(str + "耗时[" + spendTime + "]ms[双击]");
              Toast.show(str + "[双击]");
            } else {
              Log.forest(str + "耗时[" + spendTime + "]ms");
              Toast.show(str);
            }
            totalCollected += collected;
            Statistics.addData(Statistics.DataType.COLLECTED, collected);
          } else {
            Log.record("收取[" + UserIdMap.getMaskName(userId) + "]的能量失败");
            Log.i("，UserID：" + userId + "，BubbleId：" + bubble.getLong("id"));
          }
          if (bubble.getBoolean("canBeRobbedAgain")) {
            collectEnergyEntity.setNeedDouble();
            collectEnergyEntity.resetTryCount();
            collectEnergy(collectEnergyEntity, true);
            return;
          }
          JSONObject userHome = collectEnergyEntity.getUserHome();
          if (userHome == null) {
            return;
          }
          String bizNo = userHome.optString("bizNo");
          if (bizNo.isEmpty()) {
            return;
          }
          int returnCount = 0;
          if (returnWater33.getValue() > 0 && collected >= returnWater33.getValue()) {
            returnCount = 33;
          } else if (returnWater18.getValue() > 0 && collected >= returnWater18.getValue()) {
            returnCount = 18;
          } else if (returnWater10.getValue() > 0 && collected >= returnWater10.getValue()) {
            returnCount = 10;
          }
          if (returnCount > 0) {
            returnFriendWater(userId, bizNo, 1, returnCount);
          }
        }
      } catch (Exception e) {
        Log.i("collectEnergy err:");
        Log.printStackTrace(e);
      } finally {
        Statistics.save();
        NotificationUtil.updateLastExecText("收:" + totalCollected + " 帮:" + totalHelpCollected);
        notifyMain();
      }
    };
    taskCount.incrementAndGet();
    if (joinThread) {
      runnable.run();
    } else {
      addChildTask(new ChildModelTask("CE|" + collectEnergyEntity.getUserId() + "|" + runnable.hashCode(), "CE", runnable));
    }
  }

  private void updateUsingPropsEndTime() throws JSONException {
    JSONObject joHomePage = new JSONObject(AntForestRpcCall.queryHomePage());
    TimeUtil.sleep(100);
    updateUsingPropsEndTime(joHomePage);
  }

  private void updateUsingPropsEndTime(JSONObject joHomePage) {
    try {
      JSONArray ja = joHomePage.getJSONArray("loginUserUsingPropNew");
      if (ja.length() == 0) {
        ja = joHomePage.getJSONArray("usingUserPropsNew");
      }
      for (int i = 0; i < ja.length(); i++) {
        JSONObject jo = ja.getJSONObject(i);
        String propGroup = jo.getString("propGroup");
        Long endTime = jo.getLong("endTime");
        usingProps.put(propGroup, endTime);
        if (PropGroup.robExpandCard.name().equals(propGroup)) {
          collectRobExpandEnergy(jo.optString("extInfo"));
        }
      }
      forestExtensions();
    } catch (Throwable th) {
      Log.i(TAG, "updateUsingPropsEndTime err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private void collectRobExpandEnergy(String extInfo) {
    if (extInfo.isEmpty()) {
      return;
    }
    try {
      JSONObject jo = new JSONObject(extInfo);
      double leftEnergy = Double.parseDouble(jo.optString("leftEnergy", "0"));
      if (leftEnergy > 3000
          || (Objects.equals(jo.optString("overLimitToday", "false"), "true") && leftEnergy > 0)) {
        String propId = jo.getString("propId");
        String propType = jo.getString("propType");
        collectRobExpandEnergy(propId, propType);
      }
    } catch (Throwable th) {
      Log.i(TAG, "collectRobExpandEnergy err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private void collectRobExpandEnergy(String propId, String propType) {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.collectRobExpandEnergy(propId, propType));
      if (MessageUtil.checkResultCode(TAG, jo)) {
        int collectEnergy = jo.optInt("collectEnergy");
        Log.forest("额外能量🎄收取[" + collectEnergy + "g]");
        totalCollected += collectEnergy;
        Statistics.addData(Statistics.DataType.COLLECTED, collectEnergy);
      }
    } catch (Throwable th) {
      Log.i(TAG, "collectRobExpandEnergy err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private void queryForestEnergy(String scene) {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.queryForestEnergy(scene));
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }
      jo = jo.getJSONObject("data").getJSONObject("response");
      JSONArray ja = jo.getJSONArray("energyGeneratedList");
      if (ja.length() > 0) {
        harvestForestEnergy(scene, ja);
      }
      int remainBubble = jo.optInt("remainBubble");
      for (int i = 0; i < remainBubble; i++) {
        ja = produceForestEnergy(scene);
        if (ja.length() == 0 || !harvestForestEnergy(scene, ja)) {
          return;
        }
        TimeUtil.sleep(1000);
      }
    } catch (Throwable th) {
      Log.i(TAG, "queryForestEnergy err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private JSONArray produceForestEnergy(String scene) {
    JSONArray energyGeneratedList = new JSONArray();
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.produceForestEnergy(scene));
      if (MessageUtil.checkResultCode(TAG, jo)) {
        jo = jo.getJSONObject("data").getJSONObject("response");
        energyGeneratedList = jo.getJSONArray("energyGeneratedList");
        if (energyGeneratedList.length() > 0) {
          String title = scene.equals("FEEDS") ? "绿色医疗" : "电子小票";
          int cumulativeEnergy = jo.getInt("cumulativeEnergy");
          Log.forest("医疗健康🚑完成[" + title + "]#产生[" + cumulativeEnergy + "g能量]");
        }
      }
    } catch (Throwable th) {
      Log.i(TAG, "produceForestEnergy err:");
      Log.printStackTrace(TAG, th);
    }
    return energyGeneratedList;
  }

  private Boolean harvestForestEnergy(String scene, JSONArray bubbles) {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.harvestForestEnergy(scene, bubbles));
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return false;
      }
      jo = jo.getJSONObject("data").getJSONObject("response");
      int collectedEnergy = jo.getInt("collectedEnergy");
      if (collectedEnergy > 0) {
        String title = scene.equals("FEEDS") ? "绿色医疗" : "电子小票";
        Log.forest("医疗健康🚑收取[" + title + "]#获得[" + collectedEnergy + "g能量]");
        totalCollected += collectedEnergy;
        Statistics.addData(Statistics.DataType.COLLECTED, collectedEnergy);
        return true;
      }
    } catch (Throwable th) {
      Log.i(TAG, "harvestForestEnergy err:");
      Log.printStackTrace(TAG, th);
    }
    return false;
  }

  /* 6秒拼手速 打地鼠 */
  private void whackMole() {
    if (MyUtils.closeVerification()) return;
    try {
      long start = System.currentTimeMillis();
      JSONObject jo = new JSONObject(AntForestRpcCall.startWhackMole());
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }
      JSONArray moleInfo = jo.optJSONArray("moleInfo");
      if (moleInfo != null) {
        List<String> whackMoleIdList = new ArrayList<>();
        for (int i = 0; i < moleInfo.length(); i++) {
          JSONObject mole = moleInfo.getJSONObject(i);
          long moleId = mole.getLong("id");
          whackMoleIdList.add(String.valueOf(moleId));
        }
        if (!whackMoleIdList.isEmpty()) {
          String token = jo.getString("token");
          long end = System.currentTimeMillis();
          TimeUtil.sleep(6000 - end + start);
          jo = new JSONObject(AntForestRpcCall.settlementWhackMole(token, whackMoleIdList));
          if (MessageUtil.checkResultCode(TAG, jo)) {
            int totalEnergy = jo.getInt("totalEnergy");
            Log.forest("森林能量⚡[获得:6秒拼手速能量" + totalEnergy + "g]");
            totalCollected += totalEnergy;
            Statistics.addData(Statistics.DataType.COLLECTED, totalEnergy);
          }
        }
      }
    } catch (Throwable t) {
      Log.i(TAG, "whackMole err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private Boolean closeWhackMole() {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.closeWhackMole());
      return MessageUtil.checkSuccess(TAG, jo);
    } catch (Throwable t) {
      Log.printStackTrace(t);
    }
    return false;
  }

  /* 森林集市 */
  private static void greenLife() {
    sendEnergyByAction("GREEN_LIFE");
    sendEnergyByAction("ANTFOREST");
    retrieveCurrentActivity();
  }

  private static void retrieveCurrentActivity() {
    try {
      JSONObject jo = new JSONObject(GreenLifeRpcCall.retrieveCurrentActivity());
      if (!MessageUtil.checkSuccess(TAG, jo)) {
        return;
      }
      jo = jo.getJSONObject("data");
      JSONObject currentActivity = jo.optJSONObject("currentActivity");
      if (currentActivity == null) {
        return;
      }
      int numberOfDaysCompleted = currentActivity.getInt("numberOfDaysCompleted") + 1;
      JSONObject currentTask = jo.getJSONObject("currentTask");
      if (currentTask.getBoolean("checkInCompleted")) {
        return;
      }
      String taskTemplateId = currentTask.getString("taskTemplateId");
      jo = new JSONObject(GreenLifeRpcCall.finishCurrentTask(taskTemplateId));
      if (!MessageUtil.checkSuccess(TAG, jo)) {
        return;
      }
      jo = jo.getJSONObject("data");
      JSONArray ja = jo.getJSONArray("prizes");
      StringBuilder award = new StringBuilder();
      for (int i = 0; i < ja.length(); i++) {
        jo = ja.getJSONObject(i);
        if (i > 0) award.append(";");
        award.append(jo.getString("name"));
      }
      if (award.length() > 0) {
        award = new StringBuilder("#获得[" + award + "]");
      }
      Log.forest("森林集市🛍️打卡[坚持" + numberOfDaysCompleted + "天]" + award);
    } catch (Throwable t) {
      Log.i(TAG, "retrieveCurrentActivity err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private static void sendEnergyByAction(String sourceType) {
    try {
      JSONObject jo = new JSONObject(GreenLifeRpcCall.consultForSendEnergyByAction(sourceType));
      if (!MessageUtil.checkSuccess(TAG, jo)) {
        return;
      }
      JSONObject data = jo.getJSONObject("data");
      if (data.optBoolean("canSendEnergy", false)) {
        jo = new JSONObject(GreenLifeRpcCall.sendEnergyByAction(sourceType));
        if (MessageUtil.checkSuccess(TAG, jo)) {
          data = jo.getJSONObject("data");
          if (data.optBoolean("canSendEnergy", false)) {
            int receivedEnergyAmount = data.getInt("receivedEnergyAmount");
            Log.forest("森林集市🛍️完成[线上逛街]#产生[" + receivedEnergyAmount + "g能量]");
          }
        }
      }
    } catch (Throwable t) {
      Log.i(TAG, "sendEnergyByAction err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private void popupTask() {
    try {
      JSONObject resData = new JSONObject(AntForestRpcCall.popupTask());
      if (!MessageUtil.checkResultCode(TAG, resData)) {
        return;
      }
      JSONArray forestSignVOList = resData.optJSONArray("forestSignVOList");
      if (forestSignVOList != null) {
        for (int i = 0; i < forestSignVOList.length(); i++) {
          JSONObject forestSignVO = forestSignVOList.getJSONObject(i);
          String signId = forestSignVO.getString("signId");
          String currentSignKey = forestSignVO.getString("currentSignKey");
          JSONArray signRecords = forestSignVO.getJSONArray("signRecords");
          for (int j = 0; j < signRecords.length(); j++) {
            JSONObject signRecord = signRecords.getJSONObject(j);
            String signKey = signRecord.getString("signKey");
            if (signKey.equals(currentSignKey)) {
              if (!signRecord.getBoolean("signed")) {
                JSONObject resData2 = new JSONObject(
                    AntForestRpcCall.antiepSign(signId, UserIdMap.getCurrentUid()));
                if (MessageUtil.checkSuccess(TAG, resData2)) {
                  Log.forest("过期能量💊[" + signRecord.getInt("awardCount") + "g]");
                }
              }
              break;
            }
          }
        }
      }
    } catch (Throwable t) {
      Log.i(TAG, "popupTask err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private void waterFriendEnergy() {
    int waterEnergy = WaterFriendType.waterEnergy[waterFriendType.getValue()];
    if (waterEnergy == 0) {
      return;
    }
    Map<String, Integer> friendMap = waterFriendList.getValue();
    for (Map.Entry<String, Integer> friendEntry : friendMap.entrySet()) {
      String uid = friendEntry.getKey();
      if (selfId.equals(uid)) {
        continue;
      }
      Integer waterCount = friendEntry.getValue();
      if (waterCount == null || waterCount <= 0) {
        continue;
      }
      if (waterCount > 3)
        waterCount = 3;
      if (Status.canWaterFriendToday(uid, waterCount)) {
        try {
          JSONObject jo = new JSONObject(AntForestRpcCall.queryFriendHomePage(uid));
          TimeUtil.sleep(100);
          if (MessageUtil.checkResultCode(TAG, jo)) {
            String bizNo = jo.getString("bizNo");
            KVNode<Integer, Boolean> waterCountKVNode = returnFriendWater(uid, bizNo, waterCount, waterEnergy);
            waterCount = waterCountKVNode.getKey();
            if (waterCount > 0) {
              Status.waterFriendToday(uid, waterCount);
            }
            if (!waterCountKVNode.getValue()) {
              break;
            }
          }
        } catch (Throwable t) {
          Log.i(TAG, "waterFriendEnergy err:");
          Log.printStackTrace(TAG, t);
        }
      }
    }
  }

  private KVNode<Integer, Boolean> returnFriendWater(String userId, String bizNo, int count, int waterEnergy) {
    if (bizNo == null || bizNo.isEmpty()) {
      return new KVNode<>(0, true);
    }
    int wateredTimes = 0;
    boolean isContinue = true;
    try {
      String s;
      JSONObject jo;
      int energyId = getEnergyId(waterEnergy);
      label:
      for (int waterCount = 1; waterCount <= count; waterCount++) {
        s = AntForestRpcCall.transferEnergy(userId, bizNo, energyId);
        TimeUtil.sleep(1500);
        jo = new JSONObject(s);
        String resultCode = jo.getString("resultCode");
        switch (resultCode) {
          case "SUCCESS":
            String currentEnergy = jo.getJSONObject("treeEnergy").getString("currentEnergy");
            Log.forest("好友浇水🚿[" + UserIdMap.getMaskName(userId) + "]#" + waterEnergy + "g，剩余能量["
                + currentEnergy + "g]");
            wateredTimes++;
            Statistics.addData(Statistics.DataType.WATERED, waterEnergy);
            break;
          case "WATERING_TIMES_LIMIT":
            Log.record("好友浇水🚿今日给[" + UserIdMap.getMaskName(userId) + "]浇水已达上限");
            wateredTimes = 3;
            break label;
          case "ENERGY_INSUFFICIENT":
            Log.record("好友浇水🚿" + jo.optString("resultDesc"));
            if (MyUtils.closeErrorFunction() && "TA还不是您的好友哦".equals(jo.optString("resultDesc"))) {
              wateredTimes = 3;//直接按照已达上限处理
              break label;
            }
            isContinue = false;
            break label;
          default:
            Log.record("好友浇水🚿" + jo.optString("resultDesc"));
            Log.i(jo.toString());
            break;
        }
      }
    } catch (Throwable t) {
      Log.i(TAG, "returnFriendWater err:");
      Log.printStackTrace(TAG, t);
    }
    return new KVNode<>(wateredTimes, isContinue);
  }

  private int getEnergyId(int waterEnergy) {
    if (waterEnergy <= 0)
      return 0;
    if (waterEnergy >= 66)
      return 42;
    if (waterEnergy >= 33)
      return 41;
    if (waterEnergy >= 18)
      return 40;
    return 39;
  }

  private void forestExtensions() {
    try {
      ExtensionsHandle.handleAlphaRequest("antForest", "extensions", usingProps);
    } catch (Throwable t) {
      Log.i(TAG, "forestExtensions err:");
      Log.printStackTrace(TAG, t);
    }
  }

  // skuId, sku
  Map<String, JSONObject> skuInfo = new HashMap<>();

  private void vitalityExchangeBenefit() {
    try {
      getAllSkuInfo();
      Map<String, Integer> exchangeList = vitalityExchangeBenefitList.getValue();
      for (Map.Entry<String, Integer> entry : exchangeList.entrySet()) {
        String skuId = entry.getKey();
        Integer count = entry.getValue();
        if (count == null || count < 0) {
          continue;
        }
        while (Status.canVitalityExchangeBenefitToday(skuId, count) && exchangeBenefit(skuId)) {
          TimeUtil.sleep(3000);
        }
      }
    } catch (Throwable t) {
      Log.i(TAG, "vitalityExchangeBenefit err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private void vitalitySign(JSONArray forestSignVOList) {
    try {
      JSONObject forestSignVO = forestSignVOList.getJSONObject(0);
      String currentSignKey = forestSignVO.getString("currentSignKey");
      JSONArray signRecords = forestSignVO.getJSONArray("signRecords");
      for (int i = 0; i < signRecords.length(); i++) {
        JSONObject signRecord = signRecords.getJSONObject(i);
        String signKey = signRecord.getString("signKey");
        if (signKey.equals(currentSignKey)) {
          if (!signRecord.getBoolean("signed")) {
            vitalitySign();
          }
          return;
        }
      }
    } catch (Throwable t) {
      Log.i(TAG, "vitalitySign err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private void vitalitySign() {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.vitalitySign());
      TimeUtil.sleep(300);
      if (MessageUtil.checkResultCode(TAG, jo)) {
        int continuousCount = jo.getInt("continuousCount");
        int signAwardCount = jo.getInt("signAwardCount");
        Log.forest("森林任务📆签到[" + continuousCount + "天]奖励[" + signAwardCount + "活力值]");
      }
    } catch (Throwable t) {
      Log.i(TAG, "vitalitySign err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private void queryTaskList() {
    queryTaskList("DNHZ_SL_college", "DAXUESHENG_SJK");
    queryTaskList("DXS_BHZ", "NENGLIANGZHAO_20230807");
    queryTaskList("DXS_JSQ", "JIASUQI_20230808");
    try {
      boolean doubleCheck = true;
      while (doubleCheck) {
        doubleCheck = false;
        JSONObject jo = new JSONObject(AntForestRpcCall.queryTaskList());
        if (!MessageUtil.checkResultCode(TAG, jo)) {
          return;
        }
        JSONArray forestSignVOList = jo.getJSONArray("forestSignVOList");
        vitalitySign(forestSignVOList);
        JSONArray forestTasksNew = jo.optJSONArray("forestTasksNew");
        if (forestTasksNew == null) {
          return;
        }
        for (int i = 0; i < forestTasksNew.length(); i++) {
          JSONObject forestTask = forestTasksNew.getJSONObject(i);
          JSONArray taskInfoList = forestTask.getJSONArray("taskInfoList");
          for (int j = 0; j < taskInfoList.length(); j++) {
            JSONObject taskInfo = taskInfoList.getJSONObject(j);
            JSONObject taskBaseInfo = taskInfo.getJSONObject("taskBaseInfo");
            JSONObject bizInfo = new JSONObject(taskBaseInfo.getString("bizInfo"));
            String taskType = taskBaseInfo.getString("taskType");
            String taskTitle = bizInfo.optString("taskTitle", taskType);
            String sceneCode = taskBaseInfo.getString("sceneCode");
            String taskStatus = taskBaseInfo.getString("taskStatus");
            if (TaskStatus.FINISHED.name().equals(taskStatus)) {
              if (receiveTaskAward(sceneCode, taskType, taskTitle)) {
                doubleCheck = true;
              }
            } else if (TaskStatus.TODO.name().equals(taskStatus)) {
              if (bizInfo.optBoolean("autoCompleteTask", false)
                  || AntForestTaskTypeSet.contains(taskType) || taskType.endsWith("_JIASUQI")
                  || taskType.endsWith("_BAOHUDI") || taskType.startsWith("GYG")) {
                if (finishTask(sceneCode, taskType, taskTitle)) {
                  doubleCheck = true;
                }
              } else if ("DAKA_GROUP".equals(taskType)) {
                JSONArray childTaskTypeList = taskInfo.optJSONArray("childTaskTypeList");
                if (childTaskTypeList != null && childTaskTypeList.length() > 0) {
                  doChildTask(childTaskTypeList, taskTitle);
                }
              } else if ("TEST_LEAF_TASK".equals(taskType)) {
                JSONArray childTaskTypeList = taskInfo.optJSONArray("childTaskTypeList");
                if (childTaskTypeList != null && childTaskTypeList.length() > 0) {
                  doChildTask(childTaskTypeList, taskTitle);
                  doubleCheck = true;
                }
              }
            }
          }
        }
      }
    } catch (Throwable t) {
      Log.i(TAG, "queryTaskList err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private void queryTaskList(String firstTaskType, String taskType) {
    if (Status.hasFlagToday("vitalityTask::" + firstTaskType)) {
      return;
    }
    try {
      JSONObject jo = new JSONObject(
          AntForestRpcCall.queryTaskList(
              new JSONObject().put("firstTaskType", firstTaskType)
          )
      );
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }
      JSONArray taskInfoList = jo.getJSONArray("forestTasksNew").getJSONObject(0).getJSONArray("taskInfoList");
      for (int i = 0; i < taskInfoList.length(); i++) {
        jo = taskInfoList.getJSONObject(i).getJSONObject("taskBaseInfo");
        if (!Objects.equals(taskType, jo.getString("taskType"))) {
          continue;
        }
        boolean isReceived = TaskStatus.RECEIVED.name().equals(jo.getString("taskStatus"));
        if (!isReceived && TaskStatus.FINISHED.name().equals(jo.getString("taskStatus"))) {
          String sceneCode = jo.getString("sceneCode");
          String taskTitle = new JSONObject(jo.getString("bizInfo")).getString("taskTitle");
          isReceived = receiveTaskAward(sceneCode, taskType, taskTitle);
          TimeUtil.sleep(1000);
        }
        if (isReceived) {
          Status.flagToday("vitalityTask::" + firstTaskType);
        }
        return;
      }
    } catch (Throwable t) {
      Log.i(TAG, "queryTaskList err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private Boolean receiveTaskAward(String sceneCode, String taskType, String taskTitle) {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.receiveTaskAward(sceneCode, taskType));
      TimeUtil.sleep(500);
      if (MessageUtil.checkSuccess(TAG, jo)) {
        int incAwardCount = jo.optInt("incAwardCount", 1);
        Log.forest("森林任务🎖️领取[" + taskTitle + "]奖励#获得[" + incAwardCount + "活力值]");
        return true;
      }
    } catch (Throwable t) {
      Log.i(TAG, "receiveTaskAward err:");
      Log.printStackTrace(TAG, t);
    }
    return false;
  }

  private Boolean finishTask(String sceneCode, String taskType, String taskTitle) {
    if (MyUtils.closeUnRpc() && "ANTFOREST_VITALITY_TASK".equals(sceneCode) && taskType != null) {
      if (taskType.startsWith("GYG_BK_XYK")
          || taskType.startsWith("GYG_jinritoutiao")//完成任务逛一逛今日头条失败
          || taskType.startsWith("GYG_huabeikaitong")//完成任务逛一逛花呗失败
      ) {
        //方法: com.alipay.antiep.finishTask
        //参数: [{"outBizNo":"GYG_jinritoutiao_202505_0.3094441562418887","requestType":"H5","sceneCode":"ANTFOREST_VITALITY_TASK","source":"ANTFOREST","taskType":"GYG_jinritoutiao_202505"}]
        //数据: {"ariverRpcTraceId":"2197b86417545058757843021e3235","code":"400000040","desc":"不支持rpc完成的任务","success":false}

        //方法: com.alipay.antiep.finishTask
        //参数: [{"outBizNo":"GYG_huabeikaitong_202504_0.4569653304313023","requestType":"H5","sceneCode":"ANTFOREST_VITALITY_TASK","source":"ANTFOREST","taskType":"GYG_huabeikaitong_202504"}]
        //数据: {"ariverRpcTraceId":"21d101dc17545893616134198e0494","code":"400000040","desc":"不支持rpc完成的任务","success":false}
        return false;//不支持rpc完成的任务
      }
    }
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.finishTask(sceneCode, taskType));
      TimeUtil.sleep(500);
      if (MessageUtil.checkSuccess(TAG, jo)) {
        Log.forest("森林任务🧾️完成[" + taskTitle + "]");
        return true;
      }
      Log.record("完成任务" + taskTitle + "失败,");
    } catch (Throwable t) {
      Log.i(TAG, "finishTask err:");
      Log.printStackTrace(TAG, t);
    }
    return false;
  }

  private void doChildTask(JSONArray childTaskTypeList, String title) {
    try {
      for (int i = 0; i < childTaskTypeList.length(); i++) {
        JSONObject taskInfo = childTaskTypeList.getJSONObject(i);
        JSONObject taskBaseInfo = taskInfo.getJSONObject("taskBaseInfo");
        JSONObject bizInfo = new JSONObject(taskBaseInfo.getString("bizInfo"));
        String taskType = taskBaseInfo.getString("taskType");
        String taskTitle = bizInfo.optString("taskTitle", title);
        String sceneCode = taskBaseInfo.getString("sceneCode");
        String taskStatus = taskBaseInfo.getString("taskStatus");
        if (TaskStatus.TODO.name().equals(taskStatus)) {
          if (bizInfo.optBoolean("autoCompleteTask")) {
            finishTask(sceneCode, taskType, taskTitle);
          }
        }
      }
    } catch (Throwable th) {
      Log.i(TAG, "doChildTask err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private void startEnergyRain() {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.startEnergyRain());
      TimeUtil.sleep(500);
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }
      String token = jo.getString("token");
      JSONArray bubbleEnergyList = jo.getJSONObject("difficultyInfo")
          .getJSONArray("bubbleEnergyList");
      int sum = 0;
      for (int i = 0; i < bubbleEnergyList.length(); i++) {
        sum += bubbleEnergyList.getInt(i);
      }
      TimeUtil.sleep(5000L);
      jo = new JSONObject(AntForestRpcCall.energyRainSettlement(sum, token));
      if (MessageUtil.checkResultCode(TAG, jo)) {
        Toast.show("获得了[" + sum + "g]能量[能量雨]");
        Log.forest("收能量雨🌧️[" + sum + "g]");
        totalCollected += sum;
        Statistics.addData(Statistics.DataType.COLLECTED, sum);
      }
      TimeUtil.sleep(500);
    } catch (Throwable th) {
      Log.i(TAG, "startEnergyRain err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private void energyRain() {
    try {
      JSONObject joEnergyRainHome = new JSONObject(AntForestRpcCall.queryEnergyRainHome());
      TimeUtil.sleep(500);
      if (MessageUtil.checkResultCode(TAG, joEnergyRainHome)) {
        if (joEnergyRainHome.getBoolean("canPlayToday")) {
          startEnergyRain();
        }
        if (joEnergyRainHome.getBoolean("canGrantStatus")) {
          Log.record("有送能量雨的机会");
          JSONObject joEnergyRainCanGrantList = new JSONObject(
              AntForestRpcCall.queryEnergyRainCanGrantList());
          TimeUtil.sleep(500);
          JSONArray grantInfos = joEnergyRainCanGrantList.getJSONArray("grantInfos");
          Set<String> set = giveEnergyRainList.getValue();
          String userId;
          boolean granted = false;
          for (int j = 0; j < grantInfos.length(); j++) {
            JSONObject grantInfo = grantInfos.getJSONObject(j);
            if (grantInfo.getBoolean("canGrantedStatus")) {
              userId = grantInfo.getString("userId");
              if (set.contains(userId)) {
                JSONObject joEnergyRainChance = new JSONObject(
                    AntForestRpcCall.grantEnergyRainChance(userId));
                TimeUtil.sleep(500);
                Log.record("尝试送能量雨给【" + UserIdMap.getMaskName(userId) + "】");
                granted = true;
                // 20230724能量雨调整为列表中没有可赠送的好友则不赠送
                if (MessageUtil.checkResultCode(TAG, joEnergyRainChance)) {
                  Log.forest("送能量雨🌧️[" + UserIdMap.getMaskName(userId) + "]#"
                      + UserIdMap.getMaskName(UserIdMap.getCurrentUid()));
                  startEnergyRain();
                }
                break;
              }
            }
          }
          if (!granted) {
            Log.record("没有可以送的用户");
          }
        }
      }
      joEnergyRainHome = new JSONObject(AntForestRpcCall.queryEnergyRainHome());
      TimeUtil.sleep(500);
      if (MessageUtil.checkResultCode(TAG, joEnergyRainHome)
          && joEnergyRainHome.getBoolean("canPlayToday")) {
        startEnergyRain();
      }
    } catch (Throwable th) {
      Log.i(TAG, "energyRain err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private void usePropBeforeCollectEnergy(String userId) {
    if (Objects.equals(selfId, userId)) {
      return;
    }
    if (needDoubleClick()) {
      synchronized (usePropLockObj) {
        if (needDoubleClick()) {
          useDoubleCard(getForestPropVOList());
        }
      }
    }
  }

  private Boolean needDoubleClick() {
    if (doubleClickType.getValue() == UsePropType.CLOSE) {
      return false;
    }
    Long doubleClickEndTime = usingProps.get(PropGroup.doubleClick.name());
    if (doubleClickEndTime == null) {
      return true;
    }
    return doubleClickEndTime < System.currentTimeMillis();
  }

  private void useDoubleCard(JSONArray forestPropVOList) {
    try {
      if (hasDoubleCardTime() && Status.canDoubleToday()) {
        // 背包查找 能量双击卡
        JSONObject jo = null;
        List<JSONObject> list = getPropGroup(forestPropVOList, PropGroup.doubleClick.name());
        if (!list.isEmpty()) {
          jo = list.get(0);
        }
        if (jo == null || !jo.has("recentExpireTime")) {
          if (doubleCardConstant.getValue()) {
            // 商店兑换 限时能量双击卡
            if (exchangeBenefit("SK20240805004754")) {
              jo = getForestPropVO(getForestPropVOList(), "ENERGY_DOUBLE_CLICK_31DAYS");
            } else if (exchangeBenefit("CR20230516000363")) {
              jo = getForestPropVO(getForestPropVOList(), "LIMIT_TIME_ENERGY_DOUBLE_CLICK");
            }
          }
        }
        if (jo == null) {
          return;
        }
        if (!jo.has("recentExpireTime") && doubleClickType.getValue() == UsePropType.ONLY_LIMIT_TIME) {
          return;
        }
        // 使用能量双击卡
        if (consumeProp(jo)) {
          Long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(
              jo.getJSONObject("propConfigVO").getLong("durationTime"));
          usingProps.put(PropGroup.doubleClick.name(), endTime);
          Status.DoubleToday();
        } else {
          updateUsingPropsEndTime();
        }
      }
    } catch (Throwable th) {
      Log.i(TAG, "useDoubleCard err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private boolean hasDoubleCardTime() {
    long currentTimeMillis = System.currentTimeMillis();
    return TimeUtil.checkInTimeRange(currentTimeMillis, doubleCardTime.getValue());
  }

  /* 赠送道具 */
  private void giveProp() {
    Set<String> set = whoYouWantToGiveTo.getValue();
    if (set.isEmpty()) {
      return;
    }
    for (String userId : set) {
      if (UserIdMap.getCurrentUid() == null
          || Objects.equals(UserIdMap.getCurrentUid(), userId)) {
        continue;
      }
      giveProp(userId);
      break;
    }
  }

  private void giveProp(String targetUserId) {
    try {
      do {
        try {
          JSONObject jo = new JSONObject(AntForestRpcCall.queryPropList(true));
          if (!MessageUtil.checkResultCode(TAG, jo)) {
            return;
          }
          JSONArray forestPropVOList = jo.optJSONArray("forestPropVOList");
          if (forestPropVOList != null && forestPropVOList.length() > 0) {
            jo = forestPropVOList.getJSONObject(0);
            String giveConfigId = jo.getJSONObject("giveConfigVO").getString("giveConfigId");
            int holdsNum = jo.optInt("holdsNum", 0);
            String propName = jo.getJSONObject("propConfigVO").getString("propName");
            String propId = jo.getJSONArray("propIdList").getString(0);
            jo = new JSONObject(AntForestRpcCall.giveProp(giveConfigId, propId, targetUserId));
            if (MessageUtil.checkResultCode(TAG, jo)) {
              Log.forest("赠送道具🎭[" + UserIdMap.getMaskName(targetUserId) + "]#" + propName);
              if (holdsNum > 1 || forestPropVOList.length() > 1) {
                continue;
              }
            }
          }
        } finally {
          TimeUtil.sleep(1500);
        }
        break;
      } while (true);
    } catch (Throwable th) {
      Log.i(TAG, "giveProp err:");
      Log.printStackTrace(TAG, th);
    }
  }

  /**
   * 绿色行动
   */
  private void ecoLife() {
    try {
      JSONObject jo = new JSONObject(EcoLifeRpcCall.queryHomePage());
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }
      JSONObject data = jo.getJSONObject("data");
      if (!data.getBoolean("openStatus")) {
        Log.forest("绿色任务☘未开通");
        jo = new JSONObject(EcoLifeRpcCall.openEcolife());
        if (!MessageUtil.checkResultCode(TAG, jo)) {
          return;
        }
        if (!String.valueOf(true).equals(JsonUtil.getValueByPath(jo, "data.opResult"))) {
          return;
        }
        Log.forest("绿色任务🍀报告大人，开通成功(～￣▽￣)～可以愉快的玩耍了");
        jo = new JSONObject(EcoLifeRpcCall.queryHomePage());
        if (!MessageUtil.checkResultCode(TAG, jo)) {
          return;
        }
        data = jo.getJSONObject("data");
      }
      String dayPoint = data.getString("dayPoint");
      JSONArray actionListVO = data.getJSONArray("actionListVO");
      if (ecoLifeOptions.getValue().contains("tick")) {
        ecoLifeTick(actionListVO, dayPoint);
      }
      if (ecoLifeOptions.getValue().contains("dish")) {
        photoGuangPan(dayPoint);
      }
    } catch (Throwable th) {
      Log.i(TAG, "ecoLife err:");
      Log.printStackTrace(TAG, th);
    }
  }

  /* 绿色行动打卡 */

  private void ecoLifeTick(JSONArray actionListVO, String dayPoint) {
    try {
      String source = "source";
      for (int i = 0; i < actionListVO.length(); i++) {
        JSONObject actionVO = actionListVO.getJSONObject(i);
        JSONArray actionItemList = actionVO.getJSONArray("actionItemList");
        for (int j = 0; j < actionItemList.length(); j++) {
          JSONObject actionItem = actionItemList.getJSONObject(j);
          if (!actionItem.has("actionId")) {
            continue;
          }
          if (actionItem.getBoolean("actionStatus")) {
            continue;
          }
          String actionId = actionItem.getString("actionId");
          String actionName = actionItem.getString("actionName");
          if ("photoguangpan".equals(actionId)) {
            continue;
          }
          JSONObject jo = new JSONObject(EcoLifeRpcCall.tick(actionId, dayPoint, source));
          if (MessageUtil.checkResultCode(TAG, jo)) {
            Log.forest("绿色打卡🍀[" + actionName + "]");
          }
          TimeUtil.sleep(500);
        }
      }
    } catch (Throwable th) {
      Log.i(TAG, "ecoLifeTick err:");
      Log.printStackTrace(TAG, th);
    }
  }

  /**
   * 光盘行动
   */
  private void photoGuangPan(String dayPoint) {
    if (!TaskCommon.IS_AFTER_6AM) {
      return;
    }
    try {
      String source = "renwuGD";
      //检查今日任务状态
      JSONObject jo = new JSONObject(EcoLifeRpcCall.queryDish(source, dayPoint));
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }

      // 更新光盘照片
      Map<String, String> dishImage = new HashMap<>();
      JSONObject data = jo.optJSONObject("data");
      if (data != null) {
        String beforeMealsImageUrl = data.optString("beforeMealsImageUrl");
        String afterMealsImageUrl = data.optString("afterMealsImageUrl");
        if (!StringUtil.isEmpty(beforeMealsImageUrl) && !StringUtil.isEmpty(afterMealsImageUrl)) {
          Pattern pattern = Pattern.compile("img/(.*)/original");
          Matcher beforeMatcher = pattern.matcher(beforeMealsImageUrl);
          if (beforeMatcher.find()) {
            dishImage.put("BEFORE_MEALS", beforeMatcher.group(1));
          }
          Matcher afterMatcher = pattern.matcher(afterMealsImageUrl);
          if (afterMatcher.find()) {
            dishImage.put("AFTER_MEALS", afterMatcher.group(1));
          }
          TokenConfig.saveDishImage(dishImage);
        }
      }
      if (Objects.equals("SUCCESS", jo.getJSONObject("data").getString("status"))) {
        //Log.forest("光盘行动💿今日打卡已完成");
        return;
      }

      dishImage = TokenConfig.getRandomDishImage();
      if (dishImage == null) {
        Log.forest("光盘行动💿请先完成一次光盘打卡");
        return;
      }
      //上传餐前照片
      jo = new JSONObject(EcoLifeRpcCall.uploadBeforeMealsDishImage(dishImage.get("BEFORE_MEALS"), dayPoint));
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }
      //上传餐后照片
      jo = new JSONObject(EcoLifeRpcCall.uploadAfterMealsDishImage(dishImage.get("AFTER_MEALS"), dayPoint));
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }
      //提交
      jo = new JSONObject(EcoLifeRpcCall.tick("photoguangpan", dayPoint, source));
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }
      String toastMsg = jo.getJSONObject("data").getString("toastMsg");
      Log.forest("光盘行动💿打卡完成#" + toastMsg);
    } catch (Throwable t) {
      Log.i(TAG, "photoGuangPan err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private void queryUserPatrol() {
    try {
      th:
      do {
        JSONObject jo = new JSONObject(AntForestRpcCall.queryUserPatrol());
        TimeUtil.sleep(500);
        if (!MessageUtil.checkResultCode(TAG, jo)) {
          return;
        }
        JSONObject resData = new JSONObject(AntForestRpcCall.queryMyPatrolRecord());
        TimeUtil.sleep(500);
        if (resData.optBoolean("canSwitch")) {
          JSONArray records = resData.getJSONArray("records");
          for (int i = 0; i < records.length(); i++) {
            JSONObject record = records.getJSONObject(i);
            JSONObject userPatrol = record.getJSONObject("userPatrol");
            if (userPatrol.getInt("unreachedNodeCount") > 0) {
              if ("silent".equals(userPatrol.getString("mode"))) {
                JSONObject patrolConfig = record.getJSONObject("patrolConfig");
                String patrolId = patrolConfig.getString("patrolId");
                resData = new JSONObject(AntForestRpcCall.switchUserPatrol(patrolId));
                TimeUtil.sleep(500);
                if (MessageUtil.checkResultCode(TAG, resData)) {
                  Log.forest("巡护⚖️-切换地图至" + patrolId);
                }
                continue th;
              }
              break;
            }
          }
        }

        JSONObject userPatrol = jo.getJSONObject("userPatrol");
        int currentNode = userPatrol.getInt("currentNode");
        String currentStatus = userPatrol.getString("currentStatus");
        int patrolId = userPatrol.getInt("patrolId");
        JSONObject chance = userPatrol.getJSONObject("chance");
        int leftChance = chance.getInt("leftChance");
        int leftStep = chance.getInt("leftStep");
        int usedStep = chance.getInt("usedStep");
        if ("STANDING".equals(currentStatus)) {
          if (leftChance > 0) {
            jo = new JSONObject(AntForestRpcCall.patrolGo(currentNode, patrolId));
            TimeUtil.sleep(500);
            patrolKeepGoing(jo.toString(), currentNode, patrolId);
            continue;
          } else if (leftStep >= 2000 && usedStep < 10000) {
            jo = new JSONObject(AntForestRpcCall.exchangePatrolChance(leftStep));
            TimeUtil.sleep(300);
            if (MessageUtil.checkResultCode(TAG, jo)) {
              int addedChance = jo.optInt("addedChance", 0);
              Log.forest("步数兑换⚖️[巡护次数*" + addedChance + "]");
              continue;
            }
          }
        } else if ("GOING".equals(currentStatus)) {
          patrolKeepGoing(null, currentNode, patrolId);
        }
        break;
      } while (true);
    } catch (Throwable t) {
      Log.i(TAG, "queryUserPatrol err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private void patrolKeepGoing(String s, int nodeIndex, int patrolId) {
    try {
      do {
        if (s == null) {
          s = AntForestRpcCall.patrolKeepGoing(nodeIndex, patrolId, "image");
        }
        JSONObject jo = new JSONObject(s);
        if (!MessageUtil.checkResultCode(TAG, jo)) {
          return;
        }
        JSONArray jaEvents = jo.optJSONArray("events");
        if (jaEvents == null || jaEvents.length() == 0) {
          return;
        }
        JSONObject userPatrol = jo.getJSONObject("userPatrol");
        int currentNode = userPatrol.getInt("currentNode");
        JSONObject events = jo.getJSONArray("events").getJSONObject(0);
        JSONObject rewardInfo = events.optJSONObject("rewardInfo");
        if (rewardInfo != null) {
          JSONObject animalProp = rewardInfo.optJSONObject("animalProp");
          if (animalProp != null) {
            JSONObject animal = animalProp.optJSONObject("animal");
            if (animal != null) {
              Log.forest("巡护森林🏇🏻[" + animal.getString("name") + "碎片]");
            }
          }
        }
        if (!"GOING".equals(jo.getString("currentStatus"))) {
          return;
        }
        JSONObject materialInfo = events.getJSONObject("materialInfo");
        String materialType = materialInfo.optString("materialType", "image");
        s = AntForestRpcCall.patrolKeepGoing(currentNode, patrolId, materialType);
        TimeUtil.sleep(100);
      } while (true);
    } catch (Throwable t) {
      Log.i(TAG, "patrolKeepGoing err:");
      Log.printStackTrace(TAG, t);
    }
  }

  // 查询可派遣伙伴
  private void queryAnimalPropList() {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.queryAnimalPropList());
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }
      JSONArray animalProps = jo.getJSONArray("animalProps");
      JSONObject animalProp = null;
      for (int i = 0; i < animalProps.length(); i++) {
        jo = animalProps.getJSONObject(i);
        if (animalProp == null) {
          animalProp = jo;
          if (consumeAnimalPropType.getValue() == ConsumeAnimalPropType.SEQUENCE) {
            break;
          }
        } else if (jo.getJSONObject("main").getInt("holdsNum") > animalProp.getJSONObject("main").getInt("holdsNum")) {
          animalProp = jo;
        }
      }
      consumeAnimalProp(animalProp);
    } catch (Throwable t) {
      Log.i(TAG, "queryAnimalPropList err:");
      Log.printStackTrace(TAG, t);
    }
  }

  // 派遣伙伴
  private void consumeAnimalProp(JSONObject animalProp) {
    if (animalProp == null) {
      return;
    }
    try {
      String propGroup = animalProp.getJSONObject("main").getString("propGroup");
      String propType = animalProp.getJSONObject("main").getString("propType");
      String name = animalProp.getJSONObject("partner").getString("name");
      JSONObject jo = new JSONObject(AntForestRpcCall.consumeProp(propGroup, propType, false));
      if (MessageUtil.checkResultCode(TAG, jo)) {
        Log.forest("巡护派遣🐆[" + name + "]");
      }
    } catch (Throwable t) {
      Log.i(TAG, "consumeAnimalProp err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private void queryAnimalAndPiece() {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.queryAnimalAndPiece(0));
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return;
      }
      JSONArray animalProps = jo.getJSONArray("animalProps");
      for (int i = 0; i < animalProps.length(); i++) {
        boolean canCombineAnimalPiece = true;
        jo = animalProps.getJSONObject(i);
        JSONArray pieces = jo.getJSONArray("pieces");
        int id = jo.getJSONObject("animal").getInt("id");
        for (int j = 0; j < pieces.length(); j++) {
          jo = pieces.optJSONObject(j);
          if (jo == null || jo.optInt("holdsNum", 0) <= 0) {
            canCombineAnimalPiece = false;
            break;
          }
        }
        if (canCombineAnimalPiece) {
          combineAnimalPiece(id);
        }
      }
    } catch (Throwable t) {
      Log.i(TAG, "queryAnimalAndPiece err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private void combineAnimalPiece(int animalId) {
    try {
      do {
        JSONObject jo = new JSONObject(AntForestRpcCall.queryAnimalAndPiece(animalId));
        if (!MessageUtil.checkResultCode(TAG, jo)) {
          return;
        }
        JSONArray animalProps = jo.getJSONArray("animalProps");
        jo = animalProps.getJSONObject(0);
        JSONObject animal = jo.getJSONObject("animal");
        int id = animal.getInt("id");
        String name = animal.getString("name");
        JSONArray pieces = jo.getJSONArray("pieces");
        boolean canCombineAnimalPiece = true;
        JSONArray piecePropIds = new JSONArray();
        for (int j = 0; j < pieces.length(); j++) {
          jo = pieces.optJSONObject(j);
          if (jo == null || jo.optInt("holdsNum", 0) <= 0) {
            canCombineAnimalPiece = false;
            break;
          } else {
            piecePropIds.put(jo.getJSONArray("propIdList").getString(0));
          }
        }
        if (canCombineAnimalPiece) {
          jo = new JSONObject(AntForestRpcCall.combineAnimalPiece(id, piecePropIds.toString()));
          if (MessageUtil.checkResultCode(TAG, jo)) {
            Log.forest("合成动物💡[" + name + "]");
            animalId = id;
            TimeUtil.sleep(100);
            continue;
          }
        }
        break;
      } while (true);
    } catch (Throwable t) {
      Log.i(TAG, "combineAnimalPiece err:");
      Log.printStackTrace(TAG, t);
    }
  }

  private int forFriendCollectEnergy(String targetUserId, long bubbleId) {
    int helped = 0;
    try {
      String s = AntForestRpcCall.forFriendCollectEnergy(targetUserId, bubbleId);
      JSONObject jo = new JSONObject(s);
      if ("SUCCESS".equals(jo.getString("resultCode"))) {
        JSONArray jaBubbles = jo.getJSONArray("bubbles");
        for (int i = 0; i < jaBubbles.length(); i++) {
          jo = jaBubbles.getJSONObject(i);
          helped += jo.getInt("collectedEnergy");
        }
        if (helped > 0) {
          Log.forest("帮收能量🧺[" + UserIdMap.getMaskName(targetUserId) + "]#" + helped + "g");
          totalHelpCollected += helped;
          Statistics.addData(Statistics.DataType.HELPED, helped);
        } else {
          Log.record("帮[" + UserIdMap.getMaskName(targetUserId) + "]收取失败");
          Log.i("，UserID：" + targetUserId + "，BubbleId" + bubbleId);
        }
      } else {
        Log.record("[" + UserIdMap.getMaskName(targetUserId) + "]" + jo.getString("resultDesc"));
        Log.i(s);
      }
    } catch (Throwable t) {
      Log.i(TAG, "forFriendCollectEnergy err:");
      Log.printStackTrace(TAG, t);
    }
    return helped;
  }

  public static JSONArray getForestPropVOList() {
    JSONArray forestPropVOList = new JSONArray();
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.queryPropList(false));
      if (MessageUtil.checkResultCode(TAG, jo)) {
        forestPropVOList = jo.getJSONArray("forestPropVOList");
      }
    } catch (Throwable th) {
      Log.i(TAG, "getForestPropVOList err:");
      Log.printStackTrace(TAG, th);
    }
    return forestPropVOList;
  }

  // 获取道具组全部道具
  public static List<JSONObject> getPropGroup(JSONArray forestPropVOList, String propGroup) {
    List<JSONObject> list = new ArrayList<>();
    try {
      for (int i = 0; i < forestPropVOList.length(); i++) {
        JSONObject forestPropVO = forestPropVOList.getJSONObject(i);
        if (forestPropVO.getString("propGroup").equals(propGroup)) {
          list.add(forestPropVO);
        }
      }
      Collections.sort(list, new Comparator<JSONObject>() {
        @Override
        public int compare(JSONObject jsonObject1, JSONObject jsonObject2) {
          try {
            int durationTime1 = jsonObject1.getJSONObject("propConfigVO").getInt("durationTime");
            int durationTime2 = jsonObject2.getJSONObject("propConfigVO").getInt("durationTime");
            boolean hasExpireTime1 = jsonObject1.has("recentExpireTime");
            boolean hasExpireTime2 = jsonObject2.has("recentExpireTime");
            if (hasExpireTime1 && hasExpireTime2) {
              long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(durationTime1);
              long recentExpireTime = jsonObject2.getLong("recentExpireTime");
              if (endTime < recentExpireTime) {
                return -1;
              } else return durationTime2 - durationTime1;
            } else if (!hasExpireTime1 && !hasExpireTime2) {
              return durationTime1 - durationTime2;
            } else {
              return hasExpireTime1 ? -1 : 1;
            }
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }
      });
    } catch (Throwable th) {
      Log.i(TAG, "getPropGroup err:");
      Log.printStackTrace(TAG, th);
    }
    return list;
  }

  /*
   * 查找背包道具
   * prop
   * propGroup, propType, holdsNum, propIdList[], propConfigVO[propName]
   */
  private JSONObject getForestPropVO(JSONArray forestPropVOList, String propType) {
    try {
      for (int i = 0; i < forestPropVOList.length(); i++) {
        JSONObject forestPropVO = forestPropVOList.getJSONObject(i);
        if (forestPropVO.getString("propType").equals(propType)) {
          return forestPropVO;
        }
      }
    } catch (Throwable th) {
      Log.i(TAG, "getForestPropVO err:");
      Log.printStackTrace(TAG, th);
    }
    return null;
  }

  /*
   * 使用背包道具
   * prop
   * propGroup, propType, holdsNum, propIdList[], propConfigVO[propName]
   */
  public static Boolean consumeProp(JSONObject prop) {
    try {
      // 使用道具
      String propId = prop.getJSONArray("propIdList").getString(0);
      String propType = prop.getString("propType");
      String propName = prop.getJSONObject("propConfigVO").getString("propName");
      return consumeProp(propId, propType, propName);
    } catch (Throwable th) {
      Log.i(TAG, "consumeProp err:");
      Log.printStackTrace(TAG, th);
    }
    return false;
  }

  private static Boolean consumeProp(String propId, String propType, String propName) {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.consumeProp(propId, propType));
      if (MessageUtil.checkResultCode(TAG, jo)) {
        Log.forest("使用道具🎭[" + propName + "]");
        return true;
      }
    } catch (Throwable th) {
      Log.i(TAG, "consumeProp err:");
      Log.printStackTrace(TAG, th);
    }
    return false;
  }

  // 获取活力值商店列表
  private JSONArray getVitalityItemList(String labelType) {
    JSONArray itemInfoVOList = null;
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.itemList(labelType));
      if (MessageUtil.checkSuccess(TAG, jo)) {
        itemInfoVOList = jo.optJSONArray("itemInfoVOList");
      }
    } catch (Throwable th) {
      Log.i(TAG, "getVitalityItemList err:");
      Log.printStackTrace(TAG, th);
    }
    return itemInfoVOList;
  }

  // 获取活力值商店所有商品信息
  private void getAllSkuInfo() {
    try {
      JSONArray itemInfoVOList = getVitalityItemList("SC_ASSETS");
      if (itemInfoVOList == null) {
        return;
      }
      for (int i = 0; i < itemInfoVOList.length(); i++) {
        JSONObject itemInfoVO = itemInfoVOList.getJSONObject(i);
        getSkuInfoByItemInfoVO(itemInfoVO);
      }
    } catch (Throwable th) {
      Log.i(TAG, "getAllSkuInfo err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private void getSkuInfoBySpuId(String spuId) {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.itemDetail(spuId));
      if (!MessageUtil.checkSuccess(TAG, jo)) {
        return;
      }
      JSONObject spuItemInfoVo = jo.getJSONObject("spuItemInfoVO");
      getSkuInfoByItemInfoVO(spuItemInfoVo);
    } catch (Throwable th) {
      Log.i(TAG, "getSkuInfoBySpuId err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private void getSkuInfoByItemInfoVO(JSONObject spuItem) {
    try {
      String spuId = spuItem.getString("spuId");
      JSONArray skuModelList = spuItem.getJSONArray("skuModelList");
      for (int i = 0; i < skuModelList.length(); i++) {
        JSONObject skuModel = skuModelList.getJSONObject(i);
        String skuId = skuModel.getString("skuId");
        String skuName = skuModel.getString("skuName");
        if (!skuModel.has("spuId")) {
          skuModel.put("spuId", spuId);
        }
        skuInfo.put(skuId, skuModel);
        VitalityBenefitIdMap.add(skuId, skuName);
      }
      VitalityBenefitIdMap.save(UserIdMap.getCurrentUid());
    } catch (Throwable th) {
      Log.i(TAG, "getSkuInfoByItemInfoVO err:");
      Log.printStackTrace(TAG, th);
    }
  }

  /*
   * 兑换活力值商品
   * sku
   * spuId, skuId, skuName, exchangedCount, price[amount]
   * exchangedCount == 0......
   */
  private Boolean exchangeBenefit(String skuId) {
    if (skuInfo.isEmpty()) {
      getAllSkuInfo();
    }
    JSONObject sku = skuInfo.get(skuId);
    if (sku == null) {
      Log.record("活力兑换🎐找不到要兑换的权益！");
      return false;
    }
    try {
      String skuName = sku.getString("skuName");
      JSONArray itemStatusList = sku.getJSONArray("itemStatusList");
      for (int i = 0; i < itemStatusList.length(); i++) {
        String itemStatus = itemStatusList.getString(i);
        if (ItemStatus.REACH_LIMIT.name().equals(itemStatus)
            || ItemStatus.NO_ENOUGH_POINT.name().equals(itemStatus)
            || ItemStatus.NO_ENOUGH_STOCK.name().equals(itemStatus)) {
          Log.record("活力兑换🎐[" + skuName + "]停止:" + ItemStatus.valueOf(itemStatus).nickName());
          if (ItemStatus.REACH_LIMIT.name().equals(itemStatus)) {
            Status.flagToday("forest::exchangeLimit::" + skuId);
          }
          return false;
        }
      }
      String spuId = sku.getString("spuId");
      if (exchangeBenefit(spuId, skuId, skuName)) {
        return true;
      }
      getSkuInfoBySpuId(spuId);
    } catch (Throwable th) {
      Log.i(TAG, "exchangeBenefit err:");
      Log.printStackTrace(TAG, th);
    }
    return false;
  }

  public static Boolean exchangeBenefit(String spuId, String skuId, String skuName) {
    try {
      if (exchangeBenefit(spuId, skuId)) {
        Status.vitalityExchangeBenefitToday(skuId);
        int exchangedCount = Status.getVitalityExchangeBenefitCountToday(skuId);
        Log.forest("活力兑换🎐[" + skuName + "]#第" + exchangedCount + "次");
        return true;
      }
    } catch (Throwable th) {
      Log.i(TAG, "exchangeBenefit err:");
      Log.printStackTrace(TAG, th);
    }
    return false;
  }

  private static Boolean exchangeBenefit(String spuId, String skuId) {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.exchangeBenefit(spuId, skuId));
      return MessageUtil.checkResultCode(TAG, jo);
    } catch (Throwable th) {
      Log.i(TAG, "exchangeBenefit err:");
      Log.printStackTrace(TAG, th);
    }
    return false;
  }

  private void dress() {
    String dressDetail = dressDetailList.getValue();
    if (dressDetail.isEmpty()) {
      setDressDetail(getDressDetail().toString());
    } else {
      checkDressDetail(dressDetail);
    }
  }

  private JSONObject getDressDetail() {
    JSONObject dressDetail = new JSONObject();
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.queryHomePage());
      JSONArray ja = jo.getJSONObject("indexDressVO")
          .getJSONArray("dressDetailList");
      for (int i = 0; i < ja.length(); i++) {
        jo = ja.getJSONObject(i);
        String position = jo.getString("position");
        String batchType = jo.getString("batchType");
        dressDetail.put(position, batchType);
      }
    } catch (Throwable th) {
      Log.i(TAG, "getDressDetail err:");
      Log.printStackTrace(TAG, th);
    }
    return dressDetail;
  }

  private void setDressDetail(String dressDetail) {
    dressDetailList.setValue(dressDetail);
    if (ConfigV2.save(UserIdMap.getCurrentUid(), false)) {
      Log.forest("装扮保护🔐皮肤保存,芝麻粒将为你持续保护!");
    }
  }

  private void removeDressDetail(String position) {
    JSONObject jo = getDressDetail();
    jo.remove(position);
    setDressDetail(jo.toString());
  }

  private void checkDressDetail(String dressDetail) {
    String[] positions = {
        "tree__main",
        "bg__sky_0",
        "bg__sky_cloud",
        "bg__ground_a",
        "bg__ground_b",
        "bg__ground_c"
    };
    try {
      boolean isDressExchanged = false;
      JSONObject jo = new JSONObject(dressDetail);
      for (String position : positions) {
        String batchType = "";
        if (jo.has(position)) {
          batchType = jo.getString(position);
        }
        if (queryUserDressForBackpack(dressMap.get(position), batchType)) {
          isDressExchanged = true;
        }
      }
      if (isDressExchanged) {
        Log.forest("装扮保护🔐皮肤修改,芝麻粒已为你自动恢复!");
      }
    } catch (Throwable th) {
      Log.i(TAG, "checkDressDetail err:");
      Log.printStackTrace(TAG, th);
    }
  }

  private Boolean queryUserDressForBackpack(String positionType, String batchType) {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.listUserDressForBackpack(positionType));
      if (!MessageUtil.checkResultCode(TAG, jo)) {
        return false;
      }
      JSONArray userHoldDressVOList = jo.getJSONArray("userHoldDressVOList");
      boolean isTakeOff = false;
      for (int i = 0; i < userHoldDressVOList.length(); i++) {
        jo = userHoldDressVOList.getJSONObject(i);
        if (jo.optInt("remainNum", 1) == 0) {
          if (batchType.equals(jo.getString("batchType"))) {
            return false;
          }
          String position = jo.getJSONArray("posList").getString(0);
          isTakeOff = takeOffDress(jo.getString("dressType"), position);
        } else if (batchType.equals(jo.getString("batchType"))) {
          return wearDress(jo.getString("dressType"));
        }
      }

      if (!batchType.isEmpty()) {
        removeDressDetail(dressMap.get(positionType));
        Log.forest("装扮保护🔐皮肤过期,芝麻粒已为你恢复默认!");
      }
      return isTakeOff;
    } catch (Throwable th) {
      Log.i(TAG, "queryUserDressForBackpack err:");
      Log.printStackTrace(TAG, th);
    }
    return false;
  }

  private Boolean wearDress(String dressType) {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.wearDress(dressType));
      return MessageUtil.checkResultCode(TAG, jo);
    } catch (Throwable th) {
      Log.i(TAG, "wearDress err:");
      Log.printStackTrace(TAG, th);
    }
    return false;
  }

  private Boolean takeOffDress(String dressType, String position) {
    try {
      JSONObject jo = new JSONObject(AntForestRpcCall.takeOffDress(dressType, position));
      return MessageUtil.checkResultCode(TAG, jo);
    } catch (Throwable th) {
      Log.i(TAG, "takeOffDress err:");
      Log.printStackTrace(TAG, th);
    }
    return false;
  }

  /**
   * The enum Collect status.
   */
  public enum CollectStatus {
    /**
     * Available collect status.
     */
    AVAILABLE,
    /**
     * Waiting collect status.
     */
    WAITING,
    /**
     * Insufficient collect status.
     */
    INSUFFICIENT,
    /**
     * Robbed collect status.
     */
    ROBBED
  }

  /**
   * The type Bubble timer task.
   */
  private class BubbleTimerTask extends ChildModelTask {

    /**
     * The User id.
     */
    private final String userId;
    /**
     * The Bubble id.
     */
    private final long bubbleId;
    /**
     * The ProduceTime.
     */
    private final long produceTime;

    /**
     * Instantiates a new Bubble timer task.
     */
    BubbleTimerTask(String ui, long bi, long pt) {
      super(AntForestV2.getBubbleTimerTid(ui, bi), pt - advanceTimeInt);
      userId = ui;
      bubbleId = bi;
      produceTime = pt;
    }

    @Override
    public Runnable setRunnable() {
      return () -> {
        String userName = UserIdMap.getMaskName(userId);
        int averageInteger = offsetTimeMath.getAverageInteger();
        long readyTime = produceTime - advanceTimeInt + averageInteger - delayTimeMath.getAverageInteger() - System.currentTimeMillis() + 70;
        if (readyTime > 0) {
          try {
            Thread.sleep(readyTime);
          } catch (InterruptedException e) {
            Log.i("终止[" + userName + "]蹲点收取任务, 任务ID[" + getId() + "]");
            return;
          }
        }
        Log.record("执行蹲点收取[" + userName + "]" + "时差[" + averageInteger + "]ms" + "提前[" + advanceTimeInt + "]ms");
        collectEnergy(new CollectEnergyEntity(userId, null, AntForestRpcCall.getCollectEnergyRpcEntity(null, userId, bubbleId)), true);
      };
    }
  }

  public static String getBubbleTimerTid(String ui, long bi) {
    return "BT|" + ui + "|" + bi;
  }

  public enum ItemStatus {
    NO_ENOUGH_POINT, NO_ENOUGH_STOCK, REACH_LIMIT, SECKILL_NOT_BEGIN, SECKILL_HAS_END, HAS_NEVER_EXPIRE_DRESS;

    public static final String[] nickNames = {"活力值不足", "库存量不足", "兑换达上限", "秒杀未开始", "秒杀已结束", "不限时皮肤"};

    public String nickName() {
      return nickNames[ordinal()];
    }
  }

  public enum PropGroup {
    shield, boost, doubleClick, vitalitySignDouble, stealthCard, robExpandCard;

    public static final String[] nickNames = {"能量保护罩", "时光加速器", "能量双击卡", "活力翻倍卡", "隐身卡", "能量翻倍卡"};

    public String nickName() {
      return nickNames[ordinal()];
    }
  }

  public interface WaterFriendType {

    int WATER_00 = 0;
    int WATER_10 = 1;
    int WATER_18 = 2;
    int WATER_33 = 3;
    int WATER_66 = 4;

    String[] nickNames = {"不浇水", "浇水10克", "浇水18克", "浇水33克", "浇水66克"};
    int[] waterEnergy = {0, 10, 18, 33, 66};
  }

  public interface HelpFriendCollectType {

    int NONE = 0;
    int HELP = 1;
    int NOT_HELP = 2;

    String[] nickNames = {"不复活能量", "复活已选好友", "复活未选好友"};
  }

  public interface ConsumeAnimalPropType {

    int NONE = 0;
    int SEQUENCE = 1;
    int QUANTITY = 2;

    String[] nickNames = {"不派遣动物", "按默认顺序派遣", "按最大数量派遣"};
  }

  public interface UsePropType {

    int CLOSE = 0;
    int ALL = 1;
    int ONLY_LIMIT_TIME = 2;

    String[] nickNames = {"关闭", "所有道具", "限时道具"};
  }
}
