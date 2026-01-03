package io.github.lazyimmortal.sesame.model.task.antFarm;

import lombok.Getter;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.lazyimmortal.sesame.data.*;
import io.github.lazyimmortal.sesame.data.ModelFields;
import io.github.lazyimmortal.sesame.data.ModelGroup;
import io.github.lazyimmortal.sesame.data.TokenConfig;
import io.github.lazyimmortal.sesame.data.modelFieldExt.*;
import io.github.lazyimmortal.sesame.data.task.ModelTask;
import io.github.lazyimmortal.sesame.data.modelFieldExt.ChoiceModelField;
import io.github.lazyimmortal.sesame.entity.AlipayUser;
import io.github.lazyimmortal.sesame.entity.CustomOption;
import io.github.lazyimmortal.sesame.entity.FarmOrnaments;
import io.github.lazyimmortal.sesame.model.base.TaskCommon;
import io.github.lazyimmortal.sesame.model.extensions.ExtensionsHandle;
import io.github.lazyimmortal.sesame.model.normal.answerAI.AnswerAI;
import io.github.lazyimmortal.sesame.rpc.intervallimit.RpcIntervalLimit;
import io.github.lazyimmortal.sesame.util.*;
import io.github.lazyimmortal.sesame.util.idMap.FarmOrnamentsIdMap;
import io.github.lazyimmortal.sesame.util.idMap.UserIdMap;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AntFarm extends ModelTask {
    private static final String TAG = AntFarm.class.getSimpleName();

    private String ownerFarmId;
    private String ownerUserId;
    private String ownerGroupId;
    private Animal[] animals;
    private Animal ownerAnimal = new Animal();
    private int foodStock;
    private int foodStockLimit;
    private String rewardProductNum;
    private RewardFriend[] rewardList;
    private double benevolenceScore;
    private double harvestBenevolenceScore;
    private int unReceiveTaskAward = 0;
    private double finalScore = 0d;
    private int foodInTrough = 0;

    private FarmTool[] farmTools;

    @Override
    public String getName() {
        return "庄园";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.FARM;
    }

    private StringModelField sleepTime;
    private IntegerModelField sleepMinutes;
    private BooleanModelField feedAnimal;
    private BooleanModelField rewardFriend;
    private ChoiceModelField sendBackAnimalWay;
    private ChoiceModelField sendBackAnimalType;
    private SelectModelField sendBackAnimalList;
    private ChoiceModelField recallAnimalType;
    private BooleanModelField receiveFarmToolReward;
    private BooleanModelField recordFarmGame;
    private ListModelField.ListJoinCommaToStringModelField farmGameTime;
    private BooleanModelField kitchen;
    private BooleanModelField useSpecialFood;
    @Getter
    private IntegerModelField useSpecialFoodCountLimit;
    private BooleanModelField useNewEggTool;
    private BooleanModelField harvestProduce;
    private ChoiceModelField donationType;
    private IntegerModelField donationAmount;
    private BooleanModelField receiveFarmTaskAward;
    private BooleanModelField useAccelerateTool;
    private SelectModelField useAccelerateToolOptions;
    private BooleanModelField feedFriendAnimal;
    private SelectAndCountModelField feedFriendAnimalList;
    private ChoiceModelField notifyFriendType;
    private SelectModelField notifyFriendList;
    private BooleanModelField acceptGift;
    private SelectAndCountModelField visitFriendList;
    private BooleanModelField chickenDiary;
    private BooleanModelField drawMachine;
    private BooleanModelField ornamentsDressUp;
    private SelectModelField ornamentsDressUpList;
    private IntegerModelField ornamentsDressUpDays;
    private ChoiceModelField hireAnimalType;
    private SelectModelField hireAnimalList;
    private BooleanModelField drawGameCenterAward;

    private BooleanModelField useBigEaterTool; // ✅ 新增加饭卡
    //private ChoiceModelField getFeedType;
    private SelectModelField getFeedList;
    private BooleanModelField family;
    private SelectModelField familyOptions;
    private StringModelField giftFamilyDrawFragment;
    private SelectModelField notInviteList; // 新增：不邀请列表

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(useNewEggTool = new BooleanModelField("useNewEggTool", "新蛋卡 | 使用", false));
        modelFields.addField(useAccelerateTool = new BooleanModelField("useAccelerateTool", "加速卡 | 使用", false));
        modelFields.addField(useAccelerateToolOptions = new SelectModelField("useAccelerateToolOptions", "加速卡 | 选项", new LinkedHashSet<>(), CustomOption::getUseAccelerateToolOptions));
        modelFields.addField(useBigEaterTool = new BooleanModelField("useBigEaterTool", "加饭卡 | 使用", false));
        modelFields.addField(useSpecialFood = new BooleanModelField("useSpecialFood", "特殊食品 | 使用", false));
        modelFields.addField(useSpecialFoodCountLimit = new IntegerModelField("useSpecialFoodCountLimit", "特殊食品 | " + "使用上限(无限:0)", 0));
        modelFields.addField(rewardFriend = new BooleanModelField("rewardFriend", "打赏好友", false));
        modelFields.addField(recallAnimalType = new ChoiceModelField("recallAnimalType", "召回小鸡", RecallAnimalType.ALWAYS, RecallAnimalType.nickNames));
        modelFields.addField(feedAnimal = new BooleanModelField("feedAnimal", "投喂小鸡", false));
        modelFields.addField(feedFriendAnimal = new BooleanModelField("feedFriendAnimal", "帮喂小鸡 | 开启", true));
        modelFields.addField(feedFriendAnimalList = new SelectAndCountModelField("feedFriendAnimalList", "帮喂小鸡 | " + "好友列表", new LinkedHashMap<>(), AlipayUser::getList, "请填写帮喂次数(每日)"));
        modelFields.addField(hireAnimalType = new ChoiceModelField("hireAnimalType", "雇佣小鸡 | 动作", HireAnimalType.NONE, HireAnimalType.nickNames));
        modelFields.addField(hireAnimalList = new SelectModelField("hireAnimalList", "雇佣小鸡 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(sendBackAnimalWay = new ChoiceModelField("sendBackAnimalWay", "遣返小鸡 | 方式", SendBackAnimalWay.NORMAL, SendBackAnimalWay.nickNames));
        modelFields.addField(sendBackAnimalType = new ChoiceModelField("sendBackAnimalType", "遣返小鸡 | 动作", SendBackAnimalType.NONE, SendBackAnimalType.nickNames));
        modelFields.addField(sendBackAnimalList = new SelectModelField("sendFriendList", "遣返小鸡 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(notifyFriendType = new ChoiceModelField("notifyFriendType", "通知赶鸡 | 动作", NotifyFriendType.NONE, NotifyFriendType.nickNames));
        modelFields.addField(notifyFriendList = new SelectModelField("notifyFriendList", "通知赶鸡 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(ornamentsDressUp = new BooleanModelField("ornamentsDressUp", "装扮焕新 | 开启", false));
        modelFields.addField(ornamentsDressUpList = new SelectModelField("ornamentsDressUpList", "装扮焕新 | 套装列表", new LinkedHashSet<>(), FarmOrnaments::getList));
        modelFields.addField(ornamentsDressUpDays = new IntegerModelField("ornamentsDressUpDays", "装扮焕新 | 焕新频率(天)", 7));
        modelFields.addField(drawMachine = new BooleanModelField("drawMachine", "装扮抽抽乐", false));
        modelFields.addField(donationType = new ChoiceModelField("donationType", "每日捐蛋 | 方式", DonationType.ZERO, DonationType.nickNames));
        modelFields.addField(donationAmount = new IntegerModelField("donationAmount", "每日捐蛋 | 倍数(每项)", 1));
        modelFields.addField(family = new BooleanModelField("family", "亲密家庭 | 开启", false));
        modelFields.addField(familyOptions = new SelectModelField("familyOptions", "亲密家庭 | 选项", new LinkedHashSet<>(), CustomOption::getAntFarmFamilyOptions));
        modelFields.addField(notInviteList = new SelectModelField("notInviteList", "亲密家庭 | 不邀请列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(giftFamilyDrawFragment = new StringModelField("giftFamilyDrawFragment", "家庭 | 扭蛋碎片赠送用户ID(配置目录查看)", ""));
        modelFields.addField(sleepTime = new StringModelField("sleepTime", "小鸡睡觉 | 时间(关闭:-1)", "2001"));
        modelFields.addField(sleepMinutes = new IntegerModelField("sleepMinutes", "小鸡睡觉 | 时长(分钟)", 10 * 59, 1, 10 * 60));
        modelFields.addField(recordFarmGame = new BooleanModelField("recordFarmGame", "小鸡乐园 | 游戏改分(星星球、登山赛、飞行赛、揍小鸡)", false));
        List<String> farmGameTimeList = new ArrayList<>();
        farmGameTimeList.add("2200-2400");
        modelFields.addField(farmGameTime = new ListModelField.ListJoinCommaToStringModelField("farmGameTime", "小鸡乐园 " + "| 游戏时间(范围)", farmGameTimeList));
        modelFields.addField(drawGameCenterAward = new BooleanModelField("drawGameCenterAward", "小鸡乐园 | 开宝箱", false));
        modelFields.addField(kitchen = new BooleanModelField("kitchen", "小鸡厨房", false));
        modelFields.addField(chickenDiary = new BooleanModelField("chickenDiary", "小鸡日记", false));
        modelFields.addField(harvestProduce = new BooleanModelField("harvestProduce", "收取爱心鸡蛋", false));
        modelFields.addField(receiveFarmToolReward = new BooleanModelField("receiveFarmToolReward", "收取道具奖励", false));
        modelFields.addField(receiveFarmTaskAward = new BooleanModelField("receiveFarmTaskAward", "收取饲料奖励", false));
        //modelFields.addField(getFeedType = new ChoiceModelField("getFeedType", "一起拿饲料 | 动作", GetFeedType.NONE, GetFeedType.nickNames));
        //modelFields.addField(getFeedList = new SelectModelField("getFeedList", "一起拿饲料 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(acceptGift = new BooleanModelField("acceptGift", "收麦子", false));
        modelFields.addField(visitFriendList = new SelectAndCountModelField("visitFriendList", "送麦子 | 好友列表", new LinkedHashMap<>(), AlipayUser::getList, "请填写赠送次数(每日)"));
        return modelFields;
    }

    @Override
    public void boot(ClassLoader classLoader) {
        super.boot(classLoader);
        RpcIntervalLimit.addIntervalLimit("com.alipay.antfarm.enterFarm", 2000);
    }

    @Override
    public Boolean check() {
        if (TaskCommon.IS_ENERGY_TIME) {
            Log.farm("任务暂停⏸️蚂蚁庄园:当前为仅收能量时间");
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        try {
            if (enterFarm() == null) {
                return;
            }

            if (rewardFriend.getValue()) {
                rewardFriend();
            }

            if (sendBackAnimalType.getValue() != SendBackAnimalType.NONE) {
                sendBackAnimal();
            }

            if (!AnimalInteractStatus.HOME.name().equals(ownerAnimal.animalInteractStatus)) {
                if ("ORCHARD".equals(ownerAnimal.locationType)) {
                    Log.farm("庄园通知📣[你家的小鸡给拉去除草了！]");
                    JSONObject joRecallAnimal = new JSONObject(AntFarmRpcCall.orchardRecallAnimal(ownerAnimal.animalId, ownerAnimal.currentFarmMasterUserId));
                    int manureCount = joRecallAnimal.getInt("manureCount");
                    Log.farm("召回小鸡📣收获[" + manureCount + "g肥料]");
                }
                else {
                    syncAnimalStatusAtOtherFarm(ownerAnimal.currentFarmId);
                    boolean guest = false;
                    switch (SubAnimalType.valueOf(ownerAnimal.subAnimalType)) {
                        case GUEST:
                            guest = true;
                            Log.record("小鸡到好友家去做客了");
                            break;
                        case NORMAL:
                            Log.record("小鸡太饿，离家出走了");
                            break;
                        case PIRATE:
                            Log.record("小鸡外出探险了");
                            break;
                        case WORK:
                            Log.record("小鸡出去工作啦");
                            break;
                        default:
                            Log.record("小鸡不在庄园" + " " + ownerAnimal.subAnimalType);
                    }

                    boolean hungry = false;
                    String userName = UserIdMap.getMaskName(AntFarmRpcCall.farmId2UserId(ownerAnimal.currentFarmId));
                    switch (AnimalFeedStatus.valueOf(ownerAnimal.animalFeedStatus)) {
                        case HUNGRY:
                            hungry = true;
                            Log.record("小鸡在[" + userName + "]的庄园里挨饿");
                            break;

                        case EATING:
                            Log.record("小鸡在[" + userName + "]的庄园里吃得津津有味");
                            break;
                    }

                    boolean recall = false;
                    switch ((int) recallAnimalType.getValue()) {
                        case RecallAnimalType.ALWAYS:
                            recall = true;
                            break;
                        case RecallAnimalType.WHEN_THIEF:
                            recall = !guest;
                            break;
                        case RecallAnimalType.WHEN_HUNGRY:
                            recall = hungry;
                            break;
                    }
                    if (recall) {
                        recallAnimal(ownerAnimal.animalId, ownerAnimal.currentFarmId, ownerFarmId, userName);
                        syncAnimalStatus(ownerFarmId);
                    }
                }
            }

            if (receiveFarmToolReward.getValue()) {
                listFarmTool();
                receiveToolTaskReward();
            }

            if (recordFarmGame.getValue()) {
                long currentTimeMillis = System.currentTimeMillis();
                for (String time : farmGameTime.getValue()) {
                    if (TimeUtil.checkInTimeRange(currentTimeMillis, time)) {
                        recordFarmGame(GameType.starGame);
                        recordFarmGame(GameType.jumpGame);
                        recordFarmGame(GameType.flyGame);
                        recordFarmGame(GameType.hitGame);
                        break;
                    }
                }
            }

            if (kitchen.getValue()) {
                collectDailyFoodMaterial(ownerUserId);
                collectDailyLimitedFoodMaterial();
                cook(ownerUserId);
            }

            if (chickenDiary.getValue()) {
                queryChickenDiary("");
                queryChickenDiaryList();
            }

            if (useNewEggTool.getValue()) {
                useFarmTool(ownerFarmId, ToolType.NEWEGGTOOL);
                syncAnimalStatus(ownerFarmId);
            }

            if (harvestProduce.getValue() && benevolenceScore >= 1) {
                Log.record("有可收取的爱心鸡蛋");
                harvestProduce(ownerFarmId);
            }

            if (donationType.getValue() != DonationType.ZERO) {
                donation();
            }

            if (receiveFarmTaskAward.getValue()) {
                listFarmTask(TaskStatus.TODO);
                listFarmTask(TaskStatus.FINISHED);
            }

            if (AnimalInteractStatus.HOME.name().equals(ownerAnimal.animalInteractStatus)) {
                if (AnimalFeedStatus.HUNGRY.name().equals(ownerAnimal.animalFeedStatus)) {
                    Log.record("小鸡在挨饿");
                    if (feedAnimal.getValue()) {
                        feedAnimal(ownerFarmId);
                    }
                }
                else if (AnimalFeedStatus.EATING.name().equals(ownerAnimal.animalFeedStatus)) {
                    if (useAccelerateTool.getValue()) {
                        useAccelerateTool();
                        TimeUtil.sleep(1000);
                    }
                    //使用加饭卡
                    if (useBigEaterTool.getValue()) {
                        useFarmTool(ownerFarmId, AntFarm.ToolType.BIG_EATER_TOOL);
                    }
                    if (feedAnimal.getValue()) {
                        autoFeedAnimal();
                        TimeUtil.sleep(1000);
                    }
                }

                checkUnReceiveTaskAward();
            }

            // 小鸡换装
            if (ornamentsDressUp.getValue()) {
                ornamentsDressUp();
            }

            // 到访小鸡送礼
            visitAnimal();

            // 送麦子
            visitFriend();

            // 帮好友喂鸡
            if (feedFriendAnimal.getValue()) {
                feedFriend();
            }

            // 通知好友赶鸡
            if (notifyFriendType.getValue() != NotifyFriendType.NONE) {
                notifyFriend();
            }

            // 抽抽乐
            if (drawMachine.getValue()) {
                drawMachineGroups();

            }

            // 雇佣小鸡
            if (hireAnimalType.getValue() != HireAnimalType.NONE) {
                hireAnimal();
            }
            
            /*  注释掉有问题的代码
             if (getFeedType.getValue() != GetFeedType.NONE) {
                letsGetChickenFeedTogether();
            }*/

            if (family.getValue()) {
                //family();
                AntFarmFamily.run(familyOptions, notInviteList);
            }

            // 开宝箱
            if (drawGameCenterAward.getValue()) {
                drawGameCenterAward();
            }

            // 小鸡睡觉&起床
            animalSleepAndWake();

        }
        catch (Throwable t) {
            Log.i(TAG, "AntFarm.start.run err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void animalSleepAndWake() {
        String sleepTimeStr = sleepTime.getValue();
        if ("-1".equals(sleepTimeStr)) {
            return;
        }
        animalWakeUpNow();
        Calendar animalSleepTimeCalendar = TimeUtil.getTodayCalendarByTimeStr(sleepTimeStr);
        if (animalSleepTimeCalendar == null) {
            return;
        }
        Integer sleepMinutesInt = sleepMinutes.getValue();
        Calendar animalWakeUpTimeCalendar = (Calendar) animalSleepTimeCalendar.clone();
        animalWakeUpTimeCalendar.add(Calendar.MINUTE, sleepMinutesInt);
        long animalSleepTime = animalSleepTimeCalendar.getTimeInMillis();
        long animalWakeUpTime = animalWakeUpTimeCalendar.getTimeInMillis();
        if (animalSleepTime > animalWakeUpTime) {
            Log.record("小鸡睡觉设置有误，请重新设置");
            return;
        }
        Calendar now = TimeUtil.getNow();
        boolean afterSleepTime = now.compareTo(animalSleepTimeCalendar) > 0;
        boolean afterWakeUpTime = now.compareTo(animalWakeUpTimeCalendar) > 0;
        if (afterSleepTime && afterWakeUpTime) {
            // 睡觉时间后
            if (hasSleepToday()) {
                return;
            }
            Log.record("已错过小鸡今日睡觉时间");
            return;
        }
        if (afterSleepTime) {
            // 睡觉时间内
            if (!hasSleepToday()) {
                animalSleepNow();
            }
            animalWakeUpTime(animalWakeUpTime);
            return;
        }
        // 睡觉时间前
        animalWakeUpTimeCalendar.add(Calendar.HOUR_OF_DAY, -24);
        if (now.compareTo(animalWakeUpTimeCalendar) <= 0) {
            animalWakeUpTime(animalWakeUpTimeCalendar.getTimeInMillis());
        }
        animalSleepTime(animalSleepTime);
        animalWakeUpTime(animalWakeUpTime);
    }

    private JSONObject enterFarm() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.enterFarm("", UserIdMap.getCurrentUid()));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return null;
            }
            rewardProductNum = jo.getJSONObject("dynamicGlobalConfig").getString("rewardProductNum");
            JSONObject joFarmVO = jo.getJSONObject("farmVO");
            foodStock = joFarmVO.getInt("foodStock");
            foodStockLimit = joFarmVO.getInt("foodStockLimit");
            harvestBenevolenceScore = joFarmVO.getDouble("harvestBenevolenceScore");
            parseSyncAnimalStatusResponse(joFarmVO.toString());
            ownerUserId = joFarmVO.getJSONObject("masterUserInfoVO").getString("userId");
            ownerGroupId = getFamilyGroupId(ownerUserId);

            if (jo.has("activityData")) {
                JSONObject activityData = jo.optJSONObject("activityData");
                if (activityData.has("springGifts")) {
                    JSONArray springGifts = activityData.optJSONArray("springGifts");
                    if (springGifts != null) {
                        for (int i = 0; i < springGifts.length(); i++) {
                            JSONObject springGift = springGifts.getJSONObject(i);
                            String foodType = springGift.optString("foodType");
                            int giftIndex = springGift.optInt("giftIndex");
                            String foodSubType = springGift.optString("foodSubType");
                            int foodCount = springGift.optInt("foodCount");
                            AntFarmRpcCall.clickForGiftV2(foodType, giftIndex);
                            if (MessageUtil.checkMemo(TAG, jo)) {
                                Log.farm("惊喜礼包🎁[" + foodSubType + "*" + foodCount + "]");
                            }

                        }
                    }

                }

            }

            if (useSpecialFood.getValue()) {
                JSONArray cuisineList = jo.getJSONArray("cuisineList");
                if (AnimalInteractStatus.HOME.name().equals(ownerAnimal.animalInteractStatus) && !AnimalFeedStatus.SLEEPY.name().equals(ownerAnimal.animalFeedStatus) && Status.canUseSpecialFoodToday()) {
                    useFarmFood(cuisineList);
                }
            }

            if (jo.has("lotteryPlusInfo")) {
                drawLotteryPlus(jo.getJSONObject("lotteryPlusInfo"));
            }
            if (acceptGift.getValue() && joFarmVO.getJSONObject("subFarmVO").has("giftRecord") && foodStockLimit - foodStock >= 10) {
                acceptGift();
            }
            return jo;
        }
        catch (Throwable t) {
            Log.i(TAG, "enterFarm err:");
            Log.printStackTrace(TAG, t);
        }
        return null;
    }

    private void autoFeedAnimal() {
        syncAnimalStatus(ownerFarmId);
        if (!AnimalFeedStatus.EATING.name().equals(ownerAnimal.animalFeedStatus)) {
            return;
        }
        double foodHaveEatten = 0d;
        double consumeSpeed = 0d;
        long nowTime = System.currentTimeMillis();
        for (Animal animal : animals) {
            foodHaveEatten += (nowTime - animal.startEatTime) / 1000 * animal.consumeSpeed;
            consumeSpeed += animal.consumeSpeed;
        }
        long nextFeedTime = nowTime + (long) ((foodInTrough - foodHaveEatten) / consumeSpeed) * 1000;
        String taskId = "FA|" + ownerFarmId;
        if (hasChildTask(taskId)) {
            removeChildTask(taskId);
        }
        addChildTask(new ChildModelTask(taskId, "FA", () -> feedAnimal(ownerFarmId), nextFeedTime));
        Log.record("添加蹲点投喂🥣[" + UserIdMap.getCurrentMaskName() + "]在[" + TimeUtil.getCommonDate(nextFeedTime) + "]执行");
    }

    private void animalSleepTime(long animalSleepTime) {
        String sleepTaskId = "AS|" + animalSleepTime;
        if (!hasChildTask(sleepTaskId)) {
            addChildTask(new ChildModelTask(sleepTaskId, "AS", this::animalSleepNow, animalSleepTime));
            Log.record("添加定时睡觉🛌[" + UserIdMap.getCurrentMaskName() + "]在[" + TimeUtil.getCommonDate(animalSleepTime) + "]执行");
        }
    }

    private void animalWakeUpTime(long animalWakeUpTime) {
        String wakeUpTaskId = "AW|" + animalWakeUpTime;
        if (!hasChildTask(wakeUpTaskId)) {
            addChildTask(new ChildModelTask(wakeUpTaskId, "AW", this::animalWakeUpNow, animalWakeUpTime));
            Log.record("添加定时起床🔆[" + UserIdMap.getCurrentMaskName() + "]在[" + TimeUtil.getCommonDate(animalWakeUpTime) + "]执行");
        }
    }

    private Boolean hasSleepToday() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryLoveCabin(ownerUserId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return false;
            }
            //jo = jo.getJSONObject("sleepNotifyInfo");
            jo = MyUtils.antFarmSleepNotifyInfoMaybeNull(jo);//CHANGE BY KT
            if (jo == null) return true;
            return jo.optBoolean("hasSleepToday", false);
        }
        catch (Throwable t) {
            Log.i(TAG, "hasSleepToday err:");
            Log.printStackTrace(t);
        }
        return false;
    }

    private Boolean animalSleepNow() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryLoveCabin(UserIdMap.getCurrentUid()));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return false;
            }
            JSONObject sleepNotifyInfo = jo.getJSONObject("sleepNotifyInfo");
            //if (!sleepNotifyInfo.optBoolean("canSleep", false)) {
            if (!sleepNotifyInfo.optBoolean(MyUtils.NO_SLEEP, false)) {//CHANGE BY KT
                Log.record("小鸡无需睡觉🛌");
                return false;
            }
            if (family.getValue() && !StringUtil.isEmpty(ownerGroupId)) {
                return familySleep(ownerGroupId);
            }
            return animalSleep();
        }
        catch (Throwable t) {
            Log.i(TAG, "animalSleepNow err:");
            Log.printStackTrace(t);
        }
        return false;
    }

    private Boolean animalWakeUpNow() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryLoveCabin(UserIdMap.getCurrentUid()));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return false;
            }
            JSONObject ownAnimal = jo.getJSONObject("ownAnimal");
            JSONObject sleepInfo = ownAnimal.getJSONObject("sleepInfo");
            if (sleepInfo.getInt("countDown") == 0) {
                return false;
            }
            if (sleepInfo.getLong("sleepBeginTime") + TimeUnit.MINUTES.toMillis(sleepMinutes.getValue()) <= System.currentTimeMillis()) {
                if (jo.has("spaceType")) {
                    return familyWakeUp();
                }
                return animalWakeUp();
            }
            else {
                Log.record("小鸡无需起床🔆");
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "animalWakeUpNow err:");
            Log.printStackTrace(t);
        }
        return false;
    }

    private Boolean animalSleep() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.sleep());
            if (MessageUtil.checkMemo(TAG, jo)) {
                Log.farm("小鸡睡觉🛌");
                return true;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "animalSleep err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean animalWakeUp() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.wakeUp());
            if (MessageUtil.checkMemo(TAG, jo)) {
                Log.farm("小鸡起床🔆");
                return true;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "animalWakeUp err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void syncAnimalStatus(String farmId) {
        try {
            String s = AntFarmRpcCall.syncAnimalStatus(farmId);
            parseSyncAnimalStatusResponse(s);
        }
        catch (Throwable t) {
            Log.i(TAG, "syncAnimalStatus err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void syncAnimalStatusAtOtherFarm(String farmId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.enterFarmOld(farmId, ""));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            jo = jo.getJSONObject("farmVO").getJSONObject("subFarmVO");
            JSONArray jaAnimals = jo.getJSONArray("animals");
            for (int i = 0; i < jaAnimals.length(); i++) {
                jo = jaAnimals.getJSONObject(i);
                if (jo.getString("masterFarmId").equals(ownerFarmId)) {
                    Animal newOwnerAnimal = new Animal();
                    JSONObject animal = jaAnimals.getJSONObject(i);
                    newOwnerAnimal.animalId = animal.getString("animalId");
                    newOwnerAnimal.currentFarmId = animal.getString("currentFarmId");
                    newOwnerAnimal.currentFarmMasterUserId = animal.getString("currentFarmMasterUserId");
                    newOwnerAnimal.masterFarmId = ownerFarmId;
                    newOwnerAnimal.animalBuff = animal.getString("animalBuff");
                    newOwnerAnimal.locationType = animal.optString("locationType", "");
                    newOwnerAnimal.subAnimalType = animal.getString("subAnimalType");
                    animal = animal.getJSONObject("animalStatusVO");
                    newOwnerAnimal.animalFeedStatus = animal.getString("animalFeedStatus");
                    newOwnerAnimal.animalInteractStatus = animal.getString("animalInteractStatus");
                    ownerAnimal = newOwnerAnimal;
                    break;
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "syncAnimalStatusAtOtherFarm err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void rewardFriend() {
        try {
            if (rewardList != null) {
                for (RewardFriend rewardFriend : rewardList) {
                    JSONObject jo = new JSONObject(AntFarmRpcCall.rewardFriend(rewardFriend.consistencyKey, rewardFriend.friendId, rewardProductNum, rewardFriend.time));
                    if (MessageUtil.checkMemo(TAG, jo)) {
                        double rewardCount = benevolenceScore - jo.getDouble("farmProduct");
                        benevolenceScore -= rewardCount;
                        Log.farm("打赏好友💰[" + UserIdMap.getMaskName(rewardFriend.friendId) + "]#得" + rewardCount + "颗爱心鸡蛋");
                    }
                }
                rewardList = null;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "rewardFriend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void recallAnimal(String animalId, String currentFarmId, String masterFarmId, String user) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.recallAnimal(animalId, currentFarmId, masterFarmId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            double foodHaveStolen = jo.getDouble("foodHaveStolen");
            Log.farm("召回小鸡📣偷吃[" + user + "]饲料" + foodHaveStolen + "g");
            // 这里不需要加
            // add2FoodStock((int)foodHaveStolen);
        }
        catch (Throwable t) {
            Log.i(TAG, "recallAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void sendBackAnimal() {
        if (animals == null) {
            return;
        }
        try {
            for (Animal animal : animals) {
                if (AnimalInteractStatus.STEALING.name().equals(animal.animalInteractStatus) && !SubAnimalType.GUEST.name().equals(animal.subAnimalType) && !SubAnimalType.WORK.name().equals(animal.subAnimalType)) {
                    // 赶鸡
                    String user = AntFarmRpcCall.farmId2UserId(animal.masterFarmId);
                    boolean isSendBackAnimal = sendBackAnimalList.getValue().contains(user);
                    if (sendBackAnimalType.getValue() != SendBackAnimalType.BACK) {
                        isSendBackAnimal = !isSendBackAnimal;
                    }
                    if (!isSendBackAnimal) {
                        continue;
                    }
                    int sendTypeInt = sendBackAnimalWay.getValue();
                    user = UserIdMap.getMaskName(user);
                    JSONObject jo = new JSONObject(AntFarmRpcCall.sendBackAnimal(SendBackAnimalWay.nickNames[sendTypeInt], animal.animalId, animal.currentFarmId, animal.masterFarmId));
                    if (MessageUtil.checkMemo(TAG, jo)) {
                        String s;
                        if (sendTypeInt == SendBackAnimalWay.HIT) {
                            if (jo.has("hitLossFood")) {
                                s = "胖揍小鸡🤺[" + user + "]，掉落[" + jo.getInt("hitLossFood") + "g]";
                                if (jo.has("finalFoodStorage")) {
                                    foodStock = jo.getInt("finalFoodStorage");
                                }
                            }
                            else {
                                s = "[" + user + "]的小鸡躲开了攻击";
                            }
                        }
                        else {
                            s = "驱赶小鸡🧶[" + user + "]";
                        }
                        Log.farm(s);
                    }
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "sendBackAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void receiveToolTaskReward() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.listToolTaskDetails());
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            JSONArray jaList = jo.getJSONArray("list");
            for (int i = 0; i < jaList.length(); i++) {
                JSONObject joItem = jaList.getJSONObject(i);
                if (!TaskStatus.FINISHED.name().equals(joItem.optString("taskStatus"))) {
                    continue;
                }
                JSONObject bizInfo = new JSONObject(joItem.getString("bizInfo"));
                String awardType = bizInfo.getString("awardType");
                ToolType toolType = ToolType.valueOf(awardType);
                boolean isFull = false;
                for (FarmTool farmTool : farmTools) {
                    if (farmTool.toolType == toolType) {
                        if (farmTool.toolCount == farmTool.toolHoldLimit) {
                            isFull = true;
                        }
                        break;
                    }
                }
                if (isFull) {
                    if (toolType.equals(ToolType.NEWEGGTOOL)) {
                        useFarmTool(ownerFarmId, ToolType.NEWEGGTOOL);
                    }
                    else {
                        Log.record("领取道具[" + toolType.nickName() + "]#已满，暂不领取");
                        continue;
                    }
                }
                int awardCount = bizInfo.getInt("awardCount");
                String taskType = joItem.getString("taskType");
                String taskTitle = bizInfo.getString("taskTitle");
                jo = new JSONObject(AntFarmRpcCall.receiveToolTaskReward(awardType, awardCount, taskType));
                if (MessageUtil.checkMemo(TAG, jo)) {
                    Log.farm("领取道具🎖️[" + taskTitle + "-" + toolType.nickName() + "]#" + awardCount + "张");
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "receiveToolTaskReward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void harvestProduce(String farmId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.harvestProduce(farmId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            double harvest = jo.getDouble("harvestBenevolenceScore");
            harvestBenevolenceScore = jo.getDouble("finalBenevolenceScore");
            Log.farm("收取鸡蛋🥚[" + harvest + "颗]#剩余" + harvestBenevolenceScore + "颗");
        }
        catch (Throwable t) {
            Log.i(TAG, "harvestProduce err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /* 捐赠爱心鸡蛋 */
    private void donation() {
        if (!canDonationToday()) {
            return;
        }
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.listActivityInfo());
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            JSONArray activityInfos = jo.getJSONArray("activityInfos");
            for (int i = 0; i < activityInfos.length(); i++) {
                jo = activityInfos.getJSONObject(i);
                int donationTotal = jo.getInt("donationTotal");
                int donationLimit = jo.getInt("donationLimit");

                int donationNum = Math.min(donationAmount.getValue(), donationLimit - donationTotal);
                if (donationNum == 0) {
                    continue;
                }
                String activityId = jo.getString("activityId");
                String projectName = jo.getString("projectName");
                String projectId = jo.getString("projectId");
                int projectDonationNum = getProjectDonationNum(projectId);
                donationNum = Math.min(donationNum, donationAmount.getValue() - projectDonationNum % donationAmount.getValue());
                boolean isDonation;
                if (donationNum == donationAmount.getValue()) {
                    isDonation = donation(activityId, projectName, donationNum, 1);
                }
                else {
                    isDonation = donation(activityId, projectName, 1, donationNum);
                }
                if (isDonation && donationType.getValue() != DonationType.ALL) {
                    return;
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "donation err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean donation(String activityId, String activityName, int donationAmount, int count) {
        boolean isDonation = false;
        for (int i = 0; i < count; i++) {
            if (!donation(activityId, activityName, donationAmount)) {
                break;
            }
            isDonation = true;
            TimeUtil.sleep(1000L);
        }
        return isDonation;
    }

    private Boolean donation(String activityId, String activityName, int donationAmount) {
        if (harvestBenevolenceScore < donationAmount) {
            return false;
        }
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.donation(activityId, donationAmount));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return false;
            }
            jo = jo.getJSONObject("donation");
            harvestBenevolenceScore = jo.getDouble("harvestBenevolenceScore");
            int donationTimesStat = jo.getInt("donationTimesStat");
            Log.farm("公益捐赠❤️[捐爱心蛋:" + activityName + "]捐赠" + donationAmount + "颗爱心蛋#累计捐赠" + donationTimesStat + "次");
            return true;
        }
        catch (Throwable t) {
            Log.i(TAG, "donation err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private int getProjectDonationNum(String projectId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.getProjectInfo(projectId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return 0;
            }
            return jo.optInt("userProjectDonationNum");
        }
        catch (Throwable t) {
            Log.i(TAG, "getProjectDonationNum err:");
            Log.printStackTrace(TAG, t);
        }
        return 0;
    }

    private Boolean canDonationToday() {
        if (Status.hasFlagToday("farm::donation")) {
            return false;
        }
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.getCharityAccount(ownerUserId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return false;
            }
            JSONArray charityRecords = jo.getJSONArray("charityRecords");
            if (charityRecords.length() == 0) {
                return true;
            }
            jo = charityRecords.getJSONObject(0);
            long charityTime = jo.optLong("charityTime", System.currentTimeMillis());
            if (TimeUtil.isLessThanNowOfDays(charityTime)) {
                return true;
            }
            Status.flagToday("farm::donation");
        }
        catch (Throwable t) {
            Log.i(TAG, "canDonationToday err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void recordFarmGame(GameType gameType) {
        try {
            do {
                try {
                    JSONObject jo = new JSONObject(AntFarmRpcCall.initFarmGame(gameType.name()));
                    if (!MessageUtil.checkMemo(TAG, jo)) {
                        return;
                    }
                    if (jo.getJSONObject("gameAward").getBoolean("level3Get")) {
                        return;
                    }
                    if (jo.optInt("remainingGameCount", 1) == 0) {
                        return;
                    }
                    jo = new JSONObject(AntFarmRpcCall.recordFarmGame(gameType.name()));
                    if (!MessageUtil.checkMemo(TAG, jo)) {
                        return;
                    }
                    JSONArray awardInfos = jo.getJSONArray("awardInfos");
                    StringBuilder award = new StringBuilder();
                    for (int i = 0; i < awardInfos.length(); i++) {
                        JSONObject awardInfo = awardInfos.getJSONObject(i);
                        award.append(awardInfo.getString("awardName")).append("*").append(awardInfo.getInt("awardCount"));
                    }
                    if (jo.has("receiveFoodCount")) {
                        award.append(";肥料*").append(jo.getString("receiveFoodCount"));
                    }
                    Log.farm("小鸡乐园🎮游玩[" + gameType.gameName() + "]#获得[" + award + "]");
                    if (jo.optInt("remainingGameCount", 0) > 0) {
                        continue;
                    }
                    break;
                }
                finally {
                    TimeUtil.sleep(2000);
                }
            }
            while (true);
        }
        catch (Throwable t) {
            Log.i(TAG, "recordFarmGame err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void listFarmTask(TaskStatus Mode) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.listFarmTask());
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            JSONObject signList = jo.getJSONObject("signList");
            if (sign(signList)) {
                TimeUtil.sleep(1000);
            }
            JSONArray ja = jo.getJSONArray("farmTaskList");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                TaskStatus taskStatus = TaskStatus.valueOf(jo.getString("taskStatus"));
                String title = jo.getString("title");

                //屏蔽任务
                if (title.equals("到店付款") || title.equals("线上支付") || title.equals("逛闪购外卖1元起吃") || title.equals("用花呗完成一笔支付")) {
                    continue;
                }
                if (taskStatus == TaskStatus.RECEIVED || taskStatus != Mode) {
                    continue;
                }
                if (taskStatus == TaskStatus.TODO && !doFarmTask(jo)) {
                    continue;
                }
                if (taskStatus == TaskStatus.FINISHED && !receiveFarmTaskAward(jo)) {
                    continue;
                }
                TimeUtil.sleep(1000);
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "listFarmTask err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean sign(JSONObject SignList) {
        if (Status.hasFlagToday("farm::sign")) {
            return false;
        }
        boolean signed = false;
        try {
            String currentSignKey = SignList.getString("currentSignKey");
            JSONArray signList = SignList.getJSONArray("signList");
            for (int i = 0; i < signList.length(); i++) {
                JSONObject jo = signList.getJSONObject(i);
                if (!currentSignKey.equals(jo.getString("signKey"))) {
                    continue;
                }
                if (jo.optBoolean("signed")) {
                    Log.record("庄园今日已签到");
                    signed = true;
                    return false;
                }
                int awardCount = jo.getInt("awardCount");
                if (awardCount + foodStock > foodStockLimit) {
                    return false;
                }
                int currentContinuousCount = jo.getInt("currentContinuousCount");
                jo = new JSONObject(AntFarmRpcCall.sign());
                if (MessageUtil.checkMemo(TAG, jo)) {
                    foodStock = jo.getInt("foodStock");
                    Log.farm("饲料任务📅签到[坚持" + currentContinuousCount + "天]#获得[" + awardCount + "g饲料]");
                    signed = true;
                    return true;
                }
                return false;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "sign err:");
            Log.printStackTrace(TAG, t);
        }
        finally {
            if (signed) {
                Status.flagToday("farm::sign");
            }
        }
        return false;
    }

    private Boolean doVideoTask() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryTabVideoUrl());
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return false;
            }
            String videoUrl = jo.getString("videoUrl");
            String contentId = videoUrl.substring(videoUrl.indexOf("&contentId=") + 1, videoUrl.indexOf("&refer"));
            jo = new JSONObject(AntFarmRpcCall.videoDeliverModule(contentId));
            if (jo.optBoolean("success")) {
                TimeUtil.sleep(15100);
                jo = new JSONObject(AntFarmRpcCall.videoTrigger(contentId));
                if (jo.optBoolean("success")) {
                    return true;
                }
                else {
                    Log.record(jo.getString("resultMsg"));
                    Log.i(jo.toString());
                }
            }
            else {
                Log.record(jo.getString("resultMsg"));
                Log.i(jo.toString());
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "doVideoTask err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean doAnswerTask() {
        try {
            JSONObject jo = new JSONObject(DadaDailyRpcCall.home("100"));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return false;
            }
            JSONObject question = jo.getJSONObject("question");
            long questionId = question.getLong("questionId");
            JSONArray labels = question.getJSONArray("label");
            String answer = AnswerAI.getAnswer(question.getString("title"), JsonUtil.jsonArrayToList(labels));
            if (answer == null || answer.isEmpty()) {
                answer = labels.getString(0);
            }
            jo = new JSONObject(DadaDailyRpcCall.submit("100", answer, questionId));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return false;
            }
            JSONObject extInfo = jo.getJSONObject("extInfo");
            boolean correct = jo.getBoolean("correct");
            String award = extInfo.getString("award");
            Log.record("庄园答题📝回答" + (correct ? "正确" : "错误") + "#获得[" + award + "g饲料]");
            JSONArray operationConfigList = jo.getJSONArray("operationConfigList");
            savePreviewQuestion(operationConfigList);
            return true;
        }
        catch (Throwable t) {
            Log.i(TAG, "doAnswerTask err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void savePreviewQuestion(JSONArray operationConfigList) {
        try {
            for (int i = 0; i < operationConfigList.length(); i++) {
                JSONObject jo = operationConfigList.getJSONObject(i);
                String type = jo.getString("type");
                if (Objects.equals(type, "PREVIEW_QUESTION")) {
                    String question = jo.getString("title");
                    JSONArray ja = new JSONArray(jo.getString("actionTitle"));
                    for (int j = 0; j < ja.length(); j++) {
                        jo = ja.getJSONObject(j);
                        if (jo.getBoolean("correct")) {
                            TokenConfig.saveAnswer(question, jo.getString("title"));
                        }
                    }
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "saveAnswerList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 在 doFarmTask 方法中，修复 libraryDoFarmTask 的调用
    private Boolean doFarmTask(JSONObject task) {
        boolean isDoTask = false;
        try {
            String title = task.getString("title");
            String bizKey = task.getString("bizKey");

            //屏蔽任务
            //if(bizKey.contains("HEART_DONAT")||bizKey.equals("BAIDUJS_202512")||bizKey.equals("BABAFARM_TB")||bizKey.equals("TAO_GOLDEN_V2"))
            if (bizKey.contains("HEART_DONAT") || bizKey.equals("BAIDUJS_202512") || bizKey.equals("BABAFARM_TB")) {
                return false;
            }
            if (Objects.equals(title, "庄园小视频")) {
                isDoTask = doVideoTask();
            }
            else if (Objects.equals(title, "庄园小课堂")) {
                isDoTask = doAnswerTask();
            }
            else {
                // 检查library是否可用
                try {
                    isDoTask = LibraryUtil.doFarmTask(task);
                    return isDoTask;//防止重复打印 //CHANGE BY KT
                }
                catch (UnsatisfiedLinkError e) {
                    Log.record("Native库不可用，跳过任务: " + title);
                    isDoTask = false;

                }
            }

            if (isDoTask) {
                Log.farm("饲料任务🧾完成[" + title + "]");
            }
            else {
                Log.record("任务执行失败或跳过: " + title);
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "doFarmTask err:");
            Log.printStackTrace(TAG, t);
        }
        return isDoTask;
    }

    private Boolean receiveFarmTaskAward(JSONObject task) {
        try {
            String taskId = task.getString("taskId");
            String awardType = task.getString("awardType");
            int awardCount = task.getInt("awardCount");
            if (Objects.equals(awardType, "ALLPURPOSE")) {
                if (awardCount + foodStock > foodStockLimit) {
                    unReceiveTaskAward++;
                    // Log.record("领取" + awardCount + "克饲料后将超过[" + foodStockLimit + "克]上限，终止领取");
                    return false;
                }
            }
            JSONObject jo = new JSONObject(AntFarmRpcCall.receiveFarmTaskAward(taskId));
            //方法: com.alipay.antfarm.receiveFarmTaskAward
            //参数: [{"requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","taskId":"SLEEP","version":"1.8.2302070202.46"}]
            //数据: {"ariverRpcTraceId":"0b44758817545051557304311e509c","memo":"饲料槽已满","resultCode":"331","success":false}
            if (MyUtils.closeErrorFunction() && "饲料槽已满".equals(jo.optString("memo"))) {//CHANGE BY KT
                return true;//饲料槽已满则不再循环执行
            }
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return false;
            }
            if (awardType.equals("ALLPURPOSE")) {
                add2FoodStock(awardCount);
                String title = task.getString("title");
                Log.farm("饲料任务🎖️领取[" + title + "]奖励#获得[" + awardCount + "g饲料]");
                return true;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "receiveFarmTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void checkUnReceiveTaskAward() {
        if (unReceiveTaskAward > 0) {
            Log.record("还有待领取的饲料");
            unReceiveTaskAward = 0;
            listFarmTask(TaskStatus.FINISHED);
        }
    }

    private void feedAnimal(String farmId) {
        try {
            syncAnimalStatus(ownerFarmId);
            if (foodStock < 180) {
                Log.record("剩余饲料不足以投喂小鸡");
                return;
            }
            JSONObject jo = new JSONObject(AntFarmRpcCall.feedAnimal(farmId));
            if (MessageUtil.checkMemo(TAG, jo)) {
                int feedFood = foodStock - jo.getInt("foodStock");
                add2FoodStock(-feedFood);
                Log.farm("投喂小鸡🥣消耗[" + feedFood + "g]#剩余[" + foodStock + "g饲料]");
                if (useAccelerateTool.getValue()) {
                    TimeUtil.sleep(1000);
                    useAccelerateTool();
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "feedAnimal err:");
            Log.printStackTrace(TAG, t);
        }
        finally {
            long updateTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
            String taskId = "UPDATE|FA|" + farmId;
            addChildTask(new ChildModelTask(taskId, "UPDATE", this::autoFeedAnimal, updateTime));
        }
    }

    private void listFarmTool() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.listFarmTool());
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            JSONArray jaToolList = jo.getJSONArray("toolList");
            farmTools = new FarmTool[jaToolList.length()];
            for (int i = 0; i < jaToolList.length(); i++) {
                jo = jaToolList.getJSONObject(i);
                farmTools[i] = new FarmTool();
                farmTools[i].toolId = jo.optString("toolId", "");
                farmTools[i].toolType = ToolType.valueOf(jo.getString("toolType"));
                farmTools[i].toolCount = jo.getInt("toolCount");
                farmTools[i].toolHoldLimit = jo.optInt("toolHoldLimit", 20);
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "listFarmTool err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void useAccelerateTool() {
        if (!Status.canUseAccelerateToolToday()) {
            return;
        }
        syncAnimalStatus(ownerFarmId);
        if ((!useAccelerateToolOptions.getValue().contains("useAccelerateToolContinue") && AnimalBuff.ACCELERATING.name().equals(ownerAnimal.animalBuff)) || (useAccelerateToolOptions.getValue().contains("useAccelerateToolWhenMaxEmotion") && finalScore != 100)) {
            return;
        }
        double consumeSpeed = 0d;
        double foodHaveEatten = 0d;
        long nowTime = System.currentTimeMillis() / 1000;
        for (Animal animal : animals) {
            if (animal.masterFarmId.equals(ownerFarmId)) {
                consumeSpeed = animal.consumeSpeed;
            }
            foodHaveEatten += animal.consumeSpeed * (nowTime - animal.startEatTime / 1000);
        }
        // consumeSpeed: g/s
        // AccelerateTool: -1h = -60m = -3600s
        while (foodInTrough - foodHaveEatten >= consumeSpeed * 3600 && useFarmTool(ownerFarmId, ToolType.ACCELERATETOOL)) {
            TimeUtil.sleep(1000);
            foodHaveEatten += consumeSpeed * 3600;
            Status.useAccelerateToolToday();
            if (!Status.canUseAccelerateToolToday()) {
                break;
            }
            if (!useAccelerateToolOptions.getValue().contains("useAccelerateToolContinue")) {
                break;
            }
        }
    }

    private Boolean useFarmTool(String targetFarmId, ToolType toolType) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.listFarmTool());
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return false;
            }
            JSONArray jaToolList = jo.getJSONArray("toolList");
            for (int i = 0; i < jaToolList.length(); i++) {
                jo = jaToolList.getJSONObject(i);
                if (!toolType.name().equals(jo.getString("toolType"))) {
                    continue;
                }
                int toolCount = jo.optInt("toolCount");
                if (toolCount == 0) {
                    return false;
                }
                String toolId = jo.optString("toolId");
                jo = new JSONObject(AntFarmRpcCall.useFarmTool(targetFarmId, toolId, toolType.name()));
                if (MessageUtil.checkMemo(TAG, jo)) {
                    Log.farm("使用道具🎭[" + toolType.nickName() + "]#剩余" + (toolCount - 1) + "张");
                    return true;
                }
                else if (Objects.equals("3D16", jo.getString("resultCode"))) {
                    Status.flagToday("farm::useFarmToolLimit::" + toolType);
                }
                break;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "useFarmTool err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void feedFriend() {
        try {
            Map<String, Integer> feedFriendAnimalMap = feedFriendAnimalList.getValue();
            for (Map.Entry<String, Integer> entry : feedFriendAnimalMap.entrySet()) {
                String userId = entry.getKey();
                if (userId.equals(UserIdMap.getCurrentUid())) {
                    continue;
                }
                if (!Status.canFeedFriendToday(userId, entry.getValue())) {
                    continue;
                }
                JSONObject jo = new JSONObject(AntFarmRpcCall.enterFarm("", userId));
                if (!MessageUtil.checkMemo(TAG, jo)) {
                    continue;
                }
                jo = jo.getJSONObject("farmVO").getJSONObject("subFarmVO");
                //String friendFarmId = jo.getString("farmId");
                String friendFarmId = jo.optString("farmId");//CHANGE BY KT
                //JSONArray jaAnimals = jo.getJSONArray("animals");
                JSONArray jaAnimals = MyUtils.antFarmAnimalsMaybeNull(jo);
                if (jaAnimals != null) for (int j = 0; j < jaAnimals.length(); j++) {
                    jo = jaAnimals.getJSONObject(j);
                    String masterFarmId = jo.getString("masterFarmId");
                    if (masterFarmId.equals(friendFarmId)) {
                        jo = jo.getJSONObject("animalStatusVO");
                        if (AnimalInteractStatus.HOME.name().equals(jo.getString("animalInteractStatus")) && AnimalFeedStatus.HUNGRY.name().equals(jo.getString("animalFeedStatus"))) {
                            feedFriendAnimal(friendFarmId, UserIdMap.getMaskName(userId));
                        }
                        break;
                    }
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "feedFriend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void feedFriendAnimal(String friendFarmId) {
        try {
            String userId = AntFarmRpcCall.farmId2UserId(friendFarmId);
            String maskName = UserIdMap.getMaskName(userId);
            Log.record("[" + maskName + "]的小鸡在挨饿");
            if (foodStock < 180) {
                Log.record("喂鸡饲料不足");
                checkUnReceiveTaskAward();
                if (foodStock < 180) {
                    return;
                }
            }
            String groupId = null;
            if (family.getValue()) {
                groupId = getFamilyGroupId(userId);
                if (StringUtil.isEmpty(groupId) || !Objects.equals(ownerGroupId, groupId)) {
                    groupId = null;
                }
            }
            if (feedFriendAnimal(friendFarmId, groupId)) {
                String s = StringUtil.isEmpty(groupId) ? "帮喂小鸡🥣帮喂好友" : "亲密家庭🏠帮喂成员";
                s = s + "[" + maskName + "]" + "的小鸡#剩余[" + foodStock + "g饲料]";
                Log.farm(s);
                Status.feedFriendToday(AntFarmRpcCall.farmId2UserId(friendFarmId));
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "feedFriendAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean feedFriendAnimal(String friendFarmId, String groupId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.feedFriendAnimal(friendFarmId, groupId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                if (Objects.equals("391", jo.optString("resultCode"))) {
                    Status.flagToday("farm::feedFriendAnimalLimit");
                }
                return false;
            }
            int feedFood = foodStock - jo.getInt("foodStock");
            if (feedFood > 0) {
                add2FoodStock(-feedFood);
                return true;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "feedFriendAnimal err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void notifyFriend() {
        if (foodStock >= foodStockLimit) {
            return;
        }
        try {
            boolean hasNext = false;
            int pageStartSum = 0;
            String s;
            JSONObject jo;
            do {
                s = AntFarmRpcCall.rankingList(pageStartSum);
                jo = new JSONObject(s);
                if (!MessageUtil.checkMemo(TAG, jo)) {
                    break;
                }
                hasNext = jo.getBoolean("hasNext");
                JSONArray jaRankingList = jo.getJSONArray("rankingList");
                pageStartSum += jaRankingList.length();
                for (int i = 0; i < jaRankingList.length(); i++) {
                    jo = jaRankingList.getJSONObject(i);
                    String userId = jo.getString("userId");
                    String userName = UserIdMap.getMaskName(userId);
                    boolean isNotifyFriend = notifyFriendList.getValue().contains(userId);
                    if (notifyFriendType.getValue() != NotifyFriendType.NOTIFY) {
                        isNotifyFriend = !isNotifyFriend;
                    }
                    if (!isNotifyFriend || userId.equals(UserIdMap.getCurrentUid())) {
                        continue;
                    }
                    boolean starve = jo.has("actionType") && "starve_action".equals(jo.getString("actionType"));
                    if (jo.getBoolean("stealingAnimal") && !starve) {
                        jo = new JSONObject(AntFarmRpcCall.enterFarm("", userId));
                        if (!MessageUtil.checkMemo(TAG, jo)) {
                            continue;
                        }
                        jo = jo.getJSONObject("farmVO").getJSONObject("subFarmVO");
                        String friendFarmId = jo.getString("farmId");
                        JSONArray jaAnimals = jo.getJSONArray("animals");
                        for (int j = 0; j < jaAnimals.length(); j++) {
                            jo = jaAnimals.getJSONObject(j);
                            String animalId = jo.getString("animalId");
                            String masterFarmId = jo.getString("masterFarmId");
                            if (!masterFarmId.equals(friendFarmId) && !masterFarmId.equals(ownerFarmId)) {
                                jo = jo.getJSONObject("animalStatusVO");
                                if (notifyFriend(jo, friendFarmId, animalId, userName)) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            while (hasNext);
            Log.record("饲料剩余[" + foodStock + "g]");
        }
        catch (Throwable t) {
            Log.i(TAG, "notifyFriend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean notifyFriend(JSONObject joAnimalStatusVO, String friendFarmId, String animalId, String user) {
        try {
            if (AnimalInteractStatus.STEALING.name().equals(joAnimalStatusVO.getString("animalInteractStatus")) && AnimalFeedStatus.EATING.name().equals(joAnimalStatusVO.getString("animalFeedStatus"))) {
                JSONObject jo = new JSONObject(AntFarmRpcCall.notifyFriend(animalId, friendFarmId));
                if (!MessageUtil.checkMemo(TAG, jo)) {
                    return false;
                }
                int rewardCount = (int) jo.getDouble("rewardCount");
                if (jo.getBoolean("refreshFoodStock")) {
                    foodStock = (int) jo.getDouble("finalFoodStock");
                }
                else {
                    add2FoodStock(rewardCount);
                }
                Log.farm("通知赶鸡📧提醒[" + user + "]被偷吃#获得[" + rewardCount + "g饲料]");
                return true;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "notifyFriend err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void parseSyncAnimalStatusResponse(String resp) {
        try {
            JSONObject jo = new JSONObject(resp);
            if (!jo.has("subFarmVO")) {
                return;
            }
            if (jo.has("emotionInfo")) {
                finalScore = jo.getJSONObject("emotionInfo").getDouble("finalScore");
            }
            JSONObject subFarmVO = jo.getJSONObject("subFarmVO");
            if (subFarmVO.has("foodStock")) {
                foodStock = subFarmVO.getInt("foodStock");
            }
            if (subFarmVO.has("foodInTrough")) {
                foodInTrough = subFarmVO.getInt("foodInTrough");
            }
            if (subFarmVO.has("manureVO")) {
                JSONArray manurePotList = subFarmVO.getJSONObject("manureVO").getJSONArray("manurePotList");
                for (int i = 0; i < manurePotList.length(); i++) {
                    JSONObject manurePot = manurePotList.getJSONObject(i);
                    if (manurePot.getInt("manurePotNum") >= 100) {
                        JSONObject joManurePot = new JSONObject(AntFarmRpcCall.collectManurePot(manurePot.getString("manurePotNO")));
                        if (joManurePot.optBoolean("success")) {
                            int collectManurePotNum = joManurePot.getInt("collectManurePotNum");
                            Log.farm("打扫鸡屎🧹获得[" + collectManurePotNum + "g肥料]");
                        }
                    }
                }
            }
            ownerFarmId = subFarmVO.getString("farmId");
            JSONObject farmProduce = subFarmVO.getJSONObject("farmProduce");
            benevolenceScore = farmProduce.getDouble("benevolenceScore");
            if (subFarmVO.has("rewardList")) {
                JSONArray jaRewardList = subFarmVO.getJSONArray("rewardList");
                if (jaRewardList.length() > 0) {
                    rewardList = new RewardFriend[jaRewardList.length()];
                    for (int i = 0; i < rewardList.length; i++) {
                        JSONObject joRewardList = jaRewardList.getJSONObject(i);
                        if (rewardList[i] == null) {
                            rewardList[i] = new RewardFriend();
                        }
                        rewardList[i].consistencyKey = joRewardList.getString("consistencyKey");
                        rewardList[i].friendId = joRewardList.getString("friendId");
                        rewardList[i].time = joRewardList.getString("time");
                    }
                }
            }
            JSONArray jaAnimals = subFarmVO.getJSONArray("animals");
            animals = new Animal[jaAnimals.length()];
            for (int i = 0; i < animals.length; i++) {
                Animal animal = new Animal();
                JSONObject animalJsonObject = jaAnimals.getJSONObject(i);
                animal.animalId = animalJsonObject.getString("animalId");
                animal.currentFarmId = animalJsonObject.getString("currentFarmId");
                animal.masterFarmId = animalJsonObject.getString("masterFarmId");
                animal.animalBuff = animalJsonObject.getString("animalBuff");
                animal.subAnimalType = animalJsonObject.getString("subAnimalType");
                animal.currentFarmMasterUserId = animalJsonObject.getString("currentFarmMasterUserId");
                animal.locationType = animalJsonObject.optString("locationType", "");
                JSONObject animalStatusVO = animalJsonObject.getJSONObject("animalStatusVO");
                animal.animalFeedStatus = animalStatusVO.getString("animalFeedStatus");
                animal.animalInteractStatus = animalStatusVO.getString("animalInteractStatus");
                animal.animalInteractStatus = animalStatusVO.getString("animalInteractStatus");
                animal.startEatTime = animalJsonObject.optLong("startEatTime");
                animal.beHiredEndTime = animalJsonObject.optLong("beHiredEndTime");
                animal.consumeSpeed = animalJsonObject.optDouble("consumeSpeed");
                animal.foodHaveEatten = animalJsonObject.optDouble("foodHaveEatten");
                if (animal.masterFarmId.equals(ownerFarmId)) {
                    ownerAnimal = animal;
                }
                animals[i] = animal;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "parseSyncAnimalStatusResponse err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void add2FoodStock(int i) {
        foodStock += i;
        if (foodStock > foodStockLimit) {
            foodStock = foodStockLimit;
        }
        if (foodStock < 0) {
            foodStock = 0;
        }
    }

    private void collectDailyFoodMaterial(String userId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.enterKitchen(userId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            boolean canCollectDailyFoodMaterial = jo.getBoolean("canCollectDailyFoodMaterial");
            int dailyFoodMaterialAmount = jo.getInt("dailyFoodMaterialAmount");
            int garbageAmount = jo.optInt("garbageAmount", 0);
            if (jo.has("orchardFoodMaterialStatus")) {
                JSONObject orchardFoodMaterialStatus = jo.getJSONObject("orchardFoodMaterialStatus");
                if ("FINISHED".equals(orchardFoodMaterialStatus.optString("foodStatus"))) {
                    jo = new JSONObject(AntFarmRpcCall.farmFoodMaterialCollect());
                    if ("100".equals(jo.getString("resultCode"))) {
                        Log.farm("小鸡厨房👨🏻‍🍳农场食材#领取[" + jo.getInt("foodMaterialAddCount") + "g食材]");
                    }
                    else {
                        Log.i(TAG, jo.toString());
                    }
                }
            }
            if (canCollectDailyFoodMaterial) {
                jo = new JSONObject(AntFarmRpcCall.collectDailyFoodMaterial(dailyFoodMaterialAmount));
                if (MessageUtil.checkMemo(TAG, jo)) {
                    Log.farm("小鸡厨房👨🏻‍🍳今日食材#领取[" + dailyFoodMaterialAmount + "g食材]");
                }
            }
            if (garbageAmount > 0) {
                jo = new JSONObject(AntFarmRpcCall.collectKitchenGarbage());
                if (MessageUtil.checkMemo(TAG, jo)) {
                    Log.farm("小鸡厨房👨🏻‍🍳收集厨余#获得[" + jo.getInt("recievedKitchenGarbageAmount") + "g肥料]");
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "collectDailyFoodMaterial err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void collectDailyLimitedFoodMaterial() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryFoodMaterialPack());
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            boolean canCollectDailyLimitedFoodMaterial = jo.getBoolean("canCollectDailyLimitedFoodMaterial");
            if (canCollectDailyLimitedFoodMaterial) {
                int dailyLimitedFoodMaterialAmount = jo.getInt("dailyLimitedFoodMaterialAmount");
                jo = new JSONObject(AntFarmRpcCall.collectDailyLimitedFoodMaterial(dailyLimitedFoodMaterialAmount));
                if (MessageUtil.checkMemo(TAG, jo)) {
                    Log.farm("小鸡厨房👨🏻‍🍳领取[爱心食材店食材]#" + dailyLimitedFoodMaterialAmount + "g");
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "collectDailyLimitedFoodMaterial err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void cook(String userId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.enterKitchen(userId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            int cookTimesAllowed = jo.getInt("cookTimesAllowed");
            if (cookTimesAllowed > 0) {
                for (int i = 0; i < cookTimesAllowed; i++) {
                    jo = new JSONObject(AntFarmRpcCall.cook(userId, "VILLA"));
                    if (MessageUtil.checkMemo(TAG, jo)) {
                        JSONObject cuisineVO = jo.getJSONObject("cuisineVO");
                        Log.farm("小鸡厨房👨🏻‍🍳制作[" + cuisineVO.getString("name") + "]");
                    }
                    TimeUtil.sleep(RandomUtil.delay());
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "cook err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private List<JSONObject> getSortedCuisineList(JSONArray cuisineList) {
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < cuisineList.length(); i++) {
            list.add(cuisineList.optJSONObject(i));
        }
        Collections.sort(list, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObject1, JSONObject jsonObject2) {
                int count1 = jsonObject1.optInt("count");
                int count2 = jsonObject2.optInt("count");
                return count2 - count1;
            }
        });
        return list;
    }

    private void useFarmFood(JSONArray cuisineList) {
        try {
            List<JSONObject> list = getSortedCuisineList(cuisineList);
            for (int i = 0; i < list.size(); i++) {
                if (!useFarmFood(list.get(i))) {
                    return;
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "useFarmFood err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean useFarmFood(JSONObject cuisine) {
        if (!Status.canUseSpecialFoodToday()) {
            return false;
        }
        try {
            String cookbookId = cuisine.getString("cookbookId");
            String cuisineId = cuisine.getString("cuisineId");
            String name = cuisine.getString("name");
            int count = cuisine.getInt("count");
            for (int j = 0; j < count; j++) {
                JSONObject jo = new JSONObject(AntFarmRpcCall.useFarmFood(cookbookId, cuisineId));
                if (!MessageUtil.checkMemo(TAG, jo)) {
                    return false;
                }
                double deltaProduce = jo.getJSONObject("foodEffect").getDouble("deltaProduce");
                Log.farm("使用美食🍱[" + name + "]#加速" + deltaProduce + "颗爱心鸡蛋");
                Status.useSpecialFoodToday();
                if (!Status.canUseSpecialFoodToday()) {
                    break;
                }
            }
            return true;
        }
        catch (Throwable t) {
            Log.i(TAG, "useFarmFood err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void drawLotteryPlus(JSONObject lotteryPlusInfo) {
        try {
            if (!lotteryPlusInfo.has("userSevenDaysGiftsItem")) {
                return;
            }
            String itemId = lotteryPlusInfo.getString("itemId");
            JSONObject jo = lotteryPlusInfo.getJSONObject("userSevenDaysGiftsItem");
            JSONArray ja = jo.getJSONArray("userEverydayGiftItems");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                if (jo.getString("itemId").equals(itemId)) {
                    if (!jo.getBoolean("received")) {
                        String singleDesc = jo.getString("singleDesc");
                        int awardCount = jo.getInt("awardCount");
                        if (singleDesc.contains("饲料") && awardCount + foodStock > foodStockLimit) {
                            Log.record("暂停领取[" + awardCount + "]克饲料，上限为[" + foodStockLimit + "]克");
                            break;
                        }
                        jo = new JSONObject(AntFarmRpcCall.drawLotteryPlus());
                        if (MessageUtil.checkMemo(TAG, jo)) {
                            Log.farm("惊喜礼包🎁[" + singleDesc + "*" + awardCount + "]");
                        }
                    }
                    else {
                        Log.record("当日奖励已领取");
                    }
                    break;
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "drawLotteryPlus err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void visitFriend() {
        Map<String, Integer> map = visitFriendList.getValue();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String userId = entry.getKey();
            Integer countLimit = entry.getValue();
            if (userId.equals(UserIdMap.getCurrentUid())) {
                continue;
            }
            if (Status.canVisitFriendToday(userId, countLimit)) {
                visitFriend(userId, countLimit);
            }
        }
    }

    private void visitFriend(String userId, int countLimit) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.enterFarm(userId));
            //方法: com.alipay.antfarm.enterFarm
            //参数: [{"queryLastRecordNum":true,"recall":false,"requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","userId":"2088642642387040"}]
            //数据: {"ariverRpcTraceId":"2184aa8a17545054778948193e9221","memo":"非好友","resultCode":"302","success":false}
            if (MyUtils.closeErrorFunction() && "非好友".equals(jo.optString("memo"))) {//送麦子出现非好友
                return;//CHANGE BY KT
            }
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            JSONObject farmVO = jo.getJSONObject("farmVO");
            foodStock = farmVO.getInt("foodStock");
            JSONObject subFarmVO = farmVO.getJSONObject("subFarmVO");
            if (subFarmVO.optBoolean("visitedToday", true)) {
                Status.flagToday("farm::visitFriendLimit::" + userId);
                return;
            }
            String farmId = subFarmVO.getString("farmId");
            while (Status.canVisitFriendToday(userId, countLimit) && foodStock >= 10) {
                jo = new JSONObject(AntFarmRpcCall.visitFriend(farmId));
                if (!MessageUtil.checkMemo(TAG, jo)) {
                    break;
                }
                TimeUtil.sleep(1000);
                Status.visitFriendToday(userId);
                foodStock = jo.getInt("foodStock");
                Log.farm("赠送麦子🌾赠送[" + UserIdMap.getMaskName(userId) + "]麦子#消耗[" + jo.getInt("giveFoodNum") + "g饲料]");
                if (jo.optBoolean("isReachLimit")) {
                    Log.record("今日给[" + UserIdMap.getMaskName(userId) + "]送麦子已达上限");
                    Status.flagToday("farm::visitFriendLimit::" + userId);
                    break;
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "visitFriend err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void acceptGift() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.acceptGift());
            if (MessageUtil.checkMemo(TAG, jo)) {
                int receiveFoodNum = jo.getInt("receiveFoodNum");
                Log.farm("收取麦子🌾[" + receiveFoodNum + "g]");
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "acceptGift err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void queryChickenDiary(String queryDayStr) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryChickenDiary(queryDayStr));
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONObject data = jo.getJSONObject("data");
            JSONObject chickenDiary = data.getJSONObject("chickenDiary");
            String diaryDateStr = chickenDiary.getString("diaryDateStr");
            if (data.has("hasTietie")) {
                if (!data.optBoolean("hasTietie", true)) {
                    jo = new JSONObject(AntFarmRpcCall.diaryTietie(diaryDateStr, "NEW"));
                    if (MessageUtil.checkMemo(TAG, jo)) {
                        String prizeType = jo.getString("prizeType");
                        int prizeNum = jo.optInt("prizeNum", 0);
                        Log.farm("贴贴小鸡💞奖励[" + prizeType + "*" + prizeNum + "]");
                    }
                    if (!chickenDiary.has("statisticsList")) {
                        return;
                    }
                    JSONArray statisticsList = chickenDiary.getJSONArray("statisticsList");
                    if (statisticsList.length() > 0) {
                        for (int i = 0; i < statisticsList.length(); i++) {
                            JSONObject tietieStatus = statisticsList.getJSONObject(i);
                            String tietieRoleId = tietieStatus.getString("tietieRoleId");
                            jo = new JSONObject(AntFarmRpcCall.diaryTietie(diaryDateStr, tietieRoleId));
                            if (MessageUtil.checkMemo(TAG, jo)) {
                                String prizeType = jo.getString("prizeType");
                                int prizeNum = jo.optInt("prizeNum", 0);
                                Log.farm("贴贴小鸡💞奖励[" + prizeType + "*" + prizeNum + "]");
                            }
                        }
                    }
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "queryChickenDiary err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void queryChickenDiaryList() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryChickenDiaryList());
            if (!MessageUtil.checkResultCode(TAG, jo)) {
                return;
            }
            JSONArray chickenDiaryBriefList = jo.getJSONObject("data").optJSONArray("chickenDiaryBriefList");
            if (chickenDiaryBriefList != null && chickenDiaryBriefList.length() > 0) {
                for (int i = 0; i < chickenDiaryBriefList.length(); i++) {
                    jo = chickenDiaryBriefList.getJSONObject(i);
                    if (!jo.optBoolean("read", true)) {
                        String dateStr = jo.getString("dateStr");
                        queryChickenDiary(dateStr);
                        TimeUtil.sleep(300);
                    }
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "queryChickenDiaryList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void visitAnimal() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.visitAnimal());
            if (!MessageUtil.checkMemo(TAG, jo) || !jo.has("talkConfigs")) {
                return;
            }

            JSONArray talkNodes = jo.getJSONArray("talkNodes");
            JSONArray talkConfigs = jo.getJSONArray("talkConfigs");
            JSONObject data = talkConfigs.getJSONObject(0);
            String farmId = data.getString("farmId");
            jo = new JSONObject(AntFarmRpcCall.feedFriendAnimalVisit(farmId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            JSONArray actionNodes = null;
            for (int i = 0; i < talkNodes.length(); i++) {
                jo = talkNodes.getJSONObject(i);
                if (jo.has("actionNodes")) {
                    actionNodes = jo.getJSONArray("actionNodes");
                    break;
                }
            }
            if (actionNodes == null) {
                return;
            }
            for (int i = 0; i < actionNodes.length(); i++) {
                jo = actionNodes.getJSONObject(i);
                if (!"FEED".equals(jo.getString("type"))) {
                    continue;
                }
                String consistencyKey = jo.getString("consistencyKey");
                jo = new JSONObject(AntFarmRpcCall.visitAnimalSendPrize(consistencyKey));
                if (MessageUtil.checkMemo(TAG, jo)) {
                    String prizeName = jo.getString("prizeName");
                    String userMaskName = UserIdMap.getMaskName(AntFarmRpcCall.farmId2UserId(farmId));
                    Log.farm("小鸡到访💞投喂[" + userMaskName + "]#获得[" + prizeName + "]");
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "visitAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void drawMachineGroups() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryLoveCabin(UserIdMap.getCurrentUid()));
            if (MessageUtil.checkMemo(TAG, jo)) {
                drawMachine("ANTFARM_DAILY_DRAW_TASK", "dailyDrawMachine", "ipDrawMachine");

                JSONObject queryDrawMachineActivityjo = new JSONObject(AntFarmRpcCall.queryDrawMachineActivity_New("ipDrawMachine", "dailyDrawMachine"));
                if (MessageUtil.checkMemo(TAG, jo)) {
                    if (!queryDrawMachineActivityjo.has("otherDrawMachineActivityIds")) {
                        return;
                    }
                    if (queryDrawMachineActivityjo.getJSONArray("otherDrawMachineActivityIds").length() > 0) {
                        drawMachine("ANTFARM_IP_DRAW_TASK", "ipDrawMachine", "dailyDrawMachine");
                    }
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "queryLoveCabin err:");
            Log.printStackTrace(t);
        }
    }

    private void drawMachine(String taskSceneCode, String scene, String otherScenes) {
        doFarmDrawTask(taskSceneCode);
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryDrawMachineActivity_New(otherScenes, scene));
            int drawTimes = jo.optInt("drawTimes", 0);
            for (int i = 0; i < drawTimes; i++) {
                if (!drawMachine(scene)) {
                    return;
                }
                TimeUtil.sleep(5000);
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "drawMachine err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void doFarmDrawTask(String taskSceneCode) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.listFarmDrawTask(taskSceneCode));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            JSONArray farmTaskList = jo.getJSONArray("farmTaskList");
            for (int i = 0; i < farmTaskList.length(); i++) {
                jo = farmTaskList.getJSONObject(i);
                String taskStatus = jo.getString("taskStatus");
                String title = jo.getString("title");
                String desc = jo.getString("desc");
                //屏蔽玩游戏和捐赠任务
                //if(title.contains("玩游戏得新机会")||desc.contains("单笔捐赠")||desc.contains("玩30秒")){
                if (title.contains("玩游戏得新机会") || desc.contains("单笔捐赠")) {
                    Log.record("装扮抽抽乐跳过屏蔽任务：" + title);
                    continue;
                }

                if (TaskStatus.RECEIVED.name().equals(taskStatus)) {
                    continue;
                }

                if (TaskStatus.TODO.name().equals(taskStatus)) {
                    int rightsTimesLimit = jo.optInt("rightsTimesLimit");
                    int rightsTimes = jo.optInt("rightsTimes");

                    if (jo.optString("taskId").contains("EXCHANGE") || jo.optString("taskId").contains("FKDWChuodong") || jo.optString("taskId").contains("GYG2") || jo.optString("taskId").equals("jiatingdongrirongrongwu")) {
                        for (int j = 0; j < (rightsTimesLimit - rightsTimes); j++) {
                            AntFarmRpcCall.doFarmTask(jo.optString("bizKey"), taskSceneCode);
                        }
                        TimeUtil.sleep(1000);
                    }
                    if (jo.optString("taskId").contains("SHANGYEHUA") || jo.optString("taskId").contains("30s")) {
                        for (int j = 0; j < (rightsTimesLimit - rightsTimes); j++) {
                            AntFarmRpcCall.finishTask(jo.optString("taskId"), taskSceneCode);
                        }
                        TimeUtil.sleep(2000);
                    }
                    TimeUtil.sleep(1000);
                }
                TimeUtil.sleep(2000);
                String taskId = jo.getString("taskId");
                String awardType = jo.getString("awardType");
                receiveFarmDrawTaskAward(taskId, title, awardType, taskSceneCode);
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "doFarmDrawActivityTimeTask err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void receiveFarmDrawTaskAward(String taskId, String title, String awardType, String taskSceneCode) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.receiveFarmDrawTimesTaskAward(taskId, awardType, taskSceneCode));
            if (MessageUtil.checkMemo(TAG, jo)) {
                Log.farm("装扮抽奖🎖️领取[" + title + "]奖励");
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "receiveFarmDrawTimesTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean drawMachine(String scene) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.drawMachine(scene));
            if (MessageUtil.checkMemo(TAG, jo)) {
                if (!jo.has("title")) {
                    jo = jo.optJSONObject("drawMachinePrize");
                }
                String title = jo.optString("title");
                Log.farm("装扮抽奖🎁抽中[" + title + "]");
                return true;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "drawMachine err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    /* 雇佣好友小鸡 */
    private void hireAnimal() {
        try {
            syncAnimalStatus(ownerFarmId);
            if (!AnimalFeedStatus.EATING.name().equals(ownerAnimal.animalFeedStatus)) {
                return;
            }
            int count = 3 - animals.length;
            if (count <= 0) {
                return;
            }
            Log.farm("雇佣小鸡👷[当前可雇佣小鸡数量:" + count + "只]");
            if (foodStock < 50) {
                Log.record("饲料不足，暂不雇佣");
                return;
            }

            boolean hasNext;
            int pageStartSum = 0;
            Set<String> hireAnimalSet = hireAnimalList.getValue();
            do {
                JSONObject jo = new JSONObject(AntFarmRpcCall.rankingList(pageStartSum));
                if (!MessageUtil.checkMemo(TAG, jo)) {
                    return;
                }
                JSONArray rankingList = jo.getJSONArray("rankingList");
                hasNext = jo.getBoolean("hasNext");
                pageStartSum += rankingList.length();
                for (int i = 0; i < rankingList.length() && count > 0; i++) {
                    jo = rankingList.getJSONObject(i);
                    String userId = jo.getString("userId");
                    boolean isHireAnimal = hireAnimalSet.contains(userId);
                    if (hireAnimalType.getValue() != HireAnimalType.HIRE) {
                        isHireAnimal = !isHireAnimal;
                    }
                    if (!isHireAnimal || userId.equals(UserIdMap.getCurrentUid())) {
                        continue;
                    }
                    String actionTypeListStr = jo.getJSONArray("actionTypeList").toString();
                    if (actionTypeListStr.contains("can_hire_action")) {
                        if (hireAnimalAction(userId)) {
                            count--;
                            autoFeedAnimal();
                        }
                    }
                }
            }
            while (hasNext && count > 0);

            if (count > 0) {
                Log.farm("没有足够的小鸡可以雇佣");
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "hireAnimal err:");
            Log.printStackTrace(TAG, t);
        }
        finally {
            long updateTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
            String taskId = "UPDATE|HIRE|" + ownerFarmId;
            addChildTask(new ChildModelTask(taskId, "UPDATE", this::autoHireAnimal, updateTime));
        }
    }

    private void autoHireAnimal() {
        try {
            syncAnimalStatus(ownerFarmId);
            for (Animal animal : animals) {
                if (!SubAnimalType.WORK.name().equals(animal.subAnimalType)) {
                    continue;
                }
                String taskId = "HIRE|" + animal.animalId;
                if (!hasChildTask(taskId)) {
                    long beHiredEndTime = animal.beHiredEndTime;
                    addChildTask(new ChildModelTask(taskId, "HIRE", this::hireAnimal, beHiredEndTime));
                    Log.record("添加蹲点雇佣👷在[" + TimeUtil.getCommonDate(beHiredEndTime) + "]执行");
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "autoHireAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean hireAnimalAction(String userId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.enterFarm("", userId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return false;
            }
            jo = jo.getJSONObject("farmVO").getJSONObject("subFarmVO");
            String farmId = jo.getString("farmId");
            JSONArray animals = jo.getJSONArray("animals");
            for (int i = 0, len = animals.length(); i < len; i++) {
                JSONObject animal = animals.getJSONObject(i);
                if (Objects.equals(animal.getJSONObject("masterUserInfoVO").getString("userId"), userId)) {
                    String animalId = animal.getString("animalId");
                    jo = new JSONObject(AntFarmRpcCall.hireAnimal(farmId, animalId));
                    if (MessageUtil.checkMemo(TAG, jo)) {
                        foodStock = jo.getInt("foodStock");
                        int reduceFoodNum = jo.getInt("reduceFoodNum");
                        Log.farm("雇佣小鸡👷雇佣[" + UserIdMap.getMaskName(userId) + "]#消耗[" + reduceFoodNum + "g饲料]");
                        return true;
                    }
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "hireAnimalAction err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void drawGameCenterAward() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryGameList());
            if (jo.optBoolean("success")) {
                JSONObject gameDrawAwardActivity = jo.getJSONObject("gameDrawAwardActivity");
                int canUseTimes = gameDrawAwardActivity.getInt("canUseTimes");
                while (canUseTimes > 0) {
                    try {
                        jo = new JSONObject(AntFarmRpcCall.drawGameCenterAward());
                        if (jo.optBoolean("success")) {
                            canUseTimes = jo.getInt("drawRightsTimes");
                            JSONArray gameCenterDrawAwardList = jo.getJSONArray("gameCenterDrawAwardList");
                            ArrayList<String> awards = new ArrayList<String>();
                            for (int i = 0; i < gameCenterDrawAwardList.length(); i++) {
                                JSONObject gameCenterDrawAward = gameCenterDrawAwardList.getJSONObject(i);
                                int awardCount = gameCenterDrawAward.getInt("awardCount");
                                String awardName = gameCenterDrawAward.getString("awardName");
                                awards.add(awardName + "*" + awardCount);
                            }
                            Log.farm("小鸡乐园🎮开宝箱得[" + StringUtil.collectionJoinString(",", awards) + "]");
                        }
                        else {
                            Log.i(TAG, "drawGameCenterAward falsed result: " + jo.toString());
                        }
                    }
                    catch (Throwable t) {
                        Log.printStackTrace(TAG, t);
                    }
                    finally {
                        TimeUtil.sleep(3000);
                    }
                }
            }
            else {
                Log.i(TAG, "queryGameList falsed result: " + jo.toString());
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "queryChickenDiaryList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    // 装扮焕新
    private void ornamentsDressUp() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.listOrnaments());
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            List<JSONObject> list = new ArrayList<>();
            JSONArray achievementOrnaments = jo.getJSONArray("achievementOrnaments");
            long takeOffTime = System.currentTimeMillis();
            for (int i = 0; i < achievementOrnaments.length(); i++) {
                jo = achievementOrnaments.getJSONObject(i);
                if (!jo.optBoolean("acquired")) {
                    continue;
                }
                if (jo.has("takeOffTime")) {
                    takeOffTime = jo.getLong("takeOffTime");
                }
                String resourceKey = jo.getString("resourceKey");
                String name = jo.getString("name");
                if (ornamentsDressUpList.getValue().contains(resourceKey)) {
                    list.add(jo);
                }
                FarmOrnamentsIdMap.add(resourceKey, name);
            }
            FarmOrnamentsIdMap.save(UserIdMap.getCurrentUid());
            if (list.isEmpty() || takeOffTime + TimeUnit.DAYS.toMillis(ornamentsDressUpDays.getValue() - 15) > System.currentTimeMillis()) {
                return;
            }

            jo = list.get(RandomUtil.nextInt(0, list.size() - 1));
            if (saveOrnaments(jo)) {
                Log.farm("装扮焕新✨[" + jo.getString("name") + "]");
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "ornamentsDressUp err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private Boolean saveOrnaments(JSONObject ornaments) {
        try {
            String animalId = ownerAnimal.animalId;
            String farmId = ownerFarmId;
            String ornamentsSets = getOrnamentsSets(ornaments.getJSONArray("sets"));
            JSONObject jo = new JSONObject(AntFarmRpcCall.saveOrnaments(animalId, farmId, ornamentsSets));
            return MessageUtil.checkMemo(TAG, jo);
        }
        catch (Throwable t) {
            Log.i(TAG, "saveOrnaments err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private String getOrnamentsSets(JSONArray sets) {
        StringBuilder ornamentsSets = new StringBuilder();
        try {
            for (int i = 0; i < sets.length(); i++) {
                JSONObject set = sets.getJSONObject(i);
                if (i > 0) {
                    ornamentsSets.append(",");
                }
                ornamentsSets.append(set.getString("id"));
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "getOrnamentsSets err:");
            Log.printStackTrace(TAG, t);
        }
        return ornamentsSets.toString();
    }

    // 一起拿小鸡饲料
  /*  private void letsGetChickenFeedTogether() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.letsGetChickenFeedTogether());
            if (jo.optBoolean("success")) {
                String bizTraceId = jo.getString("bizTraceId");
                JSONArray p2pCanInvitePersonDetailList = jo.getJSONArray("p2pCanInvitePersonDetailList");
                
                int canInviteCount = 0;
                int hasInvitedCount = 0;
                List<String> userIdList = new ArrayList<>(); // 保存 userId
                for (int i = 0; i < p2pCanInvitePersonDetailList.length(); i++) {
                    JSONObject personDetail = p2pCanInvitePersonDetailList.getJSONObject(i);
                    String inviteStatus = personDetail.getString("inviteStatus");
                    String userId = personDetail.getString("userId");
                    
                    if (inviteStatus.equals("CAN_INVITE")) {
                        userIdList.add(userId);
                        canInviteCount++;
                    }
                    else if (inviteStatus.equals("HAS_INVITED")) {
                        hasInvitedCount++;
                    }
                }
                
                int invitedToday = hasInvitedCount;
                
                int remainingInvites = 5 - invitedToday;
                int invitesToSend = Math.min(canInviteCount, remainingInvites);
                
                if (invitesToSend == 0) {
                    return;
                }
                
                Set<String> getFeedSet = getFeedList.getValue();
                
                //if (getFeedType.getValue() == GetFeedType.GIVE) {
                    //for (String userId : userIdList) {
                        //if (invitesToSend <= 0) {
                            //                            Log.record("已达到最大邀请次数限制，停止发送邀请。");
                            //break;
                        //}
                        //if (getFeedSet.contains(userId)) {
                         //   jo = new JSONObject(AntFarmRpcCall.giftOfFeed(bizTraceId, userId));
                            //if (jo.optBoolean("success")) {
                                //Log.record("一起拿小鸡饲料🥡 [送饲料：" + UserIdMap.getMaskName(userId) + "]");
                                //invitesToSend--; // 每成功发送一次邀请，减少一次邀请次数
                            //}
                            //else {
                                //Log.record("邀请失败：" + jo);
                                //break;
                            //}
                        //}
                        //else {
                            //                            Log.record("用户 " + UserIdMap.getMaskName(userId) + "
                            // 不在勾选的好友列表中，不发送邀请。");
                     //   }
                  //  }
             //   }
                else {
                    Random random = new Random();
                    for (int j = 0; j < invitesToSend; j++) {
                        int randomIndex = random.nextInt(userIdList.size());
                        String userId = userIdList.get(randomIndex);
                        
                        jo = new JSONObject(AntFarmRpcCall.giftOfFeed(bizTraceId, userId));
                        if (jo.optBoolean("success")) {
                            Log.record("一起拿小鸡饲料🥡 [送饲料：" + UserIdMap.getMaskName(userId) + "]");
                        }
                        else {
                            Log.record("邀请失败：" + jo);
                            break;
                        }
                        userIdList.remove(randomIndex);
                    }
                }
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "letsGetChickenFeedTogether err:");
            Log.printStackTrace(t);
        }
    }  */

    private void family() {
        if (StringUtil.isEmpty(ownerGroupId)) {
            return;
        }
        // 检查 ExtensionsHandle 是否存在
        try {
            Class.forName("io.github.lazyimmortal.sesame.model.extensions.ExtensionsHandle");
            ExtensionsHandle.handleAlphaRequest("antFarm", "doFamilyTask", null);
        }
        catch (ClassNotFoundException e) {
            Log.record("ExtensionsHandle 类未找到，跳过扩展处理");
        }
        try {
            JSONObject jo = enterFamily();
            if (jo == null) {
                return;
            }
            ownerGroupId = jo.getString("groupId");
            int familyAwardNum = jo.getInt("familyAwardNum");
            boolean familySignTips = jo.getBoolean("familySignTips");
            JSONObject assignFamilyMemberInfo = jo.optJSONObject("assignFamilyMemberInfo");
            boolean feedFriendLimit = jo.optBoolean("feedFriendLimit", false);
            JSONArray familyAnimals = jo.getJSONArray("animals");
            JSONArray EatTogetherUserIds = new JSONArray();
            JSONArray familyAnimalsExceptUser = familyAnimals;
            for (int i = familyAnimalsExceptUser.length() - 1; i >= 0; i--) {
                jo = familyAnimalsExceptUser.getJSONObject(i);
                String userId = jo.getString("userId");
                EatTogetherUserIds.put(userId);
                if (userId.equals(UserIdMap.getCurrentUid())) {
                    familyAnimalsExceptUser.remove(i);
                }
            }
            // 获取家庭成员ID列表
            List<String> familyUserIds = new ArrayList<>();
            JSONArray friendUserIds = new JSONArray();
            for (int i = 0; i < familyAnimals.length(); i++) {
                jo = familyAnimals.getJSONObject(i);
                String animalId = jo.getString("animalId");
                String userId = jo.getString("userId");
                familyUserIds.add(userId);
                friendUserIds.put(userId);
                if (animalId.equals(ownerAnimal.animalId)) {
                    continue;
                }
                String farmId = jo.getString("farmId");
                JSONObject animalStatusVO = jo.getJSONObject("animalStatusVO");
                String animalFeedStatus = animalStatusVO.getString("animalFeedStatus");
                String animalInteractStatus = animalStatusVO.getString("animalInteractStatus");
                if (AnimalInteractStatus.HOME.name().equals(animalInteractStatus) && AnimalFeedStatus.HUNGRY.name().equals(animalFeedStatus)) {
                    if (familyOptions.getValue().contains("familyFeed")) {
                        feedFriendAnimal(farmId);
                    }
                }
            }


            // 家庭签到
            if (familySignTips && familyOptions.getValue().contains("familySign")) {
                familySign();
            }
            // 顶梁柱功能
            if (assignFamilyMemberInfo != null && familyOptions.getValue().contains("assignRights") && !"USED".equals(assignFamilyMemberInfo.getJSONObject("assignRights").optString("status"))) {
                if (UserIdMap.getCurrentUid().equals(assignFamilyMemberInfo.getJSONObject("assignRights").optString("assignRightsOwner"))) {
                    assignFamilyMember(assignFamilyMemberInfo, familyUserIds);
                }
                else {
                    Log.record("家庭任务🏡[使用顶梁柱特权] 不是家里的顶梁柱！");
                    // 移除选项，避免重复检查
                    familyOptions.getValue().remove("assignRights");
                }
            }

            // 领取家庭奖励
            if (familyOptions.getValue().contains("familyClaimReward") && familyAwardNum > 0) {
                familyAwardList();
            }

            // 帮家庭成员喂鸡
            if (familyOptions.getValue().contains("feedFamilyAnimal") && !feedFriendLimit) {
                familyFeedFriendAnimal(familyAnimals);
            }

            JSONArray familyInteractActions = jo.optJSONArray("familyInteractActions");
            JSONObject eatTogetherConfig = jo.optJSONObject("eatTogetherConfig");
            //家庭请客吃饭
            boolean canEatTogether = true;
            if (familyInteractActions != null) {
                for (int i = 0; i < familyInteractActions.length(); i++) {
                    JSONObject familyInteractAction = familyInteractActions.getJSONObject(i);
                    if ("EatTogether".equals(familyInteractAction.optString("familyInteractType"))) {
                        canEatTogether = false;
                    }
                }
            }
            // 一起吃饭
            if (canEatTogether && familyOptions.getValue().contains("familyEatTogether") && eatTogetherConfig != null) {
                familyEatTogether(ownerGroupId, EatTogetherUserIds);
            }

            // 分享给好友
            if (familyOptions.getValue().contains("shareToFriends")) {
                familyShareToFriends(familyUserIds, notInviteList);
            }

          //扭蛋
          JSONObject familyDrawInfo = jo.optJSONObject("familyDrawInfo");
          if (familyDrawInfo != null && familyDrawInfo.optBoolean("drawActivitySwitch", false)) {
            if (familyOptions.getValue().contains("familyDrawInfo")) {
              FamilyDrawInfo.familyDrawTask(familyOptions, giftFamilyDrawFragment, familyUserIds, familyDrawInfo);
            }
          }
        }
        catch (Throwable t) {
            Log.i(TAG, "family err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /**
     * 顶梁柱功能
     */
    private void assignFamilyMember(JSONObject jsonObject, List<String> userIds) {
        try {
            userIds.remove(UserIdMap.getCurrentUid());
            if (userIds.isEmpty()) {
                return;
            }
            String beAssignUser = userIds.get(RandomUtil.nextInt(0, userIds.size() - 1));
            JSONArray assignConfigList = jsonObject.getJSONArray("assignConfigList");
            JSONObject assignConfig = assignConfigList.getJSONObject(RandomUtil.nextInt(0, assignConfigList.length() - 1));
            JSONObject jo = new JSONObject(AntFarmRpcCall.assignFamilyMember(assignConfig.getString("assignAction"), beAssignUser));
            if (MessageUtil.checkMemo(TAG, jo)) {
                Log.farm("家庭任务🏡[使用顶梁柱特权] " + assignConfig.getString("assignDesc"));
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "assignFamilyMember err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /**
     * 帮家庭成员喂鸡
     */
    private void familyFeedFriendAnimal(JSONArray animals) {
        try {
            for (int i = 0; i < animals.length(); i++) {
                JSONObject animal = animals.getJSONObject(i);
                JSONObject status = animal.getJSONObject("animalStatusVO");
                String interactStatus = status.getString("animalInteractStatus");
                String feedStatus = status.getString("animalFeedStatus");

                if (!AnimalInteractStatus.HOME.name().equals(interactStatus) || !AnimalFeedStatus.HUNGRY.name().equals(feedStatus)) {
                    continue;
                }

                String groupId = animal.getString("groupId");
                String farmId = animal.getString("farmId");
                String userId = animal.getString("userId");

                if (!UserIdMap.getUserIdSet().contains(userId)) {
                    Log.record(userId + " 不是你的好友！ 跳过家庭喂食");
                    continue;
                }

                String flagKey = "farm::feedFriendLimit::" + userId;
                if (Status.hasFlagToday(flagKey)) {
                    Log.record("[" + userId + "] 今日喂鸡次数已达上限（已记录）🥣，跳过");
                    continue;
                }

                JSONObject jo = new JSONObject(AntFarmRpcCall.feedFriendAnimal(farmId, groupId));
                if (!jo.optBoolean("success", false)) {
                    String code = jo.optString("resultCode");
                    if ("391".equals(code)) {
                        Status.flagToday(flagKey);
                        Log.record("[" + userId + "] 今日帮喂次数已达上限🥣，已记录为当日限制");
                    }
                    else {
                        Log.record("喂食失败 user=" + userId + " code=" + code + " msg=" + jo.optString("memo"));
                    }
                    continue;
                }

                int foodStockAfter = jo.optInt("foodStock");
                String maskName = UserIdMap.getMaskName(userId);
                Log.farm("家庭任务🏠帮喂好友🥣[" + maskName + "]的小鸡180g #剩余" + foodStockAfter + "g");
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "familyFeedFriendAnimal err:");
            Log.printStackTrace(TAG, t);
        }
    }
    private void familyEatTogether(String groupId, JSONArray EatTogetherUserIds) {
        long currentTime = System.currentTimeMillis();
        String periodName;
        if (TimeUtil.isAfterTimeStr(currentTime, "0600") && TimeUtil.isBeforeTimeStr(currentTime, "1100")) {
            periodName = "早餐";
        }
        else if (TimeUtil.isAfterTimeStr(currentTime, "1100") && TimeUtil.isBeforeTimeStr(currentTime, "1600")) {
            periodName = "午餐";
        }
        else if (TimeUtil.isAfterTimeStr(currentTime, "1600") && TimeUtil.isBeforeTimeStr(currentTime, "2000")) {
            periodName = "晚餐";
        }
        else {
            return;
        }
        try {
            JSONArray cuisines = queryRecentFarmFood(EatTogetherUserIds.length());
            if (cuisines == null) {
                return;
            }
            JSONObject jo = new JSONObject(AntFarmRpcCall.familyEatTogether(groupId, cuisines, EatTogetherUserIds));
            if (MessageUtil.checkMemo(TAG, jo)) {
                Log.farm("亲密家庭🏠" + periodName + "请客#消耗美食" + EatTogetherUserIds.length() + "份#[" + UserIdMap.getShowName(UserIdMap.getCurrentUid()) + "]");
                syncFamilyStatus(groupId);
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "familyEatTogether err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /**
     * 好友分享家庭
     */
    private void familyShareToFriends(List<String> familyUserIds, SelectModelField notInviteList) {
        try {
            if (Status.hasFlagToday("antFarm::familyShareToFriends")) {
                return;
            }

            Set<String> notInviteSet = notInviteList.getValue();
            List<AlipayUser> allUser = AlipayUser.getList();

            if (allUser.isEmpty()) {
                Log.record("allUser is empty");
                return;
            }

            // 打乱顺序，实现随机选取
            List<AlipayUser> shuffledUsers = new ArrayList<>(allUser);
            Collections.shuffle(shuffledUsers);

            JSONArray inviteList = new JSONArray();
            for (AlipayUser user : shuffledUsers) {
                if (!familyUserIds.contains(user.getId()) && !notInviteSet.contains(user.getId())) {
                    inviteList.put(user.getId());
                    if (inviteList.length() >= 6) {
                        break;
                    }
                }
            }

            if (inviteList.length() == 0) {
                Log.record("没有符合分享条件的好友");
                return;
            }

            Log.record("inviteList: " + inviteList);

            JSONObject jo = new JSONObject(AntFarmRpcCall.inviteFriendVisitFamily(inviteList));
            if (MessageUtil.checkMemo(TAG, jo)) {
                Log.farm("家庭任务🏠分享好友");
                Status.flagToday("antFarm::familyShareToFriends");
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "familyShareToFriends err:");
            Log.printStackTrace(TAG, t);
        }
    }

    /**
     * 时间差格式化
     */
    private String formatDuration(long diffMillis) {
        long absSeconds = Math.abs(diffMillis) / 1000;

        long value;
        String unit;
        if (absSeconds < 60) {
            value = absSeconds;
            unit = "秒";
        }
        else if (absSeconds < 3600) {
            value = absSeconds / 60;
            unit = "分钟";
        }
        else if (absSeconds < 86400) {
            value = absSeconds / 3600;
            unit = "小时";
        }
        else if (absSeconds < 2592000) {
            value = absSeconds / 86400;
            unit = "天";
        }
        else if (absSeconds < 31536000) {
            value = absSeconds / 2592000;
            unit = "个月";
        }
        else {
            value = absSeconds / 31536000;
            unit = "年";
        }

        if (absSeconds < 1) {
            return "刚刚";
        }
        else if (diffMillis > 0) {
            return value + unit + "后";
        }
        else {
            return value + unit + "前";
        }
    }

    private String getFamilyGroupId(String userId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.queryLoveCabin(userId));
            if (MessageUtil.checkMemo(TAG, jo)) {
                return jo.optString("groupId");
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "getGroupId err:");
            Log.printStackTrace(t);
        }
        return null;
    }

    private JSONObject enterFamily() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.enterFamily());
            if (MessageUtil.checkMemo(TAG, jo)) {
                return jo;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "enterFamily err:");
            Log.printStackTrace(TAG, t);
        }
        return null;
    }

    private Boolean familySleep(String groupId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.familySleep(groupId));
            if (MessageUtil.checkMemo(TAG, jo)) {
                Log.farm("亲密家庭🏠小鸡睡觉");
                syncFamilyStatus(groupId);
                return true;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "familySleep err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private Boolean familyWakeUp() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.familyWakeUp());
            if (MessageUtil.checkMemo(TAG, jo)) {
                Log.farm("亲密家庭🏠小鸡起床");
                return true;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "familyWakeUp err:");
            Log.printStackTrace(TAG, t);
        }
        return false;
    }

    private void familyAwardList() {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.familyAwardList());
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return;
            }
            JSONArray ja = jo.getJSONArray("familyAwardRecordList");
            for (int i = 0; i < ja.length(); i++) {
                jo = ja.getJSONObject(i);
                if (jo.optBoolean("expired") || jo.optBoolean("received", true) || jo.has("linkUrl") || (jo.has("operability") && !jo.getBoolean("operability"))) {
                    continue;
                }
                String rightId = jo.getString("rightId");
                String awardName = jo.getString("awardName");
                int count = jo.optInt("count", 1);
                receiveFamilyAward(rightId, awardName, count);
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "familyAwardList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void receiveFamilyAward(String rightId, String awardName, int count) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.receiveFamilyAward(rightId));
            if (MessageUtil.checkMemo(TAG, jo)) {
                Log.farm("亲密家庭🏠领取奖励[" + awardName + "*" + count + "]");
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "familyAwardList err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private void familyReceiveFarmTaskAward(String taskId, String title) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.familyReceiveFarmTaskAward(taskId));
            if (MessageUtil.checkMemo(TAG, jo)) {
                Log.farm("亲密家庭🏠提交任务[" + title + "]");
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "familyReceiveFarmTaskAward err:");
            Log.printStackTrace(TAG, t);
        }
    }

    private JSONArray queryRecentFarmFood(int needCount) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.syncAnimalStatus(ownerFarmId));
            if (!MessageUtil.checkMemo(TAG, jo)) {
                return null;
            }
            JSONArray cuisineList = jo.getJSONArray("cuisineList");
            if (cuisineList.length() == 0) {
                return null;
            }
            List<JSONObject> list = getSortedCuisineList(cuisineList);
            JSONArray result = new JSONArray();
            int count = 0;
            for (int i = 0; i < list.size() && count < needCount; i++) {
                jo = list.get(i);
                int countTemp = jo.getInt("count");
                if (count + countTemp >= needCount) {
                    countTemp = needCount - count;
                    jo.put("count", countTemp);
                }
                count += countTemp;
                result.put(jo);
            }
            if (count == needCount) {
                return result;
            }
        }
        catch (Throwable t) {
            Log.i(TAG, "queryRecentFarmFood err:");
            Log.printStackTrace(TAG, t);
        }
        return null;
    }

    private void familySign() {
        familyReceiveFarmTaskAward("FAMILY_SIGN_TASK", "每日签到");
    }

    private void syncFamilyStatus(String groupId) {
        try {
            JSONObject jo = new JSONObject(AntFarmRpcCall.syncFamilyStatus(groupId, "INTIMACY_VALUE", ownerUserId));
            MessageUtil.checkMemo(TAG, jo);
        }
        catch (Throwable t) {
            Log.i(TAG, "syncFamilyStatus err:");
            Log.printStackTrace(TAG, t);
        }
    }

    public interface RecallAnimalType {

        int ALWAYS = 0;
        int WHEN_THIEF = 1;
        int WHEN_HUNGRY = 2;
        int NEVER = 3;

        String[] nickNames = {"始终召回", "偷吃召回", "饥饿召回", "暂不召回"};
    }

    public interface SendBackAnimalWay {

        int HIT = 0;
        int NORMAL = 1;

        String[] nickNames = {"攻击", "常规"};
    }

    public interface SendBackAnimalType {

        int NONE = 0;
        int BACK = 1;
        int NOT_BACK = 2;

        String[] nickNames = {"不遣返小鸡", "遣返已选好友", "遣返未选好友"};
    }

    public enum AnimalBuff {
        ACCELERATING, INJURED, NONE
    }

    public enum AnimalFeedStatus {
        HUNGRY, EATING, SLEEPY
    }

    public enum AnimalInteractStatus {
        HOME, GOTOSTEAL, STEALING
    }

    public enum SubAnimalType {
        NORMAL, GUEST, PIRATE, WORK
    }

    public enum ToolType {
        STEALTOOL, ACCELERATETOOL, SHARETOOL, FENCETOOL, NEWEGGTOOL, DOLLTOOL, BIG_EATER_TOOL, ADVANCE_ORNAMENT_TOOL, ORDINARY_ORNAMENT_TOOL, RARE_ORNAMENT_TOOL;

        public static final CharSequence[] nickNames = {"蹭饭卡", "加速卡", "救济卡", "篱笆卡", "新蛋卡", "公仔补签卡", "加饭卡", "高级装扮补签", "普通装扮补签卡", "稀有装扮补签卡"};

        public CharSequence nickName() {
            return nickNames[ordinal()];
        }
    }

    public enum GameType {
        starGame, jumpGame, flyGame, hitGame;

        public static final CharSequence[] gameNames = {"星星球", "登山赛", "飞行赛", "欢乐揍小鸡"};

        public CharSequence gameName() {
            return gameNames[ordinal()];
        }
    }

    private static class Animal {
        public String animalId, currentFarmId, masterFarmId, animalBuff, subAnimalType, animalFeedStatus, animalInteractStatus;
        public String locationType;

        public String currentFarmMasterUserId;

        public Long startEatTime, beHiredEndTime;

        public Double consumeSpeed;

        public Double foodHaveEatten;
    }

    public enum TaskStatus {
        TODO, FINISHED, RECEIVED
    }

    private static class RewardFriend {
        public String consistencyKey, friendId, time;
    }

    private static class FarmTool {
        public ToolType toolType;
        public String toolId;
        public int toolCount, toolHoldLimit;
    }

    public interface HireAnimalType {

        int NONE = 0;
        int HIRE = 1;
        int NOT_HIRE = 2;

        String[] nickNames = {"不雇佣小鸡", "雇佣已选好友", "雇佣未选好友"};
    }

    //  public interface GetFeedType {

    //      int NONE = 0;
    //      int GIVE = 1;
    //     int RANDOM = 2;

    //    String[] nickNames = {"不赠送饲料", "赠送已选好友", "赠送随机好友"};
    // }

    public interface NotifyFriendType {

        int NONE = 0;
        int NOTIFY = 1;
        int NOT_NOTIFY = 2;

        String[] nickNames = {"不通知赶鸡", "通知已选好友", "通知未选好友"};
    }

    public interface DonationType {

        int ZERO = 0;
        int ONE = 1;
        int ALL = 2;

        String[] nickNames = {"不捐赠", "捐赠一个项目", "捐赠所有项目"};
    }
}