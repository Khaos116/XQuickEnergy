package fansirsqi.xposed.sesame.task.antSports;

import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import fansirsqi.xposed.sesame.data.Status;
import fansirsqi.xposed.sesame.data.StatusFlags;
import fansirsqi.xposed.sesame.entity.AlipayUser;
import fansirsqi.xposed.sesame.hook.ApplicationHook;
import fansirsqi.xposed.sesame.model.BaseModel;
import fansirsqi.xposed.sesame.util.*;
import fansirsqi.xposed.sesame.model.ModelFields;
import fansirsqi.xposed.sesame.model.ModelGroup;
import fansirsqi.xposed.sesame.model.modelFieldExt.BooleanModelField;
import fansirsqi.xposed.sesame.model.modelFieldExt.ChoiceModelField;
import fansirsqi.xposed.sesame.model.modelFieldExt.IntegerModelField;
import fansirsqi.xposed.sesame.model.modelFieldExt.SelectModelField;
import fansirsqi.xposed.sesame.model.modelFieldExt.StringModelField;
import fansirsqi.xposed.sesame.task.ModelTask;
import fansirsqi.xposed.sesame.task.TaskCommon;
import fansirsqi.xposed.sesame.util.maps.UserMap;

import static fansirsqi.xposed.sesame.util.GlobalThreadPools.sleepCompat;

@SuppressLint("DefaultLocale")
public class AntSports extends ModelTask {
    private static final String TAG = AntSports.class.getSimpleName();
    private static final String SPORTS_TASKS_COMPLETED_DATE = "SPORTS_TASKS_COMPLETED_DATE"; // 运动任务完成日期缓存键
    private static final String TRAIN_FRIEND_ZERO_COIN_DATE = "TRAIN_FRIEND_ZERO_COIN_DATE"; // 训练好友0金币达上限日期缓存键
    private int tmpStepCount = -1;
    private BooleanModelField walk;
    private ChoiceModelField walkPathTheme;
    private String walkPathThemeId;
    private BooleanModelField walkCustomPath;
    private StringModelField walkCustomPathId;
    private BooleanModelField openTreasureBox;
    private BooleanModelField receiveCoinAsset;
    private BooleanModelField donateCharityCoin;
    private ChoiceModelField donateCharityCoinType;
    private IntegerModelField donateCharityCoinAmount;
    private IntegerModelField minExchangeCount;
    private IntegerModelField latestExchangeTime;
    private IntegerModelField syncStepCount;
    private BooleanModelField tiyubiz;
    private BooleanModelField battleForFriends; // 抢好友总开关
    private ChoiceModelField battleForFriendType;
    private SelectModelField originBossIdList;
    private BooleanModelField sportsTasks;
    private BooleanModelField sportsEnergyBubble;

    // 训练好友相关变量
    private BooleanModelField trainFriend;
    private IntegerModelField zeroCoinLimit;

    // 记录训练好友获得0金币的次数
    private int zeroTrainCoinCount = 0;


    //健康岛任务
    private BooleanModelField neverlandTask;  //健康岛任务
    private BooleanModelField neverlandGrid;    //健康岛走路

    private IntegerModelField neverlandGridStepCount;   //健康岛

    @Override
    public String getName() {
        return "运动";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.SPORTS;
    }

    @Override
    public String getIcon() {
        return "AntSports.png";
    }


    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(walk = new BooleanModelField("walk", "行走路线 | 开启", false));
        modelFields.addField(walkPathTheme = new ChoiceModelField("walkPathTheme", "行走路线 | 主题", WalkPathTheme.DA_MEI_ZHONG_GUO, WalkPathTheme.nickNames));
        modelFields.addField(walkCustomPath = new BooleanModelField("walkCustomPath", "行走路线 | 开启自定义路线", false));
        modelFields.addField(walkCustomPathId = new StringModelField("walkCustomPathId", "行走路线 | 自定义路线代码(debug)", "p0002023122214520001"));
        modelFields.addField(openTreasureBox = new BooleanModelField("openTreasureBox", "开启宝箱", false));
        modelFields.addField(sportsTasks = new BooleanModelField("sportsTasks", "开启运动任务", false));
        modelFields.addField(sportsEnergyBubble = new BooleanModelField("sportsEnergyBubble", "运动球任务(开启后有概率出现滑块验证)", false));
        //干就完了，都能做  modelFields.addField(sportsTaskBlacklist = new StringModelField("sportsTaskBlacklist", "运动任务黑名单 | 任务名称(用,分隔)", "开通包裹查询服务,添加目标应用小组件,领取价值1.7万元配置,目标应用积分可兑券"));
        modelFields.addField(receiveCoinAsset = new BooleanModelField("receiveCoinAsset", "收能量🎈", false));
        modelFields.addField(donateCharityCoin = new BooleanModelField("donateCharityCoin", "捐能量🎈 | 开启", false));
        modelFields.addField(donateCharityCoinType = new ChoiceModelField("donateCharityCoinType", "捐能量🎈 | 方式", DonateCharityCoinType.ONE, DonateCharityCoinType.nickNames));
        modelFields.addField(donateCharityCoinAmount = new IntegerModelField("donateCharityCoinAmount", "捐能量🎈 | 数量(每次)", 100));
        // 健康岛任务
        modelFields.addField(neverlandTask =  new BooleanModelField("neverlandTask", "健康岛 | 任务", false));
        modelFields.addField(neverlandGrid =  new BooleanModelField("neverlandGrid", "健康岛 | 自动走路建造", false));
        modelFields.addField(neverlandGridStepCount = new IntegerModelField("neverlandGridStepCount", "健康岛 | 今日走路最大次数", 20));
        // 抢好友相关配置
        modelFields.addField(battleForFriends = new BooleanModelField("battleForFriends", "抢好友 | 开启", false));
        modelFields.addField(battleForFriendType = new ChoiceModelField("battleForFriendType", "抢好友 | 动作", BattleForFriendType.ROB, BattleForFriendType.nickNames));
        modelFields.addField(originBossIdList = new SelectModelField("originBossIdList", "抢好友 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));

        // 训练好友相关配置
        modelFields.addField(trainFriend = new BooleanModelField("trainFriend", "训练好友 | 开启", false));
        modelFields.addField(zeroCoinLimit = new IntegerModelField("zeroCoinLimit", "训练好友 | 0金币上限次数当天关闭", 5));

        modelFields.addField(tiyubiz = new BooleanModelField("tiyubiz", "文体中心", false));
        modelFields.addField(minExchangeCount = new IntegerModelField("minExchangeCount", "最小捐步步数", 0));
        modelFields.addField(latestExchangeTime = new IntegerModelField("latestExchangeTime", "最晚捐步时间(24小时制)", 22));
        modelFields.addField(syncStepCount = new IntegerModelField("syncStepCount", "自定义同步步数", 22000));
        // 本地变量，用于添加字段到模型
        BooleanModelField coinExchangeDoubleCard = new BooleanModelField("coinExchangeDoubleCard", "能量🎈兑换限时能量双击卡", false);
        modelFields.addField(coinExchangeDoubleCard);
        return modelFields;
    }

    @Override
    public void boot(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod("com.alibaba.health.pedometer.core.datasource.PedometerAgent", classLoader,
                    "readDailyStep", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            int originStep = (Integer) param.getResult();
                            int step = tmpStepCount();
                            if (TaskCommon.IS_AFTER_8AM && originStep < step) {//早于8点或步数小于自定义步数hook
                                param.setResult(step);
                            }
                        }
                    });
            Log.record(TAG, "hook readDailyStep successfully");
        } catch (Throwable t) {
            Log.printStackTrace(TAG,"hook readDailyStep err:", t);
        }
    }

    /**
     * 执行运动任务的主要逻辑
     */
    @Override
    protected void runJava() {
        Log.record(TAG, "执行开始-" + getName());
        try {

            if (neverlandTask.getValue()||neverlandGrid.getValue()) {
                Log.record(TAG, "开始执行健康岛");
                NeverlandTaskHandler handler = new NeverlandTaskHandler();
                handler.runNeverland();
                Log.record(TAG, "健康岛结束");
            }
            MyUtils.CHANGE_KT8.trim();
            //if (!Status.hasFlagToday("sport::syncStep") && TimeUtil.isNowAfterOrCompareTimeStr("0600")) {
            //    addChildTask(new ChildModelTask("syncStep", () -> {
            //        int step = tmpStepCount();
            //        try {
            //            ClassLoader classLoader = ApplicationHook.classLoader;
            //            Object rpcManager = XposedHelpers.callStaticMethod(
            //                    classLoader.loadClass("com.alibaba.health.pedometer.intergation.rpc.RpcManager"),
            //                    "a"
            //            );
            //            boolean success = (Boolean) XposedHelpers.callMethod(
            //                    rpcManager,
            //                    "a",
            //                    step, Boolean.FALSE, "system"
            //            );
            //
            //            if (success) {
            //                Log.other("同步步数🏃🏻‍♂️[" + step + "步]");
            //            } else {
            //                Log.error(TAG, "同步运动步数失败:" + step);
            //            }
            //            Status.setFlagToday("sport::syncStep");
            //        } catch (Throwable t) {
            //            Log.printStackTrace(TAG, t);
            //        }
            //    }));
            //}

            // 运动任务
            if (!Status.hasFlagToday("sport::dailyTasks") && sportsTasks.getValue()) {
                // 先执行原有运动任务面板逻辑
                sportsTasks();
            }

            // 运动球任务
            if (sportsEnergyBubble.getValue()) {
                sportsEnergyBubbleTask();
            }

            ClassLoader loader = ApplicationHook.classLoader;

            if (walk.getValue()) {
                getWalkPathThemeIdOnConfig();
                walk();
            }

            if (openTreasureBox.getValue() && !walk.getValue()) {
                queryMyHomePage(loader);
            }

            if (donateCharityCoin.getValue() && Status.canDonateCharityCoin()) {
                queryProjectList(loader);
            }

            if (minExchangeCount.getValue() > 0
                    && Status.canExchangeToday(Objects.requireNonNull(Objects.requireNonNull(UserMap.INSTANCE.getCurrentUid())))) {
                queryWalkStep(loader);
            }

            if (tiyubiz.getValue()) {
                userTaskGroupQuery("SPORTS_DAILY_SIGN_GROUP");
                userTaskGroupQuery("SPORTS_DAILY_GROUP");
                userTaskRightsReceive();
                pathFeatureQuery();
                participate();
            }

            if (battleForFriends.getValue()) {
                queryClubHome();
                queryTrainItem();
                buyMember();
            }

            if (receiveCoinAsset.getValue()) {
                receiveCoinAsset();
            }

        } catch (Throwable t) {
            Log.record(TAG, "runJava error:");
            Log.printStackTrace(TAG, t);
        } finally {
            Log.record(TAG, "执行结束-" + getName());
        }
    }

    public int tmpStepCount() {
        if (tmpStepCount >= 0) {
            return tmpStepCount;
        }
        tmpStepCount = syncStepCount.getValue();
        if (tmpStepCount > 0) {
            tmpStepCount = RandomUtil.nextInt(tmpStepCount, tmpStepCount + 2000);
            if (tmpStepCount > 100000) {
                tmpStepCount = 100000;
            }
        }
        return tmpStepCount;
    }

    // 运动
    private void sportsTasks() {
        try {
            if (MyUtils.get_关闭必弹验证1()) return;
            sportsCheck_in();
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryCoinTaskPanel());

            if (ResChecker.checkRes(TAG,jo)) {
                JSONObject data = jo.getJSONObject("data");
                MyUtils.CHANGE_KT11.trim();
                JSONArray taskList = data.optJSONArray("taskList");
                if (taskList == null) taskList = new JSONArray();


                int totalTasks = 0;
                int completedTasks = 0;
                int availableTasks = 0;

                for (int i = 0; i < taskList.length(); i++) {
                    JSONObject taskDetail = taskList.getJSONObject(i);
                    String taskId = taskDetail.getString("taskId");
                    String taskName = taskDetail.getString("taskName");
                    String taskStatus = taskDetail.getString("taskStatus");
                    String taskType = taskDetail.optString("taskType", "");

                    // 排除自动结算类型任务
                    if (taskType.equals("SETTLEMENT")) {
                        continue;
                    }
                    // 检查任务是否在黑名单中
                    if (TaskBlacklist.INSTANCE.isTaskInBlacklist(taskId) || TaskBlacklist.INSTANCE.isTaskInBlacklist(taskName)) {
                        continue;
                    }
                    totalTasks++;

                    // 处理不同任务状态
                    switch (taskStatus) {
                        case "HAS_RECEIVED":
                            // Log.record(TAG, "做任务得能量🎈[任务已完成：" + taskName + "]");
                            completedTasks++;
                            break;

                        case "WAIT_RECEIVE":
                            // 需要领取奖励
                            if (receiveTaskReward(taskDetail, taskName)) {
                                completedTasks++;
                            }
                            break;

                        case "WAIT_COMPLETE":
                            // 需要完成任务
                            availableTasks++;
                            if (completeTask(taskDetail, taskName)) {
                                completedTasks++;
                            }
                            break;

                        default:
                            Log.error(TAG, "做任务得能量🎈[未知状态：" + taskName + "，状态：" + taskStatus + "]");
                            break;
                    }
                }

                Log.record(TAG, "运动任务完成情况：" + completedTasks + "/" + totalTasks + "，可执行任务：" + availableTasks);

                // 所有任务完成后标记今日完成
                if (totalTasks > 0 && completedTasks >= totalTasks && availableTasks == 0) {
                    String today = TimeUtil.getDateStr2();
                    DataStore.INSTANCE.put(SPORTS_TASKS_COMPLETED_DATE, today);
                    Status.setFlagToday("sport::dailyTasks");
                    Log.record(TAG, "✅ 所有运动任务已完成，今日不再执行");
                }
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    private boolean receiveTaskReward(JSONObject taskDetail, String taskName) {
        try {
            String assetId = taskDetail.getString("assetId");
            String prizeAmount = String.valueOf(taskDetail.getInt("prizeAmount"));

            String result = AntSportsRpcCall.pickBubbleTaskEnergy(assetId);
            JSONObject resultData = new JSONObject(result);

            if (ResChecker.checkRes(TAG,result)) {
                //String changeAmount = resultData.optString("changeAmount", "0");
                Log.other("做任务得能量🎈["+taskName+"] +"+prizeAmount + "能量");
                return true;
            } else {
                String errorMsg = resultData.optString("errorMsg", "未知错误");
                String errorCode = resultData.optString("errorCode", "");
                Log.error(TAG, "做任务得能量🎈[领取失败：" + taskName + "，错误：" + errorCode + " - " + errorMsg + "]");
                // 不可重试的错误视为完成
                if (!resultData.optBoolean("retryable", true) || "CAMP_TRIGGER_ERROR".equals(errorCode)) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.error(TAG, "做任务得能量🎈[领取异常：" + taskName + "，错误：" + e.getMessage() + "]");
        }
        return false;
    }

    private boolean completeTask(JSONObject taskDetail, String taskName) {
        try {
            String taskId = taskDetail.getString("taskId");
            String prizeAmount = taskDetail.getString("prizeAmount");
            int currentNum = taskDetail.getInt("currentNum");
            int limitConfigNum = taskDetail.getInt("limitConfigNum");
            int remainingNum = limitConfigNum - currentNum;
            boolean needSignUp = taskDetail.optBoolean("needSignUp", false);

            if (remainingNum <= 0) {
                //Log.record(TAG, "做任务得能量🎈[任务无需执行：" + taskName + "，已完成" + currentNum + "/" + limitConfigNum + "]");
                return true;
            }
            // 如果需要签到,先执行签到
            if (needSignUp) {
                if (!signUpForTask(taskId, taskName)) {
                    return false;
                }
                sleepCompat(2000);
            }

            //Log.record(TAG, "做任务得能量🎈[开始执行：" + taskName + "，需完成" + remainingNum + "次]");

            // 执行任务
            for (int i = 0; i < remainingNum; i++) {
                JSONObject result = new JSONObject(AntSportsRpcCall.completeExerciseTasks(taskId));

                if (ResChecker.checkRes(TAG,result)) {
                    Log.record(TAG, "做任务得能量🎈[完成任务：" + taskName + "，得" + prizeAmount + "💰]#(" + (i + 1) + "/" + remainingNum + ")");

                    // 完成任务后尝试领取奖励
                    if (i == remainingNum - 1) {
                        sleepCompat(2000);
                        receiveCoinAsset();
                    }
                } else {
                    String errorMsg = result.optString("errorMsg", "未知错误");
                    Log.error(TAG, "做任务得能量🎈[任务失败：" + taskName + "，错误：" + errorMsg + "]#(" + (i + 1) + "/" + remainingNum + ")");
                    String errorCode = result.optString("errorCode", "");
                    // 自动将失败的任务加入黑名单
                    if (!errorCode.isEmpty()) {
                        TaskBlacklist.INSTANCE.autoAddToBlacklist(taskId, taskName, errorCode);
                    }
                    break;
                }

                if (remainingNum > 1 && i < remainingNum - 1) {
                    sleepCompat(10000);
                }
            }

            return true;
        } catch (Exception e) {
            Log.error(TAG, "做任务得能量🎈[执行异常：" + taskName + "，错误：" + e.getMessage() + "]");
            return false;
        }
    }

    private boolean signUpForTask(String taskId, String taskName) {
        try {
            String result = AntSportsRpcCall.signUpTask(taskId);
            JSONObject resultData = new JSONObject(result);

            if (ResChecker.checkRes(TAG,resultData)) {
                JSONObject data = resultData.optJSONObject("data");
                String taskOrderId = data != null ? data.optString("taskOrderId", "") : "";
                Log.other("做任务得能量🎈[签到成功：" + taskName + "，订单：" + taskOrderId + "]");
                return true;
            } else {
                String errorMsg = resultData.optString("errorMsg", "未知错误");
                Log.error(TAG, "做任务得能量🎈[签到失败：" + taskName + "，错误：" + errorMsg + "]");
            }
        } catch (Exception e) {
            Log.error(TAG, "做任务得能量🎈[签到异常：" + taskName + "，错误：" + e.getMessage() + "]");
        }
        return false;
    }

    /**
     * 运动首页推荐能量球任务
     * 根据看我.txt：queryEnergyBubbleModule → data.recBubbleList
     * 只处理包含 channel 字段的条目，每个任务顺序执行一次，中间随机休息 1-3 秒
     */
    private void sportsEnergyBubbleTask() {
        try {
            if (MyUtils.get_关闭必弹验证2()) return;
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryEnergyBubbleModule());
            if (!ResChecker.checkRes(TAG,jo)) {
                Log.error(TAG, "queryEnergyBubbleModule fail: " + jo);
                return;
            }

            JSONObject data = jo.optJSONObject("data");
            if (data == null || !data.has("recBubbleList")) {
                return;
            }

            JSONArray recBubbleList = data.optJSONArray("recBubbleList");
            if (recBubbleList == null || recBubbleList.length() == 0) {
                return;
            }
            boolean hasCompletedTask = false; // 标志位：是否至少完成了一个任务
            for (int i = 0; i < recBubbleList.length(); i++) {
                JSONObject bubble = recBubbleList.optJSONObject(i);
                if (bubble == null) {
                    continue;
                }
                // 只处理有 channel 字段的记录（广告任务），引导/订阅等不处理
                String id=bubble.optString("id");
                String taskId = bubble.optString("channel", "");
                if (taskId.isEmpty()) {
                    continue;
                }
                if(TaskBlacklist.INSTANCE.isTaskInBlacklist(id)) continue;
                String sourceName = bubble.optString("simpleSourceName", "");
                int coinAmount = bubble.optInt("coinAmount", 0);
                Log.record(TAG, "运动首页任务[开始完成：" + sourceName + "，taskId=" + taskId + "，coin=" + coinAmount + "]");

                JSONObject completeRes = new JSONObject(AntSportsRpcCall.completeExerciseTasks(taskId));
                if (ResChecker.checkRes(TAG,completeRes)) {
                    hasCompletedTask = true; // 成功完成至少一个任务
                    JSONObject dataObj = completeRes.optJSONObject("data");
                    int assetCoinAmount = 0;
                    String taskFinishToast = "";
                    if (dataObj != null) {
                        assetCoinAmount = dataObj.optInt("assetCoinAmount", 0);
                        taskFinishToast = dataObj.optString("taskFinishToast", "");
                    }
                    Log.other("运动球任务✅[" + sourceName + "]#奖励" + assetCoinAmount + "💰 " );//+ taskFinishToast
                } else {
                    String errorCode = completeRes.optString("errorCode", "");
                    String errorMsg = completeRes.optString("errorMsg", "");
                    Log.error(TAG, "运动球任务❌[" + sourceName + "]#" + completeRes+" 任务："+ bubble);

                    if(!id.isEmpty()) {
                        TaskBlacklist.INSTANCE.addToBlacklist(id,sourceName);
                    }
                }
                // 每处理一个任务随机休息 1-3 秒
                int sleepMs = RandomUtil.nextInt(10000, 30000);
                sleepCompat(sleepMs);
            }
            if (hasCompletedTask) {  // 先判断是否有完成任务
                String result = AntSportsRpcCall.pickBubbleTaskEnergy();
                JSONObject resultJson = new JSONObject(result);
                if (ResChecker.checkRes(TAG,resultJson)) {
                    JSONObject dataObj = resultJson.optJSONObject("data");
                    if (dataObj != null) {
                        String balance = dataObj.optString("balance", "0");
                        Log.other("拾取能量球成功  当前余额: " + balance + "💰");
                    }
                } else {
                    Log.error(TAG, "领取能量球任务失败: " + resultJson.optString("errorMsg", "未知错误"));
                }
            } else {
                Log.record(TAG, "未完成任何任务，跳过领取能量球");
            }

        } catch (Throwable t) {
            Log.printStackTrace(TAG, "sportsEnergyBubbleTask err:",t);
        }
    }

    private void sportsCheck_in() {
        try {
            // 第一步：先执行查询操作，获取签到状态
            JSONObject queryJo = new JSONObject(AntSportsRpcCall.signInCoinTask("query"));
            if (ResChecker.checkRes(TAG, queryJo)) {
                JSONObject data = queryJo.getJSONObject("data");
                boolean isSigned = data.getBoolean("signed");

                // 如果整体未签到，遍历签到配置列表查找今日签到项
                if (!isSigned) {
                    MyUtils.CHANGE_KT12.trim();
                    JSONArray signConfigList = data.optJSONArray("signConfigList");
                    if (signConfigList == null) signConfigList = new JSONArray();
                    // 遍历所有签到配置项
                    for (int i = 0; i < signConfigList.length(); i++) {
                        JSONObject configItem = signConfigList.getJSONObject(i);
                        boolean toDay = configItem.getBoolean("toDay");
                        boolean itemSigned = configItem.getBoolean("signed");

                        // 找到今日未签到的项
                        if (toDay && !itemSigned) {
                            int coinAmount = configItem.getInt("coinAmount");
                            // 执行签到操作
                            JSONObject signJo = new JSONObject(AntSportsRpcCall.signInCoinTask("signIn"));

                            if (ResChecker.checkRes(TAG, signJo)) {
                                JSONObject signData = signJo.getJSONObject("data");
                                JSONObject subscribeConfig = signData.has("subscribeConfig")
                                        ? signData.getJSONObject("subscribeConfig")
                                        : new JSONObject();

                                String expireDays = subscribeConfig.has("subscribeExpireDays")
                                        ? subscribeConfig.getString("subscribeExpireDays")
                                        : "未知";
                                String toast = signData.has("toast") ? signData.getString("toast") : "";

                                // 输出签到成功信息，包含获得的能量数
                                Log.other("做任务得能量🎈[签到" + expireDays + "天|"
                                        + coinAmount + "能量，" + toast + "💰]");
                            } else {
                                Log.record(TAG, "签到接口调用失败：" + signJo);
                            }
                            // 找到今日项后退出循环，避免重复处理
                            break;
                        }
                    }
                } else {
                    Log.record(TAG, "运动签到今日已签到");
                }
            } else {
                Log.record(TAG, "查询签到状态失败：" + queryJo);
            }
        } catch (Exception e) {
            Log.printStackTrace(TAG, "sportsCheck_in err", e);
        }
    }

    private void receiveCoinAsset() {
        try {
            String s = AntSportsRpcCall.queryCoinBubbleModule();
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG,jo)) {
                JSONObject data = jo.getJSONObject("data");
                if (!data.has("receiveCoinBubbleList"))
                    return;
                JSONArray ja = data.getJSONArray("receiveCoinBubbleList");
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    String assetId = jo.getString("assetId");
                    int coinAmount = jo.getInt("coinAmount");
                    jo = new JSONObject(AntSportsRpcCall.receiveCoinAsset(assetId, coinAmount));
                    if (ResChecker.checkRes(TAG,jo)) {
                        Log.other("收集金币💰[" + coinAmount + "个]");
                    } else {
                        Log.record(TAG, "首页收集金币" + " " + jo);
                    }
                }
            } else {
                Log.record(TAG, s);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "receiveCoinAsset err:",t);
        }
    }

    /*
     * 新版行走路线 -- begin
     */
    private void walk() {
        try {
            JSONObject user = new JSONObject(AntSportsRpcCall.queryUser());
            if (!ResChecker.checkRes(TAG,user)) {
                Log.error(TAG,"查询用户失败: " + user);
                return;
            }
            MyUtils.CHANGE_KT13.trim();
            String joinedPathId = user.getJSONObject("data").optString("joinedPathId", "");
            JSONObject path = queryPath(joinedPathId);
            JSONObject userPathStep = path.getJSONObject("userPathStep");
            if ("COMPLETED".equals(userPathStep.getString("pathCompleteStatus"))) {
                Log.record(TAG, "行走路线🚶🏻‍♂️路线[" + userPathStep.getString("pathName") + "]已完成");
                String pathId = queryJoinPath(walkPathThemeId);
                joinPath(pathId);
                return;
            }
            int minGoStepCount = path.getJSONObject("path").getInt("minGoStepCount");   //最小走路步数
            int pathStepCount = path.getJSONObject("path").getInt("pathStepCount");     //路径所需步数
            int forwardStepCount = userPathStep.getInt("forwardStepCount");
            int remainStepCount = userPathStep.getInt("remainStepCount");
            int needStepCount = pathStepCount - forwardStepCount;
            if (remainStepCount >= minGoStepCount) {
                int useStepCount = Math.min(remainStepCount, needStepCount);
                walkGo(userPathStep.getString("pathId"), useStepCount, userPathStep.getString("pathName"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "walk err:",t);
        }
    }

    private void walkGo(String pathId, int useStepCount, String pathName) {
        try {
            Date date = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            JSONObject jo = new JSONObject(AntSportsRpcCall.walkGo( sdf.format(date), pathId, useStepCount));
            if (ResChecker.checkRes(TAG, jo)) {
                Log.other(TAG, "行走路线🚶🏻‍♂️路线[" + pathName + "]#前进了" + useStepCount + "步");
                queryPath(pathId);
            }else {
                Log.error(TAG,"walkGo失败： [pathId: " + pathId + "]: " +jo);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "walkGo err:",t);
        }
    }

    private JSONObject queryWorldMap(String themeId) {
        JSONObject theme = null;
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryWorldMap(themeId));
            if (ResChecker.checkRes(TAG+"queryWorldMap失败： [ThemeID: " + themeId + "]: " , jo)) {
                theme = jo.getJSONObject("data");
            }else {
                Log.error(TAG,"queryWorldMap失败： [ThemeID: " + themeId + "]: " +jo);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "queryWorldMap err:",t);
        }
        return theme;
    }


    /**
     * @brief 查询指定城市的路线详情
     * @param cityId 城市唯一标识符 (通过 queryWorldMap 获取)
     * @return JSONObject 包含城市路径信息的对象，失败返回 null
     */
    private JSONObject queryCityPath(String cityId) {
        JSONObject city = null;
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryCityPath(cityId));
            if (ResChecker.checkRes(TAG, jo)) {
                city = jo.getJSONObject("data");
            }else {
                Log.error(TAG,"queryCityPath失败： [CityID: " + cityId + "]"+jo);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "queryCityPath err:",t);
        }
        return city;
    }

    private JSONObject queryPath(String pathId) {
        JSONObject path = null;
        try {
            Date date = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryPath(sdf.format(date), pathId));
            if (ResChecker.checkRes(TAG, jo)) {
                path = jo.getJSONObject("data");
                MyUtils.CHANGE_KT10.trim();
                JSONArray ja = jo.getJSONObject("data").optJSONArray("treasureBoxList");
                if (ja == null) ja = new JSONArray();
                //如果有宝箱则收取宝箱
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject treasureBox = ja.getJSONObject(i);
                    receiveEvent(treasureBox.getString("boxNo"));
                }
            }else {
                Log.error(TAG,"queryPath失败： " +jo);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "queryPath err:",t);
        }
        return path;
    }

    private void receiveEvent(String eventBillNo) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.receiveEvent(eventBillNo));
            if (!ResChecker.checkRes(TAG, jo)) {
                return;
            }
            JSONArray ja = jo.getJSONObject("data").getJSONArray("rewards");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                Log.record(TAG, "行走路线🎁开启宝箱[" + jo.getString("rewardName") + "]*" + jo.getInt("count"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "receiveEvent err:",t);
        }
    }

    //根据主题返回可以行走路径
    private String queryJoinPath(String themeId) {
        if (walkCustomPath.getValue()) {
            return walkCustomPathId.getValue();
        }
        String pathId = null;
        try {
            JSONObject theme = queryWorldMap(walkPathThemeId);
            if (theme==null) {
                Log.error(TAG,"queryJoinPath-> theme 失败："+theme);
                return null;
            }
            JSONArray cityList = theme.getJSONArray("cityList");
            for (int i = 0; i < cityList.length(); i++) {
                String cityId = cityList.getJSONObject(i).getString("cityId");
                JSONObject city = queryCityPath(cityId);
                if (city == null) {
                    continue;
                }
                JSONArray cityPathList = city.getJSONArray("cityPathList");
                for (int j = 0; j < cityPathList.length(); j++) {
                    JSONObject cityPath = cityPathList.getJSONObject(j);
                    pathId = cityPath.getString("pathId");
                    if (!"COMPLETED".equals(cityPath.getString("pathCompleteStatus"))) {
                        return pathId;
                    }
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "queryJoinPath err:",t);
        }
        return pathId;
    }

    private void joinPath(String pathId) {
        if (pathId == null) {
            // 龙年祈福线
            pathId = "p0002023122214520001";
        }
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.joinPath(pathId));
            if (ResChecker.checkRes(TAG, jo)) {
                JSONObject path = queryPath(pathId);
                Log.record(TAG, "行走路线🚶🏻‍♂️路线[" + path.getJSONObject("path").getString("name") + "]已加入");
            } else {
                Log.error(TAG, "行走路线🚶🏻‍♂️路线[" + pathId + "]有误，无法加入！");
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG,"joinPath err:", t);
        }
    }

    /**
     * &#064;brief  根据配置索引同步更新路线主题 ID
     * * @details 从 ChoiceModelField 获取用户选择的索引值，并从 @link WalkPathTheme#themeIds @endlink
     * 映射表中取得对应的接口字符串。
     * * @return void
     */
    private void getWalkPathThemeIdOnConfig() {
        int index = walkPathTheme.getValue();

        // 检查索引是否在有效范围内 [0, themeIds.length - 1]
        if (index >= 0 && index < WalkPathTheme.themeIds.length) {
            this.walkPathThemeId = WalkPathTheme.themeIds[index];
        } else {
            // 索引异常处理：记录日志并回退到默认值（大美中国）
            Log.error(TAG, "非法的路线主题索引: " + index + "，已回退至默认主题");
            this.walkPathThemeId = WalkPathTheme.themeIds[WalkPathTheme.DA_MEI_ZHONG_GUO];
        }
    }

    /*
     * 新版行走路线 -- end
     */
    private void queryMyHomePage(ClassLoader loader) {
        try {
            String s = AntSportsRpcCall.queryMyHomePage();
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG,jo)) {
                s = jo.getString("pathJoinStatus");
                if ("GOING".equals(s)) {
                    if (jo.has("pathCompleteStatus")) {
                        if ("COMPLETED".equals(jo.getString("pathCompleteStatus"))) {
                            jo = new JSONObject(AntSportsRpcCall.queryBaseList());
                            if (ResChecker.checkRes(TAG,jo)) {
                                JSONArray allPathBaseInfoList = jo.getJSONArray("allPathBaseInfoList");
                                JSONArray otherAllPathBaseInfoList = jo.getJSONArray("otherAllPathBaseInfoList")
                                        .getJSONObject(0)
                                        .getJSONArray("allPathBaseInfoList");
                                join(loader, allPathBaseInfoList, otherAllPathBaseInfoList, "");
                            } else {
                                Log.record(TAG, jo.getString("resultDesc"));
                            }
                        }
                    } else {
                        String rankCacheKey = jo.getString("rankCacheKey");
                        JSONArray ja = jo.getJSONArray("treasureBoxModelList");
                        for (int i = 0; i < ja.length(); i++) {
                            parseTreasureBoxModel(loader, ja.getJSONObject(i), rankCacheKey);
                        }
                        JSONObject joPathRender = jo.getJSONObject("pathRenderModel");
                        String title = joPathRender.getString("title");
                        int minGoStepCount = joPathRender.getInt("minGoStepCount");
                        jo = jo.getJSONObject("dailyStepModel");
                        int consumeQuantity = jo.getInt("consumeQuantity");
                        int produceQuantity = jo.getInt("produceQuantity");
                        String day = jo.getString("day");
                        int canMoveStepCount = produceQuantity - consumeQuantity;
                        if (canMoveStepCount >= minGoStepCount) {
                            go(loader, day, rankCacheKey, canMoveStepCount, title);
                        }
                    }
                } else if ("NOT_JOIN".equals(s)) {
                    String firstJoinPathTitle = jo.getString("firstJoinPathTitle");
                    JSONArray allPathBaseInfoList = jo.getJSONArray("allPathBaseInfoList");
                    JSONArray otherAllPathBaseInfoList = jo.getJSONArray("otherAllPathBaseInfoList").getJSONObject(0)
                            .getJSONArray("allPathBaseInfoList");
                    join(loader, allPathBaseInfoList, otherAllPathBaseInfoList, firstJoinPathTitle);
                }
            } else {
                Log.record(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "queryMyHomePage err:",t);
        }
    }

    private void join(ClassLoader loader, JSONArray allPathBaseInfoList, JSONArray otherAllPathBaseInfoList,
                      String firstJoinPathTitle) {
        try {
            int index = -1;
            String title = null;
            String pathId = null;
            JSONObject jo;
            for (int i = allPathBaseInfoList.length() - 1; i >= 0; i--) {
                jo = allPathBaseInfoList.getJSONObject(i);
                if (jo.getBoolean("unlocked")) {
                    title = jo.getString("title");
                    pathId = jo.getString("pathId");
                    index = i;
                    break;
                }
            }
            if (index < 0 || index == allPathBaseInfoList.length() - 1) {
                for (int j = otherAllPathBaseInfoList.length() - 1; j >= 0; j--) {
                    jo = otherAllPathBaseInfoList.getJSONObject(j);
                    if (jo.getBoolean("unlocked")) {
                        if (j != otherAllPathBaseInfoList.length() - 1 || index != allPathBaseInfoList.length() - 1) {
                            title = jo.getString("title");
                            pathId = jo.getString("pathId");
                            index = j;
                        }
                        break;
                    }
                }
            }
            if (index >= 0) {
                String s;
                if (title.equals(firstJoinPathTitle)) {
                    s = AntSportsRpcCall.openAndJoinFirst();
                } else {
                    s = AntSportsRpcCall.join(pathId);
                }
                jo = new JSONObject(s);
                if (ResChecker.checkRes(TAG,jo)) {
                    Log.other("加入线路🚶🏻‍♂️[" + title + "]");
                    queryMyHomePage(loader);
                } else {
                    Log.record(TAG, jo.getString("resultDesc"));
                }
            } else {
                Log.record(TAG, "好像没有可走的线路了！");
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "join err:",t);
        }
    }

    private void go(ClassLoader loader, String day, String rankCacheKey, int stepCount, String title) {
        try {
            String s = AntSportsRpcCall.go(day, rankCacheKey, stepCount);
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG,jo)) {
                Log.other("行走线路🚶🏻‍♂️[" + title + "]#前进了" + jo.getInt("goStepCount") + "步");
                boolean completed = "COMPLETED".equals(jo.getString("completeStatus"));
                JSONArray ja = jo.getJSONArray("allTreasureBoxModelList");
                for (int i = 0; i < ja.length(); i++) {
                    parseTreasureBoxModel(loader, ja.getJSONObject(i), rankCacheKey);
                }
                if (completed) {
                    Log.other("完成线路🚶🏻‍♂️[" + title + "]");
                    queryMyHomePage(loader);
                }
            } else {
                Log.record(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "go err:",t);
        }
    }

    private void parseTreasureBoxModel(ClassLoader loader, JSONObject jo, String rankCacheKey) {
        try {
            String canOpenTime = jo.getString("canOpenTime");
            String issueTime = jo.getString("issueTime");
            String boxNo = jo.getString("boxNo");
            String userId = jo.getString("userId");
            if (canOpenTime.equals(issueTime)) {
                openTreasureBox(boxNo, userId);
            } else {
                long cot = Long.parseLong(canOpenTime);
                long now = Long.parseLong(rankCacheKey);
                long delay = cot - now;
                if (delay <= 0) {
                    openTreasureBox(boxNo, userId);
                    return;
                }
                if (delay < BaseModel.Companion.getCheckInterval().getValue()) {
                    String taskId = "BX|" + boxNo;
                    if (hasChildTask(taskId)) {
                        return;
                    }
                    Log.record(TAG, "还有 " + delay + "ms 开运动宝箱");
                    addChildTask(new ChildModelTask(taskId, "BX", () -> {
                        Log.record(TAG, "蹲点开箱开始");
                        long startTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - startTime < 5_000) {
                            if (openTreasureBox(boxNo, userId) > 0) {
                                break;
                            }
                            sleepCompat(200);
                        }
                    }, System.currentTimeMillis() + delay));
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "parseTreasureBoxModel err:",t);
        }
    }

    private int openTreasureBox(String boxNo, String userId) {
        try {
            String s = AntSportsRpcCall.openTreasureBox(boxNo, userId);
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG,jo)) {
                JSONArray ja = jo.getJSONArray("treasureBoxAwards");
                int num = 0;
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    num += jo.getInt("num");
                    Log.other("运动宝箱🎁[" + num + jo.getString("name") + "]");
                }
                return num;
            } else if ("TREASUREBOX_NOT_EXIST".equals(jo.getString("resultCode"))) {
                Log.record(jo.getString("resultDesc"));
                return 1;
            } else {
                Log.record(jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "openTreasureBox err:",t);
        }
        return 0;
    }

    private void queryProjectList(ClassLoader loader) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.queryProjectList(0));
            if (ResChecker.checkRes(TAG,jo)) {
                int charityCoinCount = jo.getInt("charityCoinCount");
                if (charityCoinCount < donateCharityCoinAmount.getValue()) {
                    return;
                }
                JSONArray ja = jo.getJSONObject("projectPage").getJSONArray("data");
                for (int i = 0; i < ja.length() && charityCoinCount >= donateCharityCoinAmount.getValue(); i++) {
                    jo = ja.getJSONObject(i).getJSONObject("basicModel");
                    if ("DONATE_COMPLETED".equals(jo.getString("footballFieldStatus"))) {
                        break;
                    }
                    donate(loader, donateCharityCoinAmount.getValue(), jo.getString("projectId"), jo.getString("title"));
                    Status.donateCharityCoin();
                    charityCoinCount -= donateCharityCoinAmount.getValue();
                    if (donateCharityCoinType.getValue() == DonateCharityCoinType.ONE) {
                        break;
                    }
                }
            } else {
                Log.record(TAG);
                Log.record(jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "queryProjectList err:",t);
        }
    }

    private void donate(ClassLoader loader, int donateCharityCoin, String projectId, String title) {
        try {
            String s = AntSportsRpcCall.donate(donateCharityCoin, projectId);
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG,jo)) {
                Log.other("捐赠活动❤️[" + title + "][" + donateCharityCoin + "能量🎈]");
            } else {
                Log.record(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG,"donate err:", t);
        }
    }

    private void queryWalkStep(ClassLoader loader) {
        try {
            String s = AntSportsRpcCall.queryWalkStep();
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG,jo)) {
                jo = jo.getJSONObject("dailyStepModel");
                int produceQuantity = jo.getInt("produceQuantity");
                int hour = Integer.parseInt(TimeUtil.getFormatTime().split(":")[0]);

                if (produceQuantity >= minExchangeCount.getValue() || hour >= latestExchangeTime.getValue()) {
                    AntSportsRpcCall.walkDonateSignInfo(produceQuantity);
                    s = AntSportsRpcCall.donateWalkHome(produceQuantity);
                    jo = new JSONObject(s);
                    if (!jo.getBoolean("isSuccess"))
                        return;
                    JSONObject walkDonateHomeModel = jo.getJSONObject("walkDonateHomeModel");
                    JSONObject walkUserInfoModel = walkDonateHomeModel.getJSONObject("walkUserInfoModel");
                    if (!walkUserInfoModel.has("exchangeFlag")) {
                        Status.exchangeToday(Objects.requireNonNull(UserMap.INSTANCE.getCurrentUid()));
                        return;
                    }
                    String donateToken = walkDonateHomeModel.getString("donateToken");
                    JSONObject walkCharityActivityModel = walkDonateHomeModel.getJSONObject("walkCharityActivityModel");
                    String activityId = walkCharityActivityModel.getString("activityId");
                    s = AntSportsRpcCall.exchange(activityId, produceQuantity, donateToken);
                    jo = new JSONObject(s);
                    if (jo.getBoolean("isSuccess")) {
                        JSONObject donateExchangeResultModel = jo.getJSONObject("donateExchangeResultModel");
                        int userCount = donateExchangeResultModel.getInt("userCount");
                        double amount = donateExchangeResultModel.getJSONObject("userAmount").getDouble("amount");
                        Log.other("捐出活动❤️[" + userCount + "步]#兑换" + amount + "元公益金");
                        Status.exchangeToday(Objects.requireNonNull(UserMap.INSTANCE.getCurrentUid()));
                    } else if (s.contains("已捐步")) {
                        Status.exchangeToday(Objects.requireNonNull(UserMap.INSTANCE.getCurrentUid()));
                    } else {
                        Log.record(TAG, jo.getString("resultDesc"));
                    }
                }
            } else {
                Log.record(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "queryWalkStep err:",t);
        }
    }

    /* 文体中心 */// SPORTS_DAILY_SIGN_GROUP SPORTS_DAILY_GROUP
    private void userTaskGroupQuery(String groupId) {
        try {
            String s = AntSportsRpcCall.userTaskGroupQuery(groupId);
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG, jo)) {
                jo = jo.getJSONObject("group");
                JSONArray userTaskList = jo.getJSONArray("userTaskList");
                for (int i = 0; i < userTaskList.length(); i++) {
                    jo = userTaskList.getJSONObject(i);
                    if (!"TODO".equals(jo.getString("status")))
                        continue;
                    JSONObject taskInfo = jo.getJSONObject("taskInfo");
                    String bizType = taskInfo.getString("bizType");
                    String taskId = taskInfo.getString("taskId");
                    jo = new JSONObject(AntSportsRpcCall.userTaskComplete(bizType, taskId));
                    if (ResChecker.checkRes(TAG, jo)) {
                        String taskName = taskInfo.optString("taskName", taskId);
                        Log.other("完成任务🧾[" + taskName + "]");
                    } else {
                        Log.record(TAG, "文体每日任务" + " " + jo);
                    }
                }
            } else {
                Log.record(TAG, "文体每日任务" + " " + s);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "userTaskGroupQuery err:",t);
        }
    }

    private void participate() {
        try {
            String s = AntSportsRpcCall.queryAccount();
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG, jo)) {
                double balance = jo.getDouble("balance");
                if (balance < 100)
                    return;
                jo = new JSONObject(AntSportsRpcCall.queryRoundList());
                if (ResChecker.checkRes(TAG, jo)) {
                    JSONArray dataList = jo.getJSONArray("dataList");
                    for (int i = 0; i < dataList.length(); i++) {
                        jo = dataList.getJSONObject(i);
                        if (!"P".equals(jo.getString("status")))
                            continue;
                        if (jo.has("userRecord"))
                            continue;
                        JSONArray instanceList = jo.getJSONArray("instanceList");
                        int pointOptions = 0;
                        String roundId = jo.getString("id");
                        String InstanceId = null;
                        String ResultId = null;
                        for (int j = instanceList.length() - 1; j >= 0; j--) {
                            jo = instanceList.getJSONObject(j);
                            if (jo.getInt("pointOptions") < pointOptions)
                                continue;
                            pointOptions = jo.getInt("pointOptions");
                            InstanceId = jo.getString("id");
                            ResultId = jo.getString("instanceResultId");
                        }
                        jo = new JSONObject(AntSportsRpcCall.participate(pointOptions, InstanceId, ResultId, roundId));
                        if (ResChecker.checkRes(TAG, jo)) {
                            jo = jo.getJSONObject("data");
                            String roundDescription = jo.getString("roundDescription");
                            int targetStepCount = jo.getInt("targetStepCount");
                            Log.other("走路挑战🚶🏻‍♂️[" + roundDescription + "]#" + targetStepCount);
                        } else {
                            Log.record(TAG, "走路挑战赛" + " " + jo);
                        }
                    }
                } else {
                    Log.record(TAG, "queryRoundList" + " " + jo);
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "participate err:",t);
        }
    }

    private void userTaskRightsReceive() {
        try {
            String s = AntSportsRpcCall.userTaskGroupQuery("SPORTS_DAILY_GROUP");
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG, jo)) {
                jo = jo.getJSONObject("group");
                JSONArray userTaskList = jo.getJSONArray("userTaskList");
                for (int i = 0; i < userTaskList.length(); i++) {
                    jo = userTaskList.getJSONObject(i);
                    if (!"COMPLETED".equals(jo.getString("status")))
                        continue;
                    String userTaskId = jo.getString("userTaskId");
                    JSONObject taskInfo = jo.getJSONObject("taskInfo");
                    String taskId = taskInfo.getString("taskId");
                    jo = new JSONObject(AntSportsRpcCall.userTaskRightsReceive(taskId, userTaskId));
                    if (ResChecker.checkRes(TAG, jo)) {
                        String taskName = taskInfo.optString("taskName", taskId);
                        JSONArray rightsRuleList = taskInfo.getJSONArray("rightsRuleList");
                        StringBuilder award = new StringBuilder();
                        for (int j = 0; j < rightsRuleList.length(); j++) {
                            jo = rightsRuleList.getJSONObject(j);
                            award.append(jo.getString("rightsName")).append("*").append(jo.getInt("baseAwardCount"));
                        }
                        Log.other("领取奖励🎖️[" + taskName + "]#" + award);
                    } else {
                        Log.record(TAG, "文体中心领取奖励");
                        Log.record(jo.toString());
                    }
                }
            } else {
                Log.record(TAG, "文体中心领取奖励");
                Log.record(s);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "userTaskRightsReceive err:",t);
        }
    }

    private void pathFeatureQuery() {
        try {
            String s = AntSportsRpcCall.pathFeatureQuery();
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG, jo)) {
                JSONObject path = jo.getJSONObject("path");
                String pathId = path.getString("pathId");
                String title = path.getString("title");
                int minGoStepCount = path.getInt("minGoStepCount");
                if (jo.has("userPath")) {
                    JSONObject userPath = jo.getJSONObject("userPath");
                    String userPathRecordStatus = userPath.getString("userPathRecordStatus");
                    if ("COMPLETED".equals(userPathRecordStatus)) {
                        pathMapHomepage(pathId);
                        pathMapJoin(title, pathId);
                    } else if ("GOING".equals(userPathRecordStatus)) {
                        pathMapHomepage(pathId);
                        String countDate = TimeUtil.getFormatDate();
                        jo = new JSONObject(AntSportsRpcCall.stepQuery(countDate, pathId));
                        if (ResChecker.checkRes(TAG, jo)) {
                            int canGoStepCount = jo.getInt("canGoStepCount");
                            if (canGoStepCount >= minGoStepCount) {
                                String userPathRecordId = userPath.getString("userPathRecordId");
                                tiyubizGo(countDate, title, canGoStepCount, pathId, userPathRecordId);
                            }
                        }
                    }
                } else {
                    pathMapJoin(title, pathId);
                }
            } else {
                Log.record(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "pathFeatureQuery err:",t);
        }
    }

    private void pathMapHomepage(String pathId) {
        try {
            String s = AntSportsRpcCall.pathMapHomepage(pathId);
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG, jo)) {
                if (!jo.has("userPathGoRewardList"))
                    return;
                JSONArray userPathGoRewardList = jo.getJSONArray("userPathGoRewardList");
                for (int i = 0; i < userPathGoRewardList.length(); i++) {
                    jo = userPathGoRewardList.getJSONObject(i);
                    if (!"UNRECEIVED".equals(jo.getString("status")))
                        continue;
                    String userPathRewardId = jo.getString("userPathRewardId");
                    jo = new JSONObject(AntSportsRpcCall.rewardReceive(pathId, userPathRewardId));
                    if (ResChecker.checkRes(TAG, jo)) {
                        jo = jo.getJSONObject("userPathRewardDetail");
                        JSONArray rightsRuleList = jo.getJSONArray("userPathRewardRightsList");
                        StringBuilder award = new StringBuilder();
                        for (int j = 0; j < rightsRuleList.length(); j++) {
                            jo = rightsRuleList.getJSONObject(j).getJSONObject("rightsContent");
                            award.append(jo.getString("name")).append("*").append(jo.getInt("count"));
                        }
                        Log.other("文体宝箱🎁[" + award + "]");
                    } else {
                        Log.record(TAG, "文体中心开宝箱");
                        Log.record(jo.toString());
                    }
                }
            } else {
                Log.record(TAG, "文体中心开宝箱");
                Log.record(s);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "pathMapHomepage err:",t);
        }
    }

    private void pathMapJoin(String title, String pathId) {
        try {
            JSONObject jo = new JSONObject(AntSportsRpcCall.pathMapJoin(pathId));
            if (ResChecker.checkRes(TAG, jo)) {
                Log.other("加入线路🚶🏻‍♂️[" + title + "]");
                pathFeatureQuery();
            } else {
                Log.record(TAG, jo.toString());
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "pathMapJoin err:",t);
        }
    }

    private void tiyubizGo(String countDate, String title, int goStepCount, String pathId,
                           String userPathRecordId) {
        try {
            String s = AntSportsRpcCall.tiyubizGo(countDate, goStepCount, pathId, userPathRecordId);
            JSONObject jo = new JSONObject(s);
            if (ResChecker.checkRes(TAG, jo)) {
                jo = jo.getJSONObject("userPath");
                Log.other("行走线路🚶🏻‍♂️[" + title + "]#前进了" + jo.getInt("userPathRecordForwardStepCount") + "步");
                pathMapHomepage(pathId);
                boolean completed = "COMPLETED".equals(jo.getString("userPathRecordStatus"));
                if (completed) {
                    Log.other("完成线路🚶🏻‍♂️[" + title + "]");
                    pathFeatureQuery();
                }
            } else {
                Log.record(TAG, s);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "tiyubizGo err:",t);
        }
    }

    /* 抢好友大战 */
    private void queryClubHome() {
        try {
            // 检查是否已达到0金币上限（实时检查）
            int maxCount = zeroCoinLimit.getValue();
            if (zeroTrainCoinCount >= maxCount) {
                String today = TimeUtil.getDateStr2();
                DataStore.INSTANCE.put(TRAIN_FRIEND_ZERO_COIN_DATE, today);
                Log.record(TAG, "✅ 训练好友获得0金币已达" + maxCount + "次上限，今日不再执行");
                return;
            }
            // 发送 RPC 请求获取 club home 数据
            JSONObject clubHomeData = new JSONObject(AntSportsRpcCall.queryClubHome());
            // 处理 mainRoom 中的 bubbleList
            processBubbleList(clubHomeData.optJSONObject("mainRoom"));
            // 处理 roomList 中的每个房间的 bubbleList
            JSONArray roomList = clubHomeData.optJSONArray("roomList");
            if (roomList != null) {
                for (int i = 0; i < roomList.length(); i++) {
                    JSONObject room = roomList.optJSONObject(i);
                    processBubbleList(room);
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "queryClubHome err:",t);
        }
    }

    // 训练好友-收金币
    private void processBubbleList(JSONObject object) {
        if (object == null || !object.has("bubbleList")) return;

        try {
            JSONArray bubbleList = object.getJSONArray("bubbleList");
            for (int j = 0; j < bubbleList.length(); j++) {
                JSONObject bubble = bubbleList.getJSONObject(j);
                String bubbleId = bubble.optString("bubbleId");

                // 1. 发起请求并获取响应
                String responseStr = AntSportsRpcCall.pickBubbleTaskEnergy(bubbleId,false);
                JSONObject responseJson = new JSONObject(responseStr);

                // 2. 使用你提供的 ResChecker 进行统一拦截
                if (!ResChecker.checkRes(TAG, responseJson)) {
                    Log.error(TAG, "收取训练好友 失败: " + responseStr);
                    continue;
                }

                int amount = 0;
                JSONObject data = responseJson.optJSONObject("data");
                if (data != null) {
                    String changeAmountStr = data.optString("changeAmount", "0");
                    amount = Integer.parseInt(changeAmountStr);
                }

                Log.other("训练好友💰️ [获得:" + amount + "金币]");

                // 4. 0 金币限制逻辑
                if (amount <= 0) {
                    zeroTrainCoinCount++;
                    int maxCount = zeroCoinLimit.getValue();
                    if (zeroTrainCoinCount >= maxCount) {
                        String today = TimeUtil.getDateStr2();
                        DataStore.INSTANCE.put(TRAIN_FRIEND_ZERO_COIN_DATE, today);
                        Log.record(TAG, "✅ 连续获得0金币已达" + maxCount + "次，今日停止执行");
                        return; // 退出整个方法
                    } else {
                        Log.record(TAG, "训练好友0金币计数: " + zeroTrainCoinCount + "/" + maxCount);
                    }
                } else {
                }

                // 5. 频率控制
                sleepCompat(1000);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "processBubbleList 异常:", t);
        }
    }

    // 训练好友-训练操作
    // 流程：
    // 1. 查询 clubHome，找到第一个可以训练的好友（trainInfo.training = false）
    // 2. 调用 alipay.antsports.club.train.queryTrainItem 拿到 bizId 和 trainItemList
    // 3. 从 trainItemList 中随便选一个（这里选 production 最大的），调用 trainMember 进行训练
    private void queryTrainItem() {
        try {
            JSONObject clubHomeData = new JSONObject(AntSportsRpcCall.queryClubHome());
            JSONArray roomList = clubHomeData.optJSONArray("roomList");
            if (roomList == null || roomList.length() == 0) {
                return;
            }

            // 找到第一个可训练的好友
            for (int i = 0; i < roomList.length(); i++) {
                JSONObject room = roomList.optJSONObject(i);
                if (room == null) continue;
                JSONArray memberList = room.optJSONArray("memberList");
                if (memberList == null || memberList.length() == 0) continue;

                for (int j = 0; j < memberList.length(); j++) {
                    JSONObject member = memberList.optJSONObject(j);
                    if (member == null) continue;

                    JSONObject trainInfo = member.optJSONObject("trainInfo");
                    // 只有当前未在训练中的好友才需要发起训练
                    if (trainInfo == null || trainInfo.optBoolean("training", false)) {
                        continue;
                    }

                    String memberId = member.optString("memberId");
                    String originBossId = member.optString("originBossId");
                    String userName = UserMap.getMaskName(originBossId);

                    // 查询训练项目列表
                    String responseData = AntSportsRpcCall.queryTrainItem();
                    JSONObject responseJson = new JSONObject(responseData);
                    if (!ResChecker.checkRes(TAG, responseJson)) {
                        Log.record(TAG, "queryTrainItem rpc failed: " + responseJson.optString("resultDesc"));
                        return;
                    }

                    // bizId 从响应顶层获取
                    String bizId = responseJson.optString("bizId", "");
                    if (bizId.isEmpty() && responseJson.has("taskDetail")) {
                        bizId = responseJson.getJSONObject("taskDetail").optString("taskId", "");
                    }

                    JSONArray trainItemList = responseJson.optJSONArray("trainItemList");
                    if (bizId.isEmpty() || trainItemList == null || trainItemList.length() == 0) {
                        Log.record(TAG, "queryTrainItem response missing bizId or trainItemList");
                        return;
                    }

                    // 这里随便选一个，这里选 production 最大的训练方式
                    JSONObject bestItem = null;
                    int bestProduction = -1;
                    for (int k = 0; k < trainItemList.length(); k++) {
                        JSONObject item = trainItemList.optJSONObject(k);
                        if (item == null) continue;
                        int production = item.optInt("production", 0);
                        if (production > bestProduction) {
                            bestProduction = production;
                            bestItem = item;
                        }
                    }

                    if (bestItem == null) {
                        return;
                    }

                    String itemType = bestItem.optString("itemType");
                    String trainItemName = bestItem.optString("name");

                    String trainMemberResponse = AntSportsRpcCall.trainMember(bizId, itemType, memberId, originBossId);
                    JSONObject trainMemberResponseJson = new JSONObject(trainMemberResponse);
                    if (!ResChecker.checkRes(TAG, trainMemberResponseJson)) {
                        Log.record(TAG, "trainMember request failed: " + trainMemberResponseJson.optString("resultDesc"));
                        return;
                    }

                    Log.other("训练好友🥋[训练:" + userName + " " + trainItemName + "]");
                    sleepCompat(1000);
                    return; // 只训练一个好友，逻辑足够
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "queryTrainItem err:",t);
        }
    }

    // 抢好友大战-抢购好友
    // 流程：
    // 1. 查询 clubHome 拿到当前余额 coinBalance 和房间列表
    // 2. 在空房间上，根据余额调用 queryMemberPriceRanking，拿到可买的好友列表
    // 3. 过滤出 originBossId 符合配置的好友，调用 queryClubMember → buyMember 完成抢购
    private void buyMember() {
        try {
            String clubHomeResponse = AntSportsRpcCall.queryClubHome();
            sleepCompat(500);
            JSONObject clubHomeJson = new JSONObject(clubHomeResponse);

            // 判断 clubAuth 字段是否为 "ENABLE"
            if (!"ENABLE".equals(clubHomeJson.optString("clubAuth"))) {
                Log.record(TAG, "抢好友大战🧑‍🤝‍🧑未授权开启");
                return;
            }

            JSONObject assetsInfo = clubHomeJson.optJSONObject("assetsInfo");
            if (assetsInfo == null) {
                return;
            }
            // 看我.txt：assetsInfo.energyBalance 是当前的能量值
            int coinBalance = assetsInfo.optInt("energyBalance", 0);
            if (coinBalance <= 0) {
                Log.record(TAG, "抢好友大战🧑‍🤝‍🧑当前能量为0，跳过抢好友");
                return;
            }

            JSONArray roomList = clubHomeJson.optJSONArray("roomList");
            if (roomList == null || roomList.length() == 0) {
                return;
            }

            for (int i = 0; i < roomList.length(); i++) {
                JSONObject room = roomList.optJSONObject(i);
                if (room == null) continue;

                JSONArray memberList = room.optJSONArray("memberList");
                // 只在空房间下手
                if (memberList != null && memberList.length() > 0) {
                    continue;
                }

                String roomId = room.optString("roomId");
                if (roomId.isEmpty()) continue;

                // 根据余额拉一批可抢好友
                String memberPriceResult = AntSportsRpcCall.queryMemberPriceRanking(coinBalance);
                sleepCompat(500);
                JSONObject memberPriceJson = new JSONObject(memberPriceResult);
                if (!memberPriceJson.optBoolean("success", true)) {
                    Log.error(TAG, "queryMemberPriceRanking err: " + memberPriceJson.optString("resultDesc"));
                    continue;
                }

                JSONArray memberDetailList = memberPriceJson.optJSONArray("memberDetailList");
                if (memberDetailList == null || memberDetailList.length() == 0) {
                    Log.record(TAG, "抢好友大战🧑‍🤝‍🧑暂无可抢好友");
                    continue;
                }

                // 遍历候选好友
                for (int j = 0; j < memberDetailList.length(); j++) {
                    JSONObject detail = memberDetailList.optJSONObject(j);
                    if (detail == null) continue;

                    JSONObject memberModel = detail.optJSONObject("memberModel");
                    if (memberModel == null) continue;

                    String originBossId = memberModel.optString("originBossId");
                    String memberIdFromRank = memberModel.optString("memberId");
                    if (originBossId.isEmpty() || memberIdFromRank.isEmpty()) continue;

                    // 检查 originBossId 是否在配置的列表中
                    boolean isBattleForFriend = originBossIdList.getValue().contains(originBossId);
                    if (battleForFriendType.getValue() == BattleForFriendType.DONT_ROB) {
                        isBattleForFriend = !isBattleForFriend;
                    }
                    if (!isBattleForFriend) {
                        continue;
                    }

                    // 价格判断：price <= coinBalance 才抢
                    JSONObject priceInfoObj = memberModel.optJSONObject("priceInfo");
                    if (priceInfoObj == null) continue;
                    int price = priceInfoObj.optInt("price", Integer.MAX_VALUE);
                    if (price > coinBalance) {
                        continue;
                    }

                    // 查询玩家详情，拿到 currentBossId / memberId / priceInfo
                    String clubMemberResult = AntSportsRpcCall.queryClubMember(memberIdFromRank, originBossId);
                    sleepCompat(500);
                    JSONObject clubMemberDetailJson = new JSONObject(clubMemberResult);
                    if (!clubMemberDetailJson.optBoolean("success", true) || !clubMemberDetailJson.has("member")) {
                        continue;
                    }

                    JSONObject memberObj = clubMemberDetailJson.getJSONObject("member");
                    String currentBossId = memberObj.optString("currentBossId");
                    String memberId = memberObj.optString("memberId");
                    JSONObject priceInfoFull = memberObj.optJSONObject("priceInfo");
                    if (currentBossId.isEmpty() || memberId.isEmpty() || priceInfoFull == null) {
                        continue;
                    }

                    String priceInfoStr = priceInfoFull.toString();

                    String buyMemberResult = AntSportsRpcCall.buyMember(currentBossId, memberId, originBossId, priceInfoStr, roomId);
                    sleepCompat(500);
                    JSONObject buyMemberResponse = new JSONObject(buyMemberResult);

                    if (ResChecker.checkRes(TAG, buyMemberResponse)) {
                        String userName = UserMap.getMaskName(originBossId);
                        Log.other("抢购好友🥋[成功:将 " + userName + " 抢回来]");
                        // 抢好友成功后，如果训练好友功能开启，则执行训练
                        if (trainFriend.getValue()) {
                            queryTrainItem();
                        }
                        return; // 抢到一个就够了
                    } else if ("CLUB_AMOUNT_NOT_ENOUGH".equals(buyMemberResponse.optString("resultCode"))) {
                        Log.record(TAG, "[能量🎈不足，无法完成抢购好友！]");
                        return;
                    } else if ("CLUB_MEMBER_TRADE_PROTECT".equals(buyMemberResponse.optString("resultCode"))) {
                        Log.record(TAG, "[暂时无法抢购好友，给Ta一段独处的时间吧！]");
                    }
                }
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "buyMember err:",t);
        }
    }

    /**
     * 健康岛任务处理器
     * 整体流程（与 coinExchangeItem 风格保持一致）：
     * 1. 签到（querySign + takeSign）
     * 2. 任务大厅循环处理（queryTaskCenter + taskSend / adtask.finish）→ 新增循环重试+失败限制
     * 3. 捡泡泡（queryBubbleTask + pickBubbleTaskEnergy）
     * 优化点：
     * ✔ 任务完成后自动重新获取任务列表，直到无待完成任务
     * ✔ 失败次数限制（优先取 BaseModel.getSetMaxErrorCount()，默认5次）
     * ✔ 每次循环间隔短延时（避免接口QPS过高）
     * ✔ 保留原有所有校验逻辑和日志风格
     */
    @SuppressWarnings("GrazieInspection")
    public class NeverlandTaskHandler {

        private static final String TAG = "Neverland";
        // 失败次数限制（优先从 BaseModel 获取，无则默认5次）
        private static final int MAX_ERROR_COUNT = BaseModel.Companion.getSetMaxErrorCount().getValue() > 0
                ? BaseModel.Companion.getSetMaxErrorCount().getValue()
                : 5;
        // 循环间隔延时（ms）- 避免接口调用过频繁
        private static final long TASK_LOOP_DELAY = 1000;

        /** 健康岛任务入口 */
        public void runNeverland() {
            try {
                Log.record(TAG, "开始执行健康岛任务");
                if(neverlandTask.getValue())
                {
                    // 固定顺序：1.签到 → 2.循环处理任务大厅 → 3.捡泡泡
                    neverlandDoSign();                 // 签到
                    //if(!Status.hasFlagToday(StatusFlags.FLAG_ANTSPORTS_TASKCENTER_DONE))
                    {
                        loopHandleTaskCenter();            // 循环处理任务
                    }//else Log.record(TAG, "今日已执行完成 健康岛大厅任务，跳过执行");


                    handleHealthIslandTask();            // 循环处理任务中心的浏览任务
                    neverlandPickAllBubble();          // 拾取能量球
                }

                if(neverlandTask.getValue())
                {
                    neverlandAutoTask();               //执行健康岛建造
                }


                Log.record(TAG, "健康岛任务结束");
            } catch (Throwable t) {
                Log.printStackTrace(TAG, "runNeverland err:",t);
            }
        }

        // -------------------------------------------------------------------------
        // 1. 健康岛签到（无变更）
        // -------------------------------------------------------------------------

        private void neverlandDoSign() {
            try {
                if (Status.hasFlagToday("AntSports::neverlandDoSign::已签到")) return;

                Log.record(TAG, "健康岛 · 检查签到状态");
                JSONObject jo = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.querySign(3, "jkdsportcard"));

                if (!ResChecker.checkRes(TAG + "查询签到失败:", jo)
                        || !ResChecker.checkRes(TAG, jo)
                        || jo.optJSONObject("data") == null) {

                    String errorCode = jo.optString("errorCode", "");
                    if ("ALREADY_SIGN_IN".equals(errorCode) || "已签到".equals(jo.optString("errorMsg", ""))) {
                        Status.setFlagToday("AntSports::neverlandDoSign::已签到");
                    } else if ("已经签到".equals(jo.optString("errorMsg", ""))) {
                      MyUtils.CHANGE_KT19.trim();
                      //{"ariverRpcTraceId":"21bb716817689077425904737ead5a","errorCode":"ALREADY_SIGN_IN","errorMsg":"已经签到","retryable":false,"success":false}
                      Status.setFlagToday("AntSports::neverlandDoSign::已签到");
                    }
                    return;
                }

                JSONObject data = jo.getJSONObject("data");
                JSONObject signInfo = data.optJSONObject("continuousSignInfo");

                if (signInfo != null && signInfo.optBoolean("signedToday", false)) {
                    Log.record(TAG, "今日已签到 ✔ 连续：" + signInfo.optInt("continuitySignedDayCount") + " 天");
                    return;
                }

                Log.record(TAG, "健康岛 · 正在签到…");
                JSONObject signRes = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.takeSign(3, "jkdsportcard"));

                if (!ResChecker.checkRes(TAG + "签到失败:", signRes)
                        || !ResChecker.checkRes(TAG, signRes)
                        || signRes.optJSONObject("data") == null) {
                    Log.error(TAG, "takeSign raw=" + signRes);
                    Status.setFlagToday("AntSports::neverlandDoSign::已签到");
                    return;
                }

                JSONObject signData = signRes.getJSONObject("data");
                JSONObject reward = signData.optJSONObject("continuousDoSignInVO");
                int rewardAmount = reward != null ? reward.optInt("rewardAmount", 0) : 0;
                String rewardType = reward != null ? reward.optString("rewardType", "") : "";
                JSONObject signInfoAfter = signData.optJSONObject("continuousSignInfo");
                int newContinuity = signInfoAfter != null ? signInfoAfter.optInt("continuitySignedDayCount", -1) : -1;
                Log.other("健康岛签到成功 🎉 +" + rewardAmount + rewardType
                        + " 连续：" + newContinuity + " 天");
                Status.setFlagToday("AntSports::neverlandDoSign::已签到");

            } catch (Throwable t) {
                Log.printStackTrace(TAG,"neverlandDoSign err:"+ t, t);
            }
        }

        /**
         * 循环处理任务大厅：完成一批任务后重新获取列表，直到无待完成任务或达到失败次数限制
         * 只处理 PROMOKERNEL_TASK 和 LIGHT_TASK
         */
        private void loopHandleTaskCenter() {
            int errorCount = 0;
            Log.record(TAG, "开始循环处理任务大厅（失败限制：" + MAX_ERROR_COUNT + "次）");

            while (true) {
                try {
                    if (errorCount >= MAX_ERROR_COUNT) {
                        Log.error(TAG, "任务处理失败次数达到上限，停止循环");
                        Status.setFlagToday(StatusFlags.FLAG_ANTSPORTS_TASKCENTER_DONE);
                        break;
                    }

                    // 1. 获取任务列表
                    JSONObject taskCenterResp = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.queryTaskCenter());
                    if (!ResChecker.checkRes(TAG, taskCenterResp) || taskCenterResp.optJSONObject("data") == null) {
                        errorCount++;
                        sleepCompat(TASK_LOOP_DELAY);
                        continue;
                    }

                    JSONArray taskList = taskCenterResp.getJSONObject("data").optJSONArray("taskCenterTaskVOS");
                    if (taskList == null || taskList.length() == 0) {
                        Log.other("任务中心为空，无任务可处理");
                        break;
                    }
// 2. 筛选逻辑修改
                    List<JSONObject> pendingTasks = new ArrayList<>();
                    for (int i = 0; i < taskList.length(); i++) {
                        JSONObject task = taskList.optJSONObject(i);
                        if (task == null) continue;

                        String title = task.optString("title", task.optString("taskName", "未知任务"));
                        String type = task.optString("taskType", "");
                        String status = task.optString("taskStatus", "");
                        String taskId = task.optString("id", task.optString("taskId", ""));

                        // --- 核心修改：拦截并拉黑 NOT_SIGNUP ---
                        if ("NOT_SIGNUP".equals(status)) {
                            Log.record(TAG, "任务 [" + title + "] 需要手动报名，已自动拉黑并跳过");
                            if (!taskId.isEmpty()) {
                                TaskBlacklist.INSTANCE.addToBlacklist(taskId, title);
                            }
                            continue; // 直接跳过，不加入待处理列表
                        }

                        // 检查是否已经在黑名单中
                        if (TaskBlacklist.INSTANCE.isTaskInBlacklist(taskId)) {
                            continue;
                        }
                        // ------------------------------------

                        // 原有的类型过滤逻辑
                        if (("PROMOKERNEL_TASK".equals(type) || "LIGHT_TASK".equals(type))
                                && !"FINISHED".equals(status)) {
                            pendingTasks.add(task);
                        }
                    }
                    if (pendingTasks.isEmpty()) {
                        Log.record(TAG, "没有可处理或领取的任务，退出循环");
                        break;
                    }

                    Log.record(TAG, "本次发现 " + pendingTasks.size() + " 个可处理任务（含待领取）");

                    // 3. 遍历处理
                    int currentBatchError = 0;
                    for (JSONObject task : pendingTasks) {
                        boolean handleSuccess = handleSingleTask(task);
                        if (!handleSuccess) {
                            currentBatchError++;
                        }
                        sleepCompat(3000); // 适当减小间隔，提高效率
                    }

                    errorCount += currentBatchError;

                    Log.record(TAG, "当前批次执行完毕，准备下一次刷新检查");
                    sleepCompat(TASK_LOOP_DELAY);
                } catch (Throwable t) {
                    errorCount++;
                    Log.printStackTrace(TAG, "循环异常", t);
                }
            }
        }

        private boolean handleSingleTask(JSONObject task) {
            try {
                String title = task.optString("title", "未知任务");
                String type = task.optString("taskType", "");
                String status = task.optString("taskStatus", "");
                String jumpLink = task.optString("jumpLink", "");

                Log.record(TAG, "任务：[" + title + "] 状态：" + status + " 类型：" + type);

                // --- 核心修复：优先判断是否需要领取奖励 ---
                if ("TO_RECEIVE".equals(status)) {
                    Log.record(TAG, "检测到任务已完成，开始领取奖励...");

                    try {
                        // --- 1. 注入必要参数 ---
                        task.put("scene", "MED_TASK_HALL");
                        if (!task.has("source")) {
                            task.put("source", "jkdsportcard");
                        }

                        // --- 2. 发起 RPC 请求 ---
                        String res = AntSportsRpcCall.NeverlandRpcCall.taskReceive(task);
                        JSONObject resObj = new JSONObject(res);

                        // --- 3. 解析响应结果 ---
                        if (resObj.optBoolean("success", false)) {
                            // 提取数据层
                            JSONObject data = resObj.optJSONObject("data");
                            String rewardDetail = "";

                            if (data != null && data.has("userItems")) {
                                org.json.JSONArray items = data.getJSONArray("userItems");
                                StringBuilder sb = new StringBuilder();

                                for (int i = 0; i < items.length(); i++) {
                                    JSONObject item = items.getJSONObject(i);
                                    String name = item.optString("name", "未知奖励");
                                    // modifyCount 是本次领取的数量 (例如 15)
                                    int amount = item.optInt("modifyCount", 0);
                                    // count 是领取后的总余额 (例如 37131)
                                    int total = item.optInt("count", 0);

                                    sb.append("[").append(name).append(" +").append(amount).append(" (余:").append(total).append(")] ");
                                }
                                rewardDetail = sb.toString();
                            }

                            Log.record(TAG, "完成["+title+"]✔" + rewardDetail);
                            return true;
                        } else {
                            // 错误处理逻辑
                            String errorMsg = resObj.optString("errorMsg", "未知错误");
                            String errorCode = resObj.optString("errorCode", "UNKNOWN");
                            Log.error(TAG, "❌ 奖励领取失败 [" + errorCode + "]: " + errorMsg);
                            return false;
                        }
                    } catch (Exception e) {
                        Log.error(TAG, "领取流程异常: " + e.getMessage());
                        return false;
                    }
                }

                // --- 如果状态是待完成，则执行去完成逻辑 ---
                if ("SIGNUP_COMPLETE".equals(status) || "INIT".equals(status)) {
                    return switch (type) {
                        case "PROMOKERNEL_TASK" -> handlePromoKernelTask(task, title);
                        case "LIGHT_TASK" -> handleLightTask(task, title, jumpLink);
                        default -> {
                            Log.error(TAG, "未处理的任务类型：" + type);
                            yield false;
                        }
                    };
                }

                // 其他状态（如 FINISHED）
                Log.record(TAG, "任务状态为 " + status + "，跳过执行");
                return true;

            } catch (Exception e) {
                Log.printStackTrace(TAG, "handleSingleTask 异常", e);
                return false;
            }
        }

        /**
         * 处理健康岛浏览任务
         */
        private void handleHealthIslandTask() {
            try {
                Log.record(TAG, "开始检查健康岛浏览任务");

                boolean hasTask = true;  // 用于判断是否还有任务

                while (hasTask) {
                    // 1. 查询健康岛任务信息
                    JSONObject taskInfoResp = new JSONObject(
                            AntSportsRpcCall.NeverlandRpcCall.queryTaskInfo("health-island", "LIGHT_FEEDS_TASK")
                    );

                    if (!ResChecker.checkRes(TAG + "查询健康岛浏览任务失败:", taskInfoResp)
                            || taskInfoResp.optJSONObject("data") == null) {

                        Log.error(TAG, "健康岛浏览任务查询失败 ["+taskInfoResp+"] 请关闭此功能");
                        return;
                    }

                    JSONArray taskInfos = taskInfoResp.getJSONObject("data").optJSONArray("taskInfos");

                    // 如果没有任务，跳出循环
                    if (taskInfos == null || taskInfos.length() == 0) {
                        Log.record(TAG, "健康岛浏览任务列表为空");
                        hasTask = false;  // 停止循环
                        continue;
                    }

                    // 2. 遍历处理每个任务
                    for (int i = 0; i < taskInfos.length(); i++) {
                        JSONObject taskInfo = taskInfos.getJSONObject(i);
                        String encryptValue = taskInfo.optString("encryptValue");
                        int energyNum = taskInfo.optInt("energyNum", 0);
                        int viewSec = taskInfo.optInt("viewSec", 15);

                        if (encryptValue.isEmpty()) {
                            Log.error(TAG, "健康岛任务 encryptValue 为空，跳过");
                            continue;
                        }

                        Log.record(TAG, "健康岛浏览任务：能量+" + energyNum + "，需等待" + viewSec + "秒");

                        // 3. 等待浏览时间
                        sleepCompat(viewSec /3);

                        // 4. 领取奖励
                        JSONObject receiveResp = new JSONObject(
                                AntSportsRpcCall.NeverlandRpcCall.energyReceive(encryptValue, energyNum, "LIGHT_FEEDS_TASK",null)
                        );

                        if (ResChecker.checkRes(TAG + "领取健康岛任务奖励:", receiveResp)
                                && ResChecker.checkRes(TAG, receiveResp)) {
                            Log.other("✅ 健康岛浏览任务完成，获得能量+" + energyNum);
                        } else {
                            Log.error(TAG, "健康岛任务领取失败: " + receiveResp);
                        }

                        sleepCompat(1000); // 任务间隔
                    }
                }
            } catch (Throwable t) {
                Log.printStackTrace(TAG, "handleHealthIslandTask err", t);
            }
        }

        /**
         * 处理 PROMOKERNEL_TASK（活动类任务）
         */
        private boolean handlePromoKernelTask(JSONObject task, String title) {
            try {
                // 补充必填参数 scene
                task.put("scene", "MED_TASK_HALL");
                JSONObject res = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.taskSend(task));

                if (ResChecker.checkRes(TAG,res)) {
                    Log.other("✔ 活动任务完成：" + title);
                    return true;
                } else {
                    Log.error(TAG, "taskSend 失败: "+task+" 响应："+res);
                    return false;
                }
            } catch (Exception e) {
                Log.printStackTrace(TAG, "handlePromoKernelTask 处理 PROMOKERNEL_TASK 异常（" + title + "）", e);
                return false;
            }
        }
/*
        /**
         * 处理 LIGHT_TASK（浏览类任务）

private boolean handleLightTask(JSONObject task, String title, String jumpLink) {
    try {
        String bizId = extractBizIdFromJumpLink(jumpLink);
        if (bizId == null || bizId.isEmpty()) {
            Log.error(TAG, "LIGHT_TASK 未找到 bizId：" + title + " jumpLink=" + jumpLink);
            return false;
        }

        JSONObject res = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.finish(bizId));
        if (ResChecker.checkRes(TAG,res) || "0".equals(res.optString("errCode", ""))) {
            Log.other("✔ 浏览任务完成：" + title);
            return true;
        } else {
            Log.error(TAG, "完成 LIGHT_TASK "+task+"失败: "+title + res);
            return false;
        }
    } catch (Exception e) {
        Log.printStackTrace(TAG, "handleLightTask 处理 LIGHT_TASK 异常（" + title + "）", e);
        return false;
    }
}*/


        /**
         * 处理 LIGHT_TASK（浏览类任务）
         */
        private boolean handleLightTask(JSONObject task, String title, String jumpLink) {
            try {
                // 1. 提取 bizId (优先从根部取，其次从 logExtMap 取)
                String bizId = task.optString("bizId", "");
                if (bizId.isEmpty()) {
                    JSONObject logExtMap = task.optJSONObject("logExtMap");
                    if (logExtMap != null) {
                        bizId = logExtMap.optString("bizId", "");
                    }
                }

                if (bizId.isEmpty()) {
                    Log.error(TAG, "LIGHT_TASK 未找到 bizId：" + title + " jumpLink=" + jumpLink);
                    return false;
                }

                //Log.record(TAG, "正在执行 LIGHT_TASK 浏览任务: " + title + " [bizId=" + bizId + "]");

                // 2. 调用完成接口
                JSONObject res = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.finish(bizId));

                // 3. 校验结果
                if (res.optBoolean("success", false) || "0".equals(res.optString("errCode", ""))) {
                    // 解析奖励信息
                    String rewardMsg = "";
                    JSONObject extendInfo = res.optJSONObject("extendInfo");
                    if (extendInfo != null) {
                        JSONObject rewardInfo = extendInfo.optJSONObject("rewardInfo");
                        if (rewardInfo != null) {
                            String amount = rewardInfo.optString("rewardAmount", "0");
                            rewardMsg = " (获得奖励: " + amount + " 能量)";
                        }
                    }

                    Log.other("✔ 浏览任务完成：" + title + rewardMsg);
                    return true;
                } else {
                    Log.error(TAG, "完成 LIGHT_TASK 失败: " + title + " 返回: " + res.toString());
                    return false;
                }
            } catch (Exception e) {
                Log.printStackTrace(TAG, "handleLightTask 处理 LIGHT_TASK 异常（" + title + "）", e);
                return false;
            }
        }

        // -------------------------------------------------------------------------
        // 3. 捡泡泡（无变更，仅调整执行时机）
        // -------------------------------------------------------------------------

        private void neverlandPickAllBubble() {
            try {
                Log.record(TAG, "健康岛 · 检查可领取泡泡");

                JSONObject jo = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.queryBubbleTask());

                if (!ResChecker.checkRes(TAG + "查询泡泡失败:", jo)

                        || jo.optJSONObject("data") == null) {
                    Log.error(TAG, "queryBubbleTask raw=" + jo);
                    return;
                }

                JSONArray arr = jo.getJSONObject("data").optJSONArray("bubbleTaskVOS");
                if (arr == null || arr.length() == 0) {
                    Log.other("无泡泡可领取");
                    return;
                }

                List<String> ids = new ArrayList<>();
                List<String> encryptValues = new ArrayList<>();  // 用于存储需要浏览的任务的 encryptValue

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    String bubbleTaskStatus = item.optString("bubbleTaskStatus");
                    String encryptValue = item.optString("encryptValue");
                    int energyNum = item.optInt("energyNum", 0);  // 动态获取能量值
                    int viewSec = item.optInt("viewSec", 15);  // 获取浏览时间

                    // 如果任务状态为 INIT，并且有 encryptValue，添加到处理队列
                    if ("INIT".equals(bubbleTaskStatus) && !encryptValue.isEmpty()) {
                        encryptValues.add(encryptValue);
                        Log.record(TAG, "找到可浏览任务： " + item.optString("title") + "，能量+" + energyNum + "，需等待" + viewSec + "秒");
                    } else if (!item.optBoolean("initState") && !item.optString("medEnergyBallInfoRecordId").isEmpty()) {
                        // 否则继续处理其他类型的任务
                        ids.add(item.getString("medEnergyBallInfoRecordId"));
                    }
                }

                if (ids.isEmpty() && encryptValues.isEmpty()) {
                    Log.record(TAG, "没有可领取的泡泡任务");
                    return;
                }

                // 处理普通泡泡任务
                if (!ids.isEmpty()) {
                    Log.record(TAG, "健康岛 · 正在领取 " + ids.size() + " 个泡泡…");
                    JSONObject pick = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.pickBubbleTaskEnergy(ids));

                    if (!ResChecker.checkRes(TAG + "领取泡泡失败:", pick)
                            || pick.optJSONObject("data") == null) {
                        Log.error(TAG, "pickBubbleTaskEnergy raw=" + pick);
                        return;
                    }

                    JSONObject data = pick.getJSONObject("data");
                    String changeAmount = data.optString("changeAmount", "0");
                    String balance = data.optString("balance", "0");
                    if ("0".equals(changeAmount)) {
                        Log.record(TAG, "健康岛 · 本次未获得任何能量");
                    } else {
                        Log.other("捡泡泡成功 🎈 +" + changeAmount + " 余额：" + balance);
                    }
                }

                // 处理需要浏览的任务 (和浏览任务类似)
                for (String encryptValue : encryptValues) {
                    Log.record(TAG, "开始浏览任务，任务 encryptValue: " + encryptValue);

                    // 获取浏览任务对应的能量值和浏览时间
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        if (encryptValue.equals(item.optString("encryptValue"))) {
                            int energyNum = item.optInt("energyNum", 0);
                            int viewSec = item.optInt("viewSec", 15);
                            String title =item.optString("title");
                            //Log.record(TAG, "浏览任务 ["+title+"]+"+energyNum+"能量");

                            // 3. 等待浏览时间
                            sleepCompat(viewSec * 1000L); // 假设每个浏览任务的时间是15秒

                            // 4. 提交浏览任务，领取奖励
                            JSONObject receiveResp = new JSONObject(
                                    AntSportsRpcCall.NeverlandRpcCall.energyReceive(encryptValue, energyNum, "LIGHT_FEEDS_TASK","adBubble")
                            );

                            if (ResChecker.checkRes(TAG + "领取泡泡任务奖励:", receiveResp)
                            ) {
                                Log.other("✅ 浏览任务["+title+"]"+"完成，获得能量+" + energyNum);
                            } else {
                                Log.error(TAG, "浏览任务领取失败: " + receiveResp);
                            }

                            sleepCompat(Math.round(1000 + Math.random() * 1000) ); // 任务随机1-2秒等待，我就不看15秒
                            break;  // 找到对应任务后跳出循环
                        }
                    }
                }

            } catch (Throwable t) {
                Log.printStackTrace(TAG, "neverlandPickAllBubble err:", t);
            }
        }

        // -------------------------------------------------------------------------
        // 4. 自动走路任务处理
        // -------------------------------------------------------------------------
        // =========================================================================
        // 步数上限检查 - 公共方法
        // =========================================================================
        /**
         * 检查今日 步数是否达到上限
         * @return 剩余可走步数,如果返回 0 或负数表示已达上限
         */
        
        private int checkDailyStepLimit() {
            Integer stepCount = Status.getIntFlagToday(StatusFlags.FLAG_NEVERLAND_STEPCOUNT);
            if (stepCount == null) {
                stepCount = 0;
            }
            int maxStepLimit = neverlandGridStepCount.getValue();
            int remainSteps = maxStepLimit - stepCount;

            Log.record(TAG, String.format("今日步数统计: 已走 %d/%d 步, 剩余 %d 步",
                    stepCount, maxStepLimit, Math.max(0, remainSteps)));

            return remainSteps;
        }

        /**
         * 记录步数增加
         * @param addedSteps 本次增加的步数
         * @return 更新后的总步数
         */
        private int recordStepIncrease(int addedSteps) {
            if (addedSteps <= 0) {
                return Status.getIntFlagToday(StatusFlags.FLAG_NEVERLAND_STEPCOUNT);
            }

            Integer currentSteps = Status.getIntFlagToday(StatusFlags.FLAG_NEVERLAND_STEPCOUNT);
            if (currentSteps == null) {
                currentSteps = 0;
            }

            int newSteps = currentSteps + addedSteps;
            Status.setIntFlagToday(StatusFlags.FLAG_NEVERLAND_STEPCOUNT, newSteps);

            int maxLimit = neverlandGridStepCount.getValue();
            Log.record(TAG, String.format("步数增加: +%d 步, 当前总计 %d/%d 步",
                    addedSteps, newSteps, maxLimit));

            return newSteps;
        }

        // =========================================================================
        // 健康岛自动任务
        // =========================================================================
        /**
         * 健康岛走路建造任务入口
         *
         * <p>功能说明:</p>
         * <ul>
         *   <li>自动检测游戏模式(新游戏建造模式 or 旧版行走模式)</li>
         *   <li>检查每日步数上限和能量余额</li>
         *   <li>根据模式自动执行对应任务</li>
         * </ul>
         *
         * <p>执行流程:</p>
         * <ol>
         *   <li>查询基础信息,判断游戏模式</li>
         *   <li>检查每日步数限额</li>
         *   <li>检查能量余额</li>
         *   <li>分发到对应的任务处理函数</li>
         * </ol>
         *
         */
        private void neverlandAutoTask() {
            try {
                Log.record(TAG, "健康岛 · 启动走路建造任务");

                // ========== 1. 查询基础信息 ==========
                JSONObject baseInfo = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.queryBaseinfo());
                if (!ResChecker.checkRes(TAG + " 查询基础信息失败:", baseInfo)
                        || baseInfo.optJSONObject("data") == null) {
                    Log.error(TAG, "queryBaseinfo 失败, 响应数据: " + baseInfo);
                    return;
                }

                JSONObject baseData = baseInfo.getJSONObject("data");
                boolean isNewGame = baseData.optBoolean("newGame", false);
                String branchId = baseData.optString("branchId", "MASTER");
                String mapId = baseData.optString("mapId", "");
                String mapName = baseData.optString("mapName", "未知地图");

                Log.record(TAG, String.format("当前地图: [%s](%s) | 模式: %s",
                        mapName, mapId, isNewGame ? "新游戏建造" : "旧版行走"));

                // ========== 2. 检查每日步数上限 ==========
                int remainSteps = checkDailyStepLimit();
                if (remainSteps <= 0) {
                    Log.record(TAG, "今日步数已达上限, 任务结束");
                    return;
                }

                // ========== 3. 查询剩余能量 ==========
                int leftEnergy = queryUserEnergy();
                if (leftEnergy < 5) {
                    Log.record(TAG, "剩余能量不足(< 5), 无法执行任务");
                    return;
                }

                // ========== 4. 根据模式分发任务 ==========
                if (isNewGame) {
                    executeAutoBuild(branchId, mapId, remainSteps, leftEnergy,mapName);
                } else {
                    executeAutoWalk(branchId, mapId,remainSteps, leftEnergy,mapName);
                }

                Log.record(TAG, "健康岛自动走路建造执行完成 ✓");

            } catch (Throwable t) {
                Log.printStackTrace(TAG, "neverlandAutoTask 发生异常"+ t,t);
            }
        }

        // =========================================================================
        // 辅助函数
        // =========================================================================
        /**
         * 查询用户剩余能量
         *
         * @return 剩余能量值,查询失败返回 0
         */
        private int queryUserEnergy() {
            try {
                JSONObject energyResp = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.queryUserEnergy());
                if (!ResChecker.checkRes(TAG + " 查询用户能量失败:", energyResp)
                        || energyResp.optJSONObject("data") == null) {
                    Log.error(TAG, "queryUserEnergy 失败, 响应数据: " + energyResp);
                    return 0;
                }

                int balance = energyResp.getJSONObject("data").optInt("balance", 0);
                Log.record(TAG, "当前剩余能量: " + balance);
                return balance;

            } catch (Throwable t) {
                Log.printStackTrace(TAG, "queryUserEnergy err",t);
                return 0;
            }
        }

        // =========================================================================
        // 旧版行走模式
        // =========================================================================
        /**
         * 执行自动行走任务(能量泵 走路模式)
         *
         * @param BranchId 基础分支 ID
         * @param mapId 走路用的
         * @param remainSteps 剩余可用步数
         * @param leftEnergy 剩余能量
         * @param mapName 地图名称，显示用的，没啥卵用
         */
        
        private void executeAutoWalk(String BranchId, String mapId,int remainSteps, int leftEnergy, String mapName) {
            try {
                Log.record(TAG, "开始执行旧版行走任务");
                // ====== 0. 查询地图信息，判断是否可走 ======
                JSONObject mapInfoResp = new JSONObject(
                        AntSportsRpcCall.NeverlandRpcCall.queryMapInfo(mapId,BranchId));

                if (!ResChecker.checkRes(TAG + " queryMapInfo 失败:", mapInfoResp)
                        || mapInfoResp.optJSONObject("data") == null) {
                    Log.error(TAG, "queryMapInfo 失败，终止走路任务");
                    return;
                }

                JSONObject mapInfo = mapInfoResp.getJSONObject("data");
                if (!mapInfo.optBoolean("canWalk", false)) {
                    Log.record(TAG, "当前地图不可走(canWalk=false)，跳过走路任务");
                    return;
                }

                // 记录初始星星数（用于后续对比）
                JSONObject mapStarData = mapInfo.optJSONObject("starData");
                int lastCurrStar = mapStarData != null ? mapStarData.optInt("curr", 0) : 0;


                // ====== 1. 开始走路 ======
                for (int i = 0; i < remainSteps; i++) {

                    if (leftEnergy < 5) {
                        Log.record(TAG, "[" + mapName + "] 能量不足(< 5), 停止走路任务");
                        break;
                    }

                    JSONObject walkResp = new JSONObject(
                            AntSportsRpcCall.NeverlandRpcCall.walkGrid(BranchId, mapId, false));

                    if (!ResChecker.checkRes(TAG + " walkGrid 失败:", walkResp)
                            || walkResp.optJSONObject("data") == null) {

                        String errorCode = walkResp.optString("errorCode", "");
                        Log.error(TAG, String.format(
                                "walkGrid 失败, 错误码: %s, 响应数据: %s",
                                errorCode, walkResp));
                        break;
                    }

                    // ====== 处理走路结果 ======
                    JSONObject walkData = walkResp.getJSONObject("data");
                    leftEnergy = walkData.optInt("leftCount", leftEnergy);

                    // 步数
                    int stepIncrease = 1;
                    recordStepIncrease(stepIncrease);
                    int stepThisTime = extractStepIncrease(walkData);

                    // 星星判断
                    JSONObject starData = walkData.optJSONObject("starData");
                    int currStar = starData != null ? starData.optInt("curr", lastCurrStar) : lastCurrStar;
                    int maxStar = starData != null ? starData.optInt("count", 0) : Integer.MAX_VALUE;
                    boolean starIncreased = currStar > lastCurrStar;
                    lastCurrStar = currStar;

                    // 红包判断
                    int redPocketAdd = 0;
                    JSONArray userItems = walkData.optJSONArray("userItems");
                    if (userItems != null && userItems.length() > 0) {
                        JSONObject item = userItems.optJSONObject(0);
                        if (item != null) {
                            redPocketAdd = item.optInt("modifyCount", item.optInt("count", 0));
                        }
                    }

                    // ====== 输出提示 ======
                    StringBuilder sb = new StringBuilder();
                    sb.append("[").append(mapName).append("] 前进 ").append(stepThisTime).append(" 步，");

                    if (starIncreased) {
                        sb.append("获得 🌟");
                    } else if (redPocketAdd > 0) {
                        sb.append("获得 🧧 +").append(redPocketAdd);
                    } else {
                        sb.append("啥也没有");
                    }

                    Log.other(sb.toString());

                    // 地图阶段奖励（你原有逻辑）
                    tryReceiveStageReward(BranchId, mapId, starData);
                    // ====== 关键判断：地图是否完成 ======
                    if (currStar >= maxStar) {
                        Log.other("[" + mapName + "] 当前地图已完成星星，准备切换地图");
                        chooseAvailableMap();
                        break; // 结束循环，切换地图
                    }
                    Thread.sleep(888);
                }
                Log.record(TAG, "自动走路任务完成 ✓");

            } catch (Throwable t) {
                Log.printStackTrace(TAG,"executeAutoWalk err", t);
            }
        }

        private void tryReceiveStageReward(String branchId, String mapId, JSONObject starData) {
            if (starData == null) return;

            int rewardLevel = starData.optInt("rewardLevel", -1);
            if (rewardLevel <= 0) return;

            JSONArray recordArr = starData.optJSONArray("stageRewardRecord");
            if (recordArr != null) {
                for (int i = 0; i < recordArr.length(); i++) {
                    if (recordArr.optInt(i, -1) == rewardLevel) {
                        return; // 已领取
                    }
                }
            }

            Log.other(String.format("检测到未领取关卡奖励 🎁 map=%s 等级: %d，尝试领取…",mapId, rewardLevel));

            // ========== 1️⃣ 先拿字符串 ==========
            String rewardStr;
            try {
                rewardStr = AntSportsRpcCall.NeverlandRpcCall
                        .mapStageReward(branchId, rewardLevel, mapId);
            } catch (Throwable t) {

                Log.printStackTrace(TAG,"mapStageReward RPC 调用异常", t);
                return;
            }

            // ========== 2️⃣ 判空 ==========
            if (rewardStr.isEmpty()) {
                Log.error(TAG, "mapStageReward 返回空字符串");
                return;
            }

            rewardStr = rewardStr.trim();

            // ========== 3️⃣ 必须是 JSON ==========
            if (!rewardStr.startsWith("{")) {
                Log.error(TAG, "mapStageReward 返回非 JSON: " + rewardStr);
                return;
            }

            // ========== 4️⃣ 安全构造 JSONObject ==========
            JSONObject rewardResp;
            try {
                rewardResp = new JSONObject(rewardStr);
            } catch (Throwable t) {
                Log.printStackTrace(TAG, "mapStageReward JSON 解析失败",t);
                return;
            }

            // ========== 5️⃣ 业务判断 ==========
            if (!ResChecker.checkRes(TAG,rewardResp)) {
                String errCode = rewardResp.optString("errorCode", "");
                if ("ASSET_ITEM_NOT_EXISTED".equals(errCode)) {
                    Log.other("关卡奖励已被领取或不存在（可忽略）");
                } else {
                    Log.error(TAG, "领取关卡奖励失败: " + rewardResp);
                }
                return;
            }

            // ========== 6️⃣ 解析奖励 ==========
            JSONObject data = rewardResp.optJSONObject("data");
            JSONObject receiveResult = data != null ? data.optJSONObject("receiveResult") : null;

            if (receiveResult == null) {
                Log.record(TAG, "关卡奖励领取成功 🎉（无奖励详情）");
                return;
            }

            JSONArray prizes = receiveResult.optJSONArray("prizes");
            String balance = receiveResult.optString("balance", "");

            if (prizes != null && prizes.length() > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < prizes.length(); i++) {
                    JSONObject p = prizes.optJSONObject(i);
                    if (p == null) continue;

                    sb.append(p.optString("title", "未知奖励"))
                            .append(" x")
                            .append(p.optString("modifyCount", "1"));

                    if (i != prizes.length() - 1) sb.append("，");
                }

                Log.other(String.format(
                        "Lv.%s 奖励领取成功 🎉 %s | 当前余额: %s",
                        rewardLevel,sb, balance
                ));
            } else {
                Log.other("关卡奖励领取成功 🎉（无可展示奖励）");
            }
        }

        /**
         * 查询地图列表，优先返回状态为 DOING 的地图；
         * 若不存在 DOING，则从状态为 LOCKED 的地图中随机选择一个并尝试切换；
         * 若切换成功，则返回切换后的地图对象。
         *
         * 优化点：使用单次 for 循环完成 DOING 查找和 LOCKED 收集，避免 Stream API 转换开销。
         * * @return 最终选中的地图对象；失败时返回 null
         *
         * {
         *         "branchId": "MASTER",
         *         "currentPercent": 0,
         *         "islandImageUrl": "<a href="<a">href="https://mdn.alipayobjects.com/huamei_nbiabh/a</a>fts/img/A">...</a>*TuESSqNLW9YAAAAAQ6AAAAgAegH_AQ/fmt.avif",
         *         "mapId": "MM125",
         *         "mapName": "悦动药材岛",
         *         "mapType": "COMMON",
         *         "newIsLandFlg": true,
         *         "order": 268009,
         *         "recommendRewardToast": "",
         *         "status": "LOCKED"
         *       }
         */
        private JSONObject chooseAvailableMap() {
            try {
                // 1. 查询地图列表并进行基础检查
                JSONObject mapResp = new JSONObject(AntSportsRpcCall.NeverlandRpcCall.queryMapList());

                // 统一且精简的错误检查
                if (!ResChecker.checkRes(TAG + " 查询地图失败:", mapResp) ) {
                    Log.error(TAG, "queryMapList 失败: " + mapResp);
                    return null;
                }

                JSONObject data = mapResp.optJSONObject("data");
                JSONArray mapList = data != null ? data.optJSONArray("mapList") : null;

                if (mapList == null || mapList.length() == 0) {
                    Log.error(TAG, "地图列表为空");
                    return null;
                }

                JSONObject doingMap = null;
                List<JSONObject> lockedMaps = new ArrayList<>();

                // 2. 单次循环扫描 DOING (优先) / LOCKED
                for (int i = 0; i < mapList.length(); i++) {
                    JSONObject map = mapList.getJSONObject(i);
                    String status = map.optString("status");

                    if ("DOING".equals(status)) {
                        doingMap = map;
                        break; // 找到 DOING，立即跳出循环，实现优先级
                    } else if ("LOCKED".equals(status)) {
                        lockedMaps.add(map);
                    }
                }

                // 3. 处理 DOING 地图：如果存在，执行一次切换以确保状态同步
                if (doingMap != null) {
                    Log.other("当前 DOING 地图: "+doingMap.optString("mapName") + doingMap.optString("mapId") + " → 执行一次强制切换确保状态一致");
                    return chooseMap(doingMap); // 调用统一的切换方法
                }

                // 4. 处理 LOCKED 地图：随机选择并尝试切换
                if (lockedMaps.isEmpty()) {
                    Log.error(TAG, "没有 DOING 且没有可选的 LOCKED 地图");
                    return null;
                }

                JSONObject chosenLocked = lockedMaps.get(new Random().nextInt(lockedMaps.size()));
                Log.other("随机选择 LOCKED 地图: " + chosenLocked.optString("mapId"));
                return chooseMap(chosenLocked); // 调用统一的切换方法

            } catch (Throwable t) {
                Log.printStackTrace(TAG,"chooseAvailableMap err", t);
                return null;
            }
        }

        /**
         * 统一的地图切换方法，简化并统一日志逻辑。
         * 替代了原来的 chooseMapAndLog，移除了 isDoing 逻辑。
         *
         * @param map 要切换的地图对象
         * @return 切换成功的地图对象；失败时返回 null
         */
        private JSONObject chooseMap(JSONObject map) {
            try {
                String mapId = map.optString("mapId");
                String branchId = map.optString("branchId");

                JSONObject resp = new JSONObject(
                        AntSportsRpcCall.NeverlandRpcCall.chooseMap(branchId, mapId)
                );

                if (ResChecker.checkRes(TAG,resp)) {
                    Log.record(TAG, "切换地图成功: " + mapId);
                    return map;
                } else {
                    Log.error(TAG, "切换地图失败: " + resp);
                    return null;
                }
            } catch (Throwable t) {
                // 统一异常日志，避免原来的 Log.error(TAG, "")
                Log.printStackTrace(TAG,"chooseMap err", t);
                return null;
            }
        }

        /**
         * 从 walkData 中提取步数增量
         *
         * @param walkData 走路响应数据
         * @return 步数增量
         */
        private int extractStepIncrease(JSONObject walkData) {
            try {
                JSONArray mapAwards = walkData.optJSONArray("mapAwards");
                if (mapAwards != null && mapAwards.length() > 0) {
                    return mapAwards.getJSONObject(0).optInt("step", 0);
                }
            } catch (Throwable t) {
                Log.printStackTrace(TAG, t);
            }
            return 0;
        }

        // =========================================================================
        // 新游戏建造模式
        // =========================================================================
        /**
         * 执行自动建造任务(新游戏模式)
         *
         * <p>功能说明:</p>
         * <ul>
         *   <li>根据剩余步数和能量计算建造倍数</li>
         *   <li>循环执行 build 直到能量或步数耗尽</li>
         *   <li>实时记录建造进度和奖励</li>
         * </ul>
         *
         * @param branchId 分支 ID
         * @param mapId 地图 ID
         * @param remainSteps 剩余可用步数
         * @param leftEnergy 剩余能量
         */
        
        private void executeAutoBuild(String branchId, String mapId, int remainSteps, int leftEnergy, String MapName) {
            try {
                Log.other(String.format("开始执行建造任务, 地图: %s", mapId));

                // 1. 首次查询地图信息
                String resp = AntSportsRpcCall.NeverlandRpcCall.queryMapInfoNew(mapId);
                JSONObject mapInfo = new JSONObject(resp);

                if (!ResChecker.checkRes(TAG + " 查询建造地图失败", mapInfo)) {
                    Log.error(TAG, "查询建造地图失败 " + mapInfo);
                    return;
                }
                JSONObject data = mapInfo.optJSONObject("data");
                if (data == null) {
                    Log.error(TAG, "地图Data 为空，无法解析");
                    return;
                }

                int mapEnergyFinal = data.optInt("mapEnergyFinal");     // 最终进度
                int mapEnergyProcess = data.optInt("mapEnergyProcess"); // 当前进度
                JSONArray buildings = data.optJSONArray("buildingConfigInfos");
                int lastBuildingIndex = -1;

                if (buildings != null && buildings.length() > 0) {
                    lastBuildingIndex = buildings.getJSONObject(buildings.length() - 1)
                            .optInt("buildingIndex", -1);
                    Log.record(TAG, "最后一个建筑 Index: " + lastBuildingIndex);
                }

                // 2. 地图完成后的处理逻辑
                if (mapEnergyProcess == mapEnergyFinal) {
                    Log.record(TAG, "当前地图已建造完成，准备切换地图...");
                    JSONObject choiceMapInfo = chooseAvailableMap(); // 尝试切换到 DOING/LOCKED

                    if (choiceMapInfo == null) {
                        Log.error(TAG, "切换地图失败，可能无可用地图，任务终止。");
                        return;
                    }

                    // 检查新选择的地图是否是需要继续建造的地图 (newIsLandFlg=true)
                    if (choiceMapInfo.optBoolean("newIsLandFlg", true)) { // 默认值设为 true，确保安全
                        // **关键修复：更新参数变量，以便继续执行 while 循环**
                        branchId = choiceMapInfo.optString("branchId");
                        mapId = choiceMapInfo.optString("mapId");
                        Log.record(TAG, String.format("成功切换到可建造的新地图: %s，继续执行建造。", mapId));

                        // 注意：这里没有 return，代码会跳过 if 块，直接进入下面的 while 循环，
                        // 此时 while 循环将使用新的 branchId 和 mapId 继续执行。

                    } else {
                        // newIsLandFlg 为 false，通常意味着这是用户需要走路才能解锁的特殊地图
                        Log.record(TAG, String.format("已切换至走路地图: %s，将在下次运行时执行，任务终止。", mapId));
                        return;
                    }
                }

                // 3. 进入循环建造阶段
                while (remainSteps > 0 && leftEnergy >= 5) {

                    // 计算本次建造倍数
                    int maxMulti = Math.min(10, remainSteps);
                    int energyBasedMulti = leftEnergy / 5;
                    int multiNum = Math.min(maxMulti, energyBasedMulti);

                    // 执行 build RPC
                    JSONObject buildResp = new JSONObject(
                            AntSportsRpcCall.NeverlandRpcCall.build(branchId, mapId, multiNum));

                    if (!ResChecker.checkRes(TAG + " build 失败:", buildResp)) {
                        Log.error(TAG, String.format("build 失败, multiNum=%d, 响应: %s",
                                multiNum, buildResp));
                        break;
                    }

                    JSONObject buildData = buildResp.optJSONObject("data");

                    // 优化：build data 为空，表示本次建造任务已完成当前地图
                    if (buildData == null || buildData.length() == 0) {
                        Log.record(TAG, "⚠️ build响应数据为空，当前地图已达限制，任务重新进入地图完成处理流程。");
                        JSONObject choiceMapInfo = chooseAvailableMap(); // 尝试切换到 DOING/LOCKED
                        return;
                        // 重新执行地图完成逻辑，尝试切换到下一张地图
                        // 此处需要递归调用或重构，但最简单的处理是 break，让程序在下一次 run 的时候处理。
                        // 因为一旦 break，任务就结束了。如果想立刻处理，需要重构方法。
                        //break;
                    }
                    // 获取建造前后的数据
                    JSONObject before = buildData.optJSONObject("beforeStageInfo");
                    JSONObject end = buildData.optJSONObject("endStageInfo");

                    int actualUsedEnergy = 0;

                    if (before != null && end != null) {
                        int bIdxBefore = before.optInt("buildingIndex");
                        int bIdxEnd = end.optInt("buildingIndex");

                        if (bIdxEnd > bIdxBefore) {
                            // 情况 A: (B3 -> B4)
                            // 消耗 = (B3的总进度 - B3开始时的进度) + B4当前的进度
                            actualUsedEnergy = (before.optInt("buildingEnergyFinal") - before.optInt("buildingEnergyProcess"))
                                    + end.optInt("buildingEnergyProcess");
                        } else {
                            // 情况 B: 还在同一个建筑
                            actualUsedEnergy = end.optInt("buildingEnergyProcess") - before.optInt("buildingEnergyProcess");
                        }
                    } else {
                        // 保底：按倍数扣除
                        actualUsedEnergy = multiNum * 5;
                    }
                    


                    leftEnergy -= actualUsedEnergy;
                    int stepIncrease = calculateBuildSteps(buildData, multiNum);
                    int totalSteps = recordStepIncrease(stepIncrease);
                    remainSteps -= stepIncrease;

                    String awardInfo = extractAwardInfo(buildData);

                    Log.other(String.format("建造进度 🏗️ 倍数: x%d | 能量: %d | 本次: +%d | 今日: %d/%d%s",multiNum, leftEnergy, stepIncrease, totalSteps,neverlandGridStepCount.getValue(), awardInfo));

                    sleepCompat(1000);
                }

                Log.other("自动建造任务完成 ✓");

            } catch (Throwable t) {
                Log.printStackTrace(TAG,"executeAutoBuild err", t);
            }
        }

        /**
         * 计算建造实际产生的步数
         *
         * @param buildData 建造响应数据
         * @param defaultMulti 默认倍数
         * @return 实际步数
         */
        private int calculateBuildSteps(JSONObject buildData, int defaultMulti) {
            try {
                JSONArray buildResults = buildData.optJSONArray("buildResults");
                if (buildResults != null && buildResults.length() > 0) {
                    return buildResults.length();
                }
            } catch (Throwable t) {
                Log.printStackTrace(TAG, t);
            }
            return defaultMulti;
        }

        /**
         * 提取建造奖励信息
         *
         * @param buildData 建造响应数据
         * @return 奖励描述字符串
         */
        
        private String extractAwardInfo(JSONObject buildData) {
            try {
                JSONArray awards = buildData.optJSONArray("awards");
                if (awards != null && awards.length() > 0) {
                    return String.format(" | 获得奖励: %d 项", awards.length());
                }
            } catch (Throwable t) {
                Log.printStackTrace(TAG, t);
            }
            return "";
        }

        // -------------------------------------------------------------------------
        // 工具函数（bizId提取逻辑无变更）
        // -------------------------------------------------------------------------

//        private String extractBizIdFromJumpLink(String jumpLink) {
//            if (jumpLink == null || jumpLink.isEmpty()) return null;
//
//            try {
//                // 格式1：直接提取 bizId 参数（含URL编码）
//                int idx = jumpLink.indexOf("bizId=");
//                if (idx < 0) idx = jumpLink.indexOf("bizId%3D");
//                if (idx >= 0) {
//                    int start = jumpLink.indexOf("=", idx) + 1;
//                    int end = jumpLink.indexOf("&", start);
//                    if (end < 0) end = jumpLink.length();
//                    String bizId = URLDecoder.decode(jumpLink.substring(start, end), StandardCharsets.UTF_8).trim();
//                    if (!bizId.isEmpty()) return bizId;
//                }
//
//                // 格式2：从 cdpQueryParams 提取
//                if (jumpLink.contains("cdpQueryParams=")) {
//                    int cdpIdx = jumpLink.indexOf("cdpQueryParams=");
//                    int cdpStart = jumpLink.indexOf("=", cdpIdx) + 1;
//                    int cdpEnd = jumpLink.indexOf("&", cdpStart);
//                    if (cdpEnd < 0) cdpEnd = jumpLink.length();
//                    String cdpEncoded = jumpLink.substring(cdpStart, cdpEnd);
//                    String cdpJson = URLDecoder.decode(cdpEncoded, StandardCharsets.UTF_8);
//                    JSONObject cdpObj = new JSONObject(cdpJson);
//                    String bizId = cdpObj.optString("bizId", "").trim();
//                    if (!bizId.isEmpty()) return bizId;
//                }
//
//                // 兼容32位hex格式
//                String candidate = jumpLink.replaceAll("%26", "&");
//                if (candidate.length() >= 32) {
//                    for (int i = 0; i + 32 <= candidate.length(); i++) {
//                        String sub = candidate.substring(i, i + 32);
//                        if (sub.matches("[0-9a-fA-F]{32}")) return sub;
//                    }
//                }
//
//            } catch (Exception e) {
//                Log.error(TAG, "extractBizIdFromJumpLink 解析失败，jumpLink=" + jumpLink);
//            }
//            return null;
//        }
    }


    /**
     * @file WalkPathTheme
     * @brief 蚂蚁运动路线主题常量与映射表
     */

    public interface WalkPathTheme {
        int DA_MEI_ZHONG_GUO = 0;      ///< 大美中国 (默认)
        int GONG_YI_YI_XIAO_BU = 1;    ///< 公益一小步
        int DENG_DING_ZHI_MA_SHAN = 2; ///< 登顶芝麻山
        int WEI_C_DA_TIAO_ZHAN = 3;    ///< 维C大挑战
        int LONG_NIAN_QI_FU = 4;       ///< 龙年祈福
        int SHOU_HU_TI_YU_MENG = 5;    ///< 守护体育梦

        /** @brief 界面显示的名称列表 */
        String[] nickNames = {
                "大美中国", "公益一小步", "登顶芝麻山", "维C大挑战", "龙年祈福", "守护体育梦"
        };

        /** * @brief 对应目标应用接口的 ThemeID 映射表
         * @note 数组顺序必须与上方常量定义保持严格一致
         */
        String[] themeIds = {
                "M202308082226", ///< [0] 大美中国
                "M202401042147", ///< [1] 公益一小步
                "V202405271625", ///< [2] 登顶芝麻山
                "202404221422",  ///< [3] 维C大挑战
                "WF202312050200",///< [4] 龙年祈福
                "V202409061650"  ///< [5] 守护体育梦
        };
    }

    public interface DonateCharityCoinType {
        int ONE = 0;
        // --注释掉检查 (2025/12/25 15:13):int ALL = 1;
        String[] nickNames = {"捐赠一个项目", "捐赠所有项目"};
    }

    public interface BattleForFriendType {
        int ROB = 0;
        int DONT_ROB = 1;
        String[] nickNames = {"选中抢", "选中不抢"};
    }
}