package pansong291.xposed.quickenergy.util;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lombok.Data;
import lombok.Getter;
import pansong291.xposed.quickenergy.task.model.antFarm.AntFarm.SendType;

@Data
public class Config {

    private static final String TAG = Config.class.getSimpleName();

    public static final Config INSTANCE = defInit();

    @Getter
    private static volatile boolean init;

    /* application */
    private boolean immediateEffect = true;
    private boolean recordLog;
    private boolean showToast;
    private int toastOffsetY;
    private int checkInterval;
    private boolean stayAwake;
    private boolean timeoutRestart;
    private boolean startAt0 = true;
    private boolean startAt7;
    private boolean enableOnGoing;
    private boolean batteryPerm = true;
    private boolean newRpc = true;
    private boolean debugMode = false;
    private boolean languageSimplifiedChinese;


    /* forest */
    private boolean collectEnergy;

    private boolean collectWateringBubble;

    private boolean batchRobEnergy;
    private boolean collectProp;
    private boolean limitCollect;
    private int limitCount;
    private boolean doubleCard;
    private List<String> doubleCardTime;
    private int doubleCountLimit;
    private int advanceTime;
    private int collectInterval = 350;
    private int collectTimeout;
    private int returnWater33;
    private int returnWater18;
    private int returnWater10;
    private boolean helpFriendCollect;
    private Set<String> dontCollectSet = new HashSet<>();
    private List<String> dontHelpCollectList;
    private boolean receiveForestTaskAward;
    private List<String> waterFriendList;
    private int waterFriendCount;
    private List<Integer> waterCountList;
    private boolean cooperateWater;
    private List<String> cooperateWaterList;
    private List<Integer> cooperateWaterNumList;
    private boolean ancientTree;
    private List<String> ancientTreeCityCodeList;
    private boolean energyRain;
    private boolean reserve;
    private List<String> reserveList;
    private List<Integer> reserveCountList;
    private boolean beach;
    private List<String> beachList;
    private List<Integer> beachCountList;
    private boolean ancientTreeOnlyWeek;

    private List<String> giveEnergyRainList;

    private int waitWhenException;

    private boolean exchangeEnergyDoubleClick;
    private int exchangeEnergyDoubleClickCount;
    private boolean antdodoCollect;
    private boolean antOcean;
    private boolean userPatrol;
    private boolean animalConsumeProp;
    private boolean collectGiftBox;
    private boolean totalCertCount;

    /* farm */
    private boolean enableFarm;
    private boolean rewardFriend;
    private boolean sendBackAnimal;
    private int sendType;
    private List<String> dontSendFriendList;
    private int recallAnimalType;
    private boolean receiveFarmToolReward;
    private boolean recordFarmGame;
    private boolean kitchen;
    private boolean useSpecialFood;
    private boolean useNewEggTool;
    private boolean harvestProduce;
    private boolean donation;
    private boolean answerQuestion;
    private boolean receiveFarmTaskAward;
    private boolean feedAnimal;
    private boolean useAccelerateTool;
    private List<String> feedFriendAnimalList;
    private List<Integer> feedFriendCountList;

    private List<String> farmGameTime;
    private List<String> animalSleepTime;
    private boolean notifyFriend;
    private boolean enableChouchoule = true;
    private List<String> dontNotifyFriendList;
    private List<String> whoYouWantGiveTo;
    private List<String> sendFriendCard;
    private boolean acceptGift;
    private List<String> visitFriendList;
    private List<Integer> visitFriendCountList;
    private boolean chickenDiary;
    private boolean antOrchard;
    private boolean receiveOrchardTaskAward;
    private int orchardSpreadManureCount;

    private boolean enableStall;
    private boolean stallAutoClose;
    private boolean stallAutoOpen;
    private boolean stallAutoTask;
    private boolean stallReceiveAward;
    private boolean stallOpenType;
    private List<String> stallOpenList;
    private List<String> stallWhiteList;
    private List<String> stallBlackList;
    private int stallAllowOpenTime;
    private int stallSelfOpenTime;
    private boolean stallDonate;
    private boolean stallInviteRegister;
    private boolean stallThrowManure;
    private List<String> stallInviteShopList;

    /* other */
    private boolean receivePoint;
    private boolean openTreasureBox;
    private boolean receiveCoinAsset;
    private boolean donateCharityCoin;
    private int minExchangeCount;
    private int latestExchangeTime;
    private int syncStepCount;
    private boolean kbSignIn;
    private boolean ecoLifeTick;
    private boolean tiyubiz;
    private boolean insBlueBeanExchange;
    private boolean collectSesame;
    private boolean zcjSignIn;
    private boolean merchantKmdk;
    private boolean greenFinance;
    private boolean antBookRead;
    private boolean consumeGold;
    private boolean omegakoiTown;

    /* forest */
    public void setDoubleCardTime(String i) {
        doubleCardTime = Arrays.asList(i.split(","));
    }

    public String getDoubleCardTime() {
        return String.join(",", doubleCardTime);
    }

    public void setFarmGameTime(String i) {
        farmGameTime = Arrays.asList(i.split(","));
    }

    public String getFarmGameTime() {
        return String.join(",", farmGameTime);
    }

    public boolean hasAnimalSleepTime() {
        for (String doubleTime : animalSleepTime) {
            if (checkInTimeSpan(doubleTime))
                return true;
        }
        return false;
    }

    public boolean hasFarmGameTime() {
        for (String doubleTime : farmGameTime) {
            if (checkInTimeSpan(doubleTime))
                return true;
        }
        return false;
    }

    public int getCollectInterval() {
        return Math.max(collectInterval, 100);
    }

    public List<String> getDontCollectList() {
        return new ArrayList<>(dontCollectSet);
    }

    public void setDontCollectList(List<String> dontCollectList) {
        this.dontCollectSet = new HashSet<>(dontCollectList);
    }

    public static Config defInit() {
        Config c = new Config();

        c.immediateEffect = true;
        c.recordLog = true;
        c.showToast = true;
        c.toastOffsetY = 0;
        c.stayAwake = true;
        c.timeoutRestart = true;
        c.startAt7 = false;
        c.enableOnGoing = false;
        c.languageSimplifiedChinese = false;

        c.collectEnergy = false;
        c.collectWateringBubble = true;
        c.batchRobEnergy = false;
        c.collectProp = true;
        c.checkInterval = 1800_000;
        c.waitWhenException = 60 * 60 * 1000;
        c.limitCollect = true;
        c.limitCount = 50;
        c.doubleCard = false;
        c.doubleCardTime = new ArrayList<>();
        c.doubleCardTime.add("0700-0730");
        c.doubleCountLimit = 6;
        c.advanceTime = 0;
        c.collectInterval = 350;
        c.collectTimeout = 2_000;
        c.returnWater33 = 0;
        c.returnWater18 = 0;
        c.returnWater10 = 0;
        c.helpFriendCollect = true;
        if (c.dontCollectSet == null)
            c.dontCollectSet = new HashSet<>();
        if (c.dontHelpCollectList == null)
            c.dontHelpCollectList = new ArrayList<>();
        c.receiveForestTaskAward = true;
        if (c.waterFriendList == null)
            c.waterFriendList = new ArrayList<>();
        if (c.waterCountList == null)
            c.waterCountList = new ArrayList<>();
        c.waterFriendCount = 66;
        c.cooperateWater = true;
        if (c.cooperateWaterList == null)
            c.cooperateWaterList = new ArrayList<>();
        if (c.cooperateWaterNumList == null)
            c.cooperateWaterNumList = new ArrayList<>();
        if (c.ancientTreeCityCodeList == null)
            c.ancientTreeCityCodeList = new ArrayList<>();
        c.ancientTree = false;
        c.reserve = true;
        if (c.reserveList == null)
            c.reserveList = new ArrayList<>();
        if (c.reserveCountList == null)
            c.reserveCountList = new ArrayList<>();
        c.beach = true;
        if (c.beachList == null)
            c.beachList = new ArrayList<>();
        if (c.beachCountList == null)
            c.beachCountList = new ArrayList<>();
        c.energyRain = true;
        if (c.giveEnergyRainList == null)
            c.giveEnergyRainList = new ArrayList<>();
        c.exchangeEnergyDoubleClick = false;
        c.exchangeEnergyDoubleClickCount = 6;
        c.ancientTreeOnlyWeek = true;
        c.antdodoCollect = true;
        c.antOcean = true;
        c.userPatrol = true;
        c.animalConsumeProp = true;
        c.collectGiftBox = true;
        c.totalCertCount = false;

        c.enableFarm = true;
        c.rewardFriend = false;
        c.sendBackAnimal = true;
        c.sendType = SendType.NORMAL;
        if (c.dontSendFriendList == null)
            c.dontSendFriendList = new ArrayList<>();
        c.recallAnimalType = RecallAnimalType.ALWAYS;
        c.receiveFarmToolReward = true;
        c.recordFarmGame = true;
        c.kitchen = true;
        c.useSpecialFood = false;
        c.useNewEggTool = true;
        c.harvestProduce = true;
        c.donation = true;
        c.answerQuestion = true;
        c.receiveFarmTaskAward = true;
        c.feedAnimal = true;
        c.useAccelerateTool = true;
        if (c.feedFriendAnimalList == null)
            c.feedFriendAnimalList = new ArrayList<>();
        if (c.feedFriendCountList == null)
            c.feedFriendCountList = new ArrayList<>();
        c.farmGameTime = new ArrayList<>();
        c.farmGameTime.add("2200-2400");
        c.animalSleepTime = new ArrayList<>();
        c.animalSleepTime.add("2300-2400");
        c.animalSleepTime.add("0000-0559");
        c.notifyFriend = false;
        if (c.dontNotifyFriendList == null)
            c.dontNotifyFriendList = new ArrayList<>();
        c.whoYouWantGiveTo = new ArrayList<>();
        c.sendFriendCard = new ArrayList<>();
        c.acceptGift = true;
        if (c.visitFriendList == null)
            c.visitFriendList = new ArrayList<>();
        if (c.visitFriendCountList == null)
            c.visitFriendCountList = new ArrayList<>();
        c.chickenDiary = true;
        c.antOrchard = true;
        c.receiveOrchardTaskAward = true;
        c.orchardSpreadManureCount = 0;

        c.enableStall = false;
        c.stallAutoClose = false;
        c.stallAutoOpen = false;
        c.stallAutoTask = true;
        c.stallReceiveAward = false;
        c.stallOpenType = true;
        c.stallOpenList = new ArrayList<>();
        c.stallWhiteList = new ArrayList<>();
        c.stallBlackList = new ArrayList<>();
        c.stallAllowOpenTime = 121;
        c.stallSelfOpenTime = 120;
        c.stallDonate = false;
        c.stallInviteRegister = false;
        c.stallThrowManure = false;
        c.stallInviteShopList = new ArrayList<>();

        c.receivePoint = true;
        c.openTreasureBox = true;
        c.receiveCoinAsset = true;
        c.donateCharityCoin = false;
        c.kbSignIn = true;
        c.syncStepCount = 22000;
        c.ecoLifeTick = true;
        c.tiyubiz = true;
        c.insBlueBeanExchange = true;
        c.collectSesame = false;
        c.zcjSignIn = false;
        c.merchantKmdk = false;
        c.greenFinance = false;
        c.antBookRead = false;
        c.consumeGold = false;
        c.omegakoiTown = false;
        return c;
    }

    public static Boolean isModify() {
        String json = null;
        if (FileUtils.getConfigFile(UserIdMap.getCurrentUid()).exists()) {
            json = FileUtils.readFromFile(FileUtils.getConfigFile(UserIdMap.getCurrentUid()));
        }
        if (json != null) {
            String formatted = JsonUtil.toJsonString(INSTANCE);
            return formatted == null || !formatted.equals(json);
        }
        return true;
    }

    public static Boolean save(Boolean force) {
        if (!force) {
            if (!isModify()) {
                return true;
            }
        }
        String json = JsonUtil.toJsonString(INSTANCE);
        Log.system(TAG, "保存 config.json: " + json);
        return FileUtils.write2File(json, FileUtils.getConfigFile());
    }

    /* base */
    public static synchronized Config load() {
        Log.i(TAG, "load config");
        String json = null;
        if (FileUtils.getConfigFile(UserIdMap.getCurrentUid()).exists()) {
            json = FileUtils.readFromFile(FileUtils.getConfigFile(UserIdMap.getCurrentUid()));
        }
        try {
            JsonUtil.MAPPER.readerForUpdating(INSTANCE).readValue(json);
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.i(TAG, "配置文件格式有误，已重置配置文件");
            Log.system(TAG, "配置文件格式有误，已重置配置文件");
            try {
                JsonUtil.MAPPER.updateValue(INSTANCE, defInit());
            } catch (JsonMappingException e) {
                Log.printStackTrace(TAG, t);
            }
        }
        String formatted = JsonUtil.toJsonString(INSTANCE);
        if (formatted != null && !formatted.equals(json)) {
            Log.i(TAG, "重新格式化 config.json");
            Log.system(TAG, "重新格式化 config.json");
            FileUtils.write2File(formatted, FileUtils.getConfigFile());
        }
        init = true;
        return INSTANCE;
    }

    public void setAnimalSleepTime(String i) {
        animalSleepTime = Arrays.asList(i.split(","));
    }

    public String getAnimalSleepTime() {
        return String.join(",", animalSleepTime);
    }

    public boolean hasDoubleCardTime() {
        for (String doubleTime : doubleCardTime) {
            if (checkInTimeSpan(doubleTime))
                return true;
        }
        return false;
    }

    public static boolean hasAncientTreeWeek() {
        if (!INSTANCE.isAncientTreeOnlyWeek()) {
            return true;
        }
        SimpleDateFormat sdf_week = new SimpleDateFormat("EEEE", Locale.getDefault());
        String week = sdf_week.format(new Date());
        return "星期一".equals(week) || "星期三".equals(week) || "星期五".equals(week);
    }

    private static int tmpStepCount = -1;

    public static int tmpStepCount() {
        if (tmpStepCount >= 0) {
            return tmpStepCount;
        }
        tmpStepCount = Config.INSTANCE.getSyncStepCount();
        if (tmpStepCount > 0) {
            tmpStepCount = RandomUtils.nextInt(tmpStepCount, tmpStepCount + 2000);
            if (tmpStepCount > 100000) {
                tmpStepCount = 100000;
            }
        }
        return tmpStepCount;
    }

    private static boolean checkInTimeSpan(String timeStr) {
        if (timeStr.contains("-")) {
            String[] arr = timeStr.split("-");
            String min = arr[0];
            String max = arr[1];
            String now = TimeUtil.getTimeStr();
            return min.compareTo(now) <= 0 && max.compareTo(now) >= 0;
        } else {
            return TimeUtil.checkInTime(-INSTANCE.checkInterval, timeStr);
        }
    }

    public interface RecallAnimalType {

        int ALWAYS = 0;
        int WHEN_THIEF = 1;
        int WHEN_HUNGRY = 2;
        int NEVER = 3;

        CharSequence[] nickNames = {"始终召回", "偷吃时召回", "饥饿时召回", "不召回"};
    }

    public static boolean isInit() {
        return init;
    }

    public static void setInit(boolean init) {
        Config.init = init;
    }

    public boolean isImmediateEffect() {
        return immediateEffect;
    }

    public void setImmediateEffect(boolean immediateEffect) {
        this.immediateEffect = immediateEffect;
    }

    public boolean isRecordLog() {
        return recordLog;
    }

    public void setRecordLog(boolean recordLog) {
        this.recordLog = recordLog;
    }

    public boolean isShowToast() {
        return showToast;
    }

    public void setShowToast(boolean showToast) {
        this.showToast = showToast;
    }

    public int getToastOffsetY() {
        return toastOffsetY;
    }

    public void setToastOffsetY(int toastOffsetY) {
        this.toastOffsetY = toastOffsetY;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    public boolean isStayAwake() {
        return stayAwake;
    }

    public void setStayAwake(boolean stayAwake) {
        this.stayAwake = stayAwake;
    }

    public boolean isTimeoutRestart() {
        return timeoutRestart;
    }

    public void setTimeoutRestart(boolean timeoutRestart) {
        this.timeoutRestart = timeoutRestart;
    }

    public boolean isStartAt0() {
        return startAt0;
    }

    public void setStartAt0(boolean startAt0) {
        this.startAt0 = startAt0;
    }

    public boolean isStartAt7() {
        return startAt7;
    }

    public void setStartAt7(boolean startAt7) {
        this.startAt7 = startAt7;
    }

    public boolean isEnableOnGoing() {
        return enableOnGoing;
    }

    public void setEnableOnGoing(boolean enableOnGoing) {
        this.enableOnGoing = enableOnGoing;
    }

    public boolean isBatteryPerm() {
        return batteryPerm;
    }

    public void setBatteryPerm(boolean batteryPerm) {
        this.batteryPerm = batteryPerm;
    }

    public boolean isNewRpc() {
        return newRpc;
    }

    public void setNewRpc(boolean newRpc) {
        this.newRpc = newRpc;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isLanguageSimplifiedChinese() {
        return languageSimplifiedChinese;
    }

    public void setLanguageSimplifiedChinese(boolean languageSimplifiedChinese) {
        this.languageSimplifiedChinese = languageSimplifiedChinese;
    }

    public boolean isCollectEnergy() {
        return collectEnergy;
    }

    public void setCollectEnergy(boolean collectEnergy) {
        this.collectEnergy = collectEnergy;
    }

    public boolean isCollectWateringBubble() {
        return collectWateringBubble;
    }

    public void setCollectWateringBubble(boolean collectWateringBubble) {
        this.collectWateringBubble = collectWateringBubble;
    }

    public boolean isBatchRobEnergy() {
        return batchRobEnergy;
    }

    public void setBatchRobEnergy(boolean batchRobEnergy) {
        this.batchRobEnergy = batchRobEnergy;
    }

    public boolean isCollectProp() {
        return collectProp;
    }

    public void setCollectProp(boolean collectProp) {
        this.collectProp = collectProp;
    }

    public boolean isLimitCollect() {
        return limitCollect;
    }

    public void setLimitCollect(boolean limitCollect) {
        this.limitCollect = limitCollect;
    }

    public int getLimitCount() {
        return limitCount;
    }

    public void setLimitCount(int limitCount) {
        this.limitCount = limitCount;
    }

    public boolean isDoubleCard() {
        return doubleCard;
    }

    public void setDoubleCard(boolean doubleCard) {
        this.doubleCard = doubleCard;
    }

    public void setDoubleCardTime(List<String> doubleCardTime) {
        this.doubleCardTime = doubleCardTime;
    }

    public int getDoubleCountLimit() {
        return doubleCountLimit;
    }

    public void setDoubleCountLimit(int doubleCountLimit) {
        this.doubleCountLimit = doubleCountLimit;
    }

    public int getAdvanceTime() {
        return advanceTime;
    }

    public void setAdvanceTime(int advanceTime) {
        this.advanceTime = advanceTime;
    }

    public void setCollectInterval(int collectInterval) {
        this.collectInterval = collectInterval;
    }

    public int getCollectTimeout() {
        return collectTimeout;
    }

    public void setCollectTimeout(int collectTimeout) {
        this.collectTimeout = collectTimeout;
    }

    public int getReturnWater33() {
        return returnWater33;
    }

    public void setReturnWater33(int returnWater33) {
        this.returnWater33 = returnWater33;
    }

    public int getReturnWater18() {
        return returnWater18;
    }

    public void setReturnWater18(int returnWater18) {
        this.returnWater18 = returnWater18;
    }

    public int getReturnWater10() {
        return returnWater10;
    }

    public void setReturnWater10(int returnWater10) {
        this.returnWater10 = returnWater10;
    }

    public boolean isHelpFriendCollect() {
        return helpFriendCollect;
    }

    public void setHelpFriendCollect(boolean helpFriendCollect) {
        this.helpFriendCollect = helpFriendCollect;
    }

    public Set<String> getDontCollectSet() {
        return dontCollectSet;
    }

    public void setDontCollectSet(Set<String> dontCollectSet) {
        this.dontCollectSet = dontCollectSet;
    }

    public List<String> getDontHelpCollectList() {
        return dontHelpCollectList;
    }

    public void setDontHelpCollectList(List<String> dontHelpCollectList) {
        this.dontHelpCollectList = dontHelpCollectList;
    }

    public boolean isReceiveForestTaskAward() {
        return receiveForestTaskAward;
    }

    public void setReceiveForestTaskAward(boolean receiveForestTaskAward) {
        this.receiveForestTaskAward = receiveForestTaskAward;
    }

    public List<String> getWaterFriendList() {
        return waterFriendList;
    }

    public void setWaterFriendList(List<String> waterFriendList) {
        this.waterFriendList = waterFriendList;
    }

    public int getWaterFriendCount() {
        return waterFriendCount;
    }

    public void setWaterFriendCount(int waterFriendCount) {
        this.waterFriendCount = waterFriendCount;
    }

    public List<Integer> getWaterCountList() {
        return waterCountList;
    }

    public void setWaterCountList(List<Integer> waterCountList) {
        this.waterCountList = waterCountList;
    }

    public boolean isCooperateWater() {
        return cooperateWater;
    }

    public void setCooperateWater(boolean cooperateWater) {
        this.cooperateWater = cooperateWater;
    }

    public List<String> getCooperateWaterList() {
        return cooperateWaterList;
    }

    public void setCooperateWaterList(List<String> cooperateWaterList) {
        this.cooperateWaterList = cooperateWaterList;
    }

    public List<Integer> getCooperateWaterNumList() {
        return cooperateWaterNumList;
    }

    public void setCooperateWaterNumList(List<Integer> cooperateWaterNumList) {
        this.cooperateWaterNumList = cooperateWaterNumList;
    }

    public boolean isAncientTree() {
        return ancientTree;
    }

    public void setAncientTree(boolean ancientTree) {
        this.ancientTree = ancientTree;
    }

    public List<String> getAncientTreeCityCodeList() {
        return ancientTreeCityCodeList;
    }

    public void setAncientTreeCityCodeList(List<String> ancientTreeCityCodeList) {
        this.ancientTreeCityCodeList = ancientTreeCityCodeList;
    }

    public boolean isEnergyRain() {
        return energyRain;
    }

    public void setEnergyRain(boolean energyRain) {
        this.energyRain = energyRain;
    }

    public boolean isReserve() {
        return reserve;
    }

    public void setReserve(boolean reserve) {
        this.reserve = reserve;
    }

    public List<String> getReserveList() {
        return reserveList;
    }

    public void setReserveList(List<String> reserveList) {
        this.reserveList = reserveList;
    }

    public List<Integer> getReserveCountList() {
        return reserveCountList;
    }

    public void setReserveCountList(List<Integer> reserveCountList) {
        this.reserveCountList = reserveCountList;
    }

    public boolean isBeach() {
        return beach;
    }

    public void setBeach(boolean beach) {
        this.beach = beach;
    }

    public List<String> getBeachList() {
        return beachList;
    }

    public void setBeachList(List<String> beachList) {
        this.beachList = beachList;
    }

    public List<Integer> getBeachCountList() {
        return beachCountList;
    }

    public void setBeachCountList(List<Integer> beachCountList) {
        this.beachCountList = beachCountList;
    }

    public boolean isAncientTreeOnlyWeek() {
        return ancientTreeOnlyWeek;
    }

    public void setAncientTreeOnlyWeek(boolean ancientTreeOnlyWeek) {
        this.ancientTreeOnlyWeek = ancientTreeOnlyWeek;
    }

    public List<String> getGiveEnergyRainList() {
        return giveEnergyRainList;
    }

    public void setGiveEnergyRainList(List<String> giveEnergyRainList) {
        this.giveEnergyRainList = giveEnergyRainList;
    }

    public int getWaitWhenException() {
        return waitWhenException;
    }

    public void setWaitWhenException(int waitWhenException) {
        this.waitWhenException = waitWhenException;
    }

    public boolean isExchangeEnergyDoubleClick() {
        return exchangeEnergyDoubleClick;
    }

    public void setExchangeEnergyDoubleClick(boolean exchangeEnergyDoubleClick) {
        this.exchangeEnergyDoubleClick = exchangeEnergyDoubleClick;
    }

    public int getExchangeEnergyDoubleClickCount() {
        return exchangeEnergyDoubleClickCount;
    }

    public void setExchangeEnergyDoubleClickCount(int exchangeEnergyDoubleClickCount) {
        this.exchangeEnergyDoubleClickCount = exchangeEnergyDoubleClickCount;
    }

    public boolean isAntdodoCollect() {
        return antdodoCollect;
    }

    public void setAntdodoCollect(boolean antdodoCollect) {
        this.antdodoCollect = antdodoCollect;
    }

    public boolean isAntOcean() {
        return antOcean;
    }

    public void setAntOcean(boolean antOcean) {
        this.antOcean = antOcean;
    }

    public boolean isUserPatrol() {
        return userPatrol;
    }

    public void setUserPatrol(boolean userPatrol) {
        this.userPatrol = userPatrol;
    }

    public boolean isAnimalConsumeProp() {
        return animalConsumeProp;
    }

    public void setAnimalConsumeProp(boolean animalConsumeProp) {
        this.animalConsumeProp = animalConsumeProp;
    }

    public boolean isCollectGiftBox() {
        return collectGiftBox;
    }

    public void setCollectGiftBox(boolean collectGiftBox) {
        this.collectGiftBox = collectGiftBox;
    }

    public boolean isTotalCertCount() {
        return totalCertCount;
    }

    public void setTotalCertCount(boolean totalCertCount) {
        this.totalCertCount = totalCertCount;
    }

    public boolean isEnableFarm() {
        return enableFarm;
    }

    public void setEnableFarm(boolean enableFarm) {
        this.enableFarm = enableFarm;
    }

    public boolean isRewardFriend() {
        return rewardFriend;
    }

    public void setRewardFriend(boolean rewardFriend) {
        this.rewardFriend = rewardFriend;
    }

    public boolean isSendBackAnimal() {
        return sendBackAnimal;
    }

    public void setSendBackAnimal(boolean sendBackAnimal) {
        this.sendBackAnimal = sendBackAnimal;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }

    public List<String> getDontSendFriendList() {
        return dontSendFriendList;
    }

    public void setDontSendFriendList(List<String> dontSendFriendList) {
        this.dontSendFriendList = dontSendFriendList;
    }

    public int getRecallAnimalType() {
        return recallAnimalType;
    }

    public void setRecallAnimalType(int recallAnimalType) {
        this.recallAnimalType = recallAnimalType;
    }

    public boolean isReceiveFarmToolReward() {
        return receiveFarmToolReward;
    }

    public void setReceiveFarmToolReward(boolean receiveFarmToolReward) {
        this.receiveFarmToolReward = receiveFarmToolReward;
    }

    public boolean isRecordFarmGame() {
        return recordFarmGame;
    }

    public void setRecordFarmGame(boolean recordFarmGame) {
        this.recordFarmGame = recordFarmGame;
    }

    public boolean isKitchen() {
        return kitchen;
    }

    public void setKitchen(boolean kitchen) {
        this.kitchen = kitchen;
    }

    public boolean isUseSpecialFood() {
        return useSpecialFood;
    }

    public void setUseSpecialFood(boolean useSpecialFood) {
        this.useSpecialFood = useSpecialFood;
    }

    public boolean isUseNewEggTool() {
        return useNewEggTool;
    }

    public void setUseNewEggTool(boolean useNewEggTool) {
        this.useNewEggTool = useNewEggTool;
    }

    public boolean isHarvestProduce() {
        return harvestProduce;
    }

    public void setHarvestProduce(boolean harvestProduce) {
        this.harvestProduce = harvestProduce;
    }

    public boolean isDonation() {
        return donation;
    }

    public void setDonation(boolean donation) {
        this.donation = donation;
    }

    public boolean isAnswerQuestion() {
        return answerQuestion;
    }

    public void setAnswerQuestion(boolean answerQuestion) {
        this.answerQuestion = answerQuestion;
    }

    public boolean isReceiveFarmTaskAward() {
        return receiveFarmTaskAward;
    }

    public void setReceiveFarmTaskAward(boolean receiveFarmTaskAward) {
        this.receiveFarmTaskAward = receiveFarmTaskAward;
    }

    public boolean isFeedAnimal() {
        return feedAnimal;
    }

    public void setFeedAnimal(boolean feedAnimal) {
        this.feedAnimal = feedAnimal;
    }

    public boolean isUseAccelerateTool() {
        return useAccelerateTool;
    }

    public void setUseAccelerateTool(boolean useAccelerateTool) {
        this.useAccelerateTool = useAccelerateTool;
    }

    public List<String> getFeedFriendAnimalList() {
        return feedFriendAnimalList;
    }

    public void setFeedFriendAnimalList(List<String> feedFriendAnimalList) {
        this.feedFriendAnimalList = feedFriendAnimalList;
    }

    public List<Integer> getFeedFriendCountList() {
        return feedFriendCountList;
    }

    public void setFeedFriendCountList(List<Integer> feedFriendCountList) {
        this.feedFriendCountList = feedFriendCountList;
    }

    public void setFarmGameTime(List<String> farmGameTime) {
        this.farmGameTime = farmGameTime;
    }

    public void setAnimalSleepTime(List<String> animalSleepTime) {
        this.animalSleepTime = animalSleepTime;
    }

    public boolean isNotifyFriend() {
        return notifyFriend;
    }

    public void setNotifyFriend(boolean notifyFriend) {
        this.notifyFriend = notifyFriend;
    }

    public boolean isEnableChouchoule() {
        return enableChouchoule;
    }

    public void setEnableChouchoule(boolean enableChouchoule) {
        this.enableChouchoule = enableChouchoule;
    }

    public List<String> getDontNotifyFriendList() {
        return dontNotifyFriendList;
    }

    public void setDontNotifyFriendList(List<String> dontNotifyFriendList) {
        this.dontNotifyFriendList = dontNotifyFriendList;
    }

    public List<String> getWhoYouWantGiveTo() {
        return whoYouWantGiveTo;
    }

    public void setWhoYouWantGiveTo(List<String> whoYouWantGiveTo) {
        this.whoYouWantGiveTo = whoYouWantGiveTo;
    }

    public List<String> getSendFriendCard() {
        return sendFriendCard;
    }

    public void setSendFriendCard(List<String> sendFriendCard) {
        this.sendFriendCard = sendFriendCard;
    }

    public boolean isAcceptGift() {
        return acceptGift;
    }

    public void setAcceptGift(boolean acceptGift) {
        this.acceptGift = acceptGift;
    }

    public List<String> getVisitFriendList() {
        return visitFriendList;
    }

    public void setVisitFriendList(List<String> visitFriendList) {
        this.visitFriendList = visitFriendList;
    }

    public List<Integer> getVisitFriendCountList() {
        return visitFriendCountList;
    }

    public void setVisitFriendCountList(List<Integer> visitFriendCountList) {
        this.visitFriendCountList = visitFriendCountList;
    }

    public boolean isChickenDiary() {
        return chickenDiary;
    }

    public void setChickenDiary(boolean chickenDiary) {
        this.chickenDiary = chickenDiary;
    }

    public boolean isAntOrchard() {
        return antOrchard;
    }

    public void setAntOrchard(boolean antOrchard) {
        this.antOrchard = antOrchard;
    }

    public boolean isReceiveOrchardTaskAward() {
        return receiveOrchardTaskAward;
    }

    public void setReceiveOrchardTaskAward(boolean receiveOrchardTaskAward) {
        this.receiveOrchardTaskAward = receiveOrchardTaskAward;
    }

    public int getOrchardSpreadManureCount() {
        return orchardSpreadManureCount;
    }

    public void setOrchardSpreadManureCount(int orchardSpreadManureCount) {
        this.orchardSpreadManureCount = orchardSpreadManureCount;
    }

    public boolean isEnableStall() {
        return enableStall;
    }

    public void setEnableStall(boolean enableStall) {
        this.enableStall = enableStall;
    }

    public boolean isStallAutoClose() {
        return stallAutoClose;
    }

    public void setStallAutoClose(boolean stallAutoClose) {
        this.stallAutoClose = stallAutoClose;
    }

    public boolean isStallAutoOpen() {
        return stallAutoOpen;
    }

    public void setStallAutoOpen(boolean stallAutoOpen) {
        this.stallAutoOpen = stallAutoOpen;
    }

    public boolean isStallAutoTask() {
        return stallAutoTask;
    }

    public void setStallAutoTask(boolean stallAutoTask) {
        this.stallAutoTask = stallAutoTask;
    }

    public boolean isStallReceiveAward() {
        return stallReceiveAward;
    }

    public void setStallReceiveAward(boolean stallReceiveAward) {
        this.stallReceiveAward = stallReceiveAward;
    }

    public boolean isStallOpenType() {
        return stallOpenType;
    }

    public void setStallOpenType(boolean stallOpenType) {
        this.stallOpenType = stallOpenType;
    }

    public List<String> getStallOpenList() {
        return stallOpenList;
    }

    public void setStallOpenList(List<String> stallOpenList) {
        this.stallOpenList = stallOpenList;
    }

    public List<String> getStallWhiteList() {
        return stallWhiteList;
    }

    public void setStallWhiteList(List<String> stallWhiteList) {
        this.stallWhiteList = stallWhiteList;
    }

    public List<String> getStallBlackList() {
        return stallBlackList;
    }

    public void setStallBlackList(List<String> stallBlackList) {
        this.stallBlackList = stallBlackList;
    }

    public int getStallAllowOpenTime() {
        return stallAllowOpenTime;
    }

    public void setStallAllowOpenTime(int stallAllowOpenTime) {
        this.stallAllowOpenTime = stallAllowOpenTime;
    }

    public int getStallSelfOpenTime() {
        return stallSelfOpenTime;
    }

    public void setStallSelfOpenTime(int stallSelfOpenTime) {
        this.stallSelfOpenTime = stallSelfOpenTime;
    }

    public boolean isStallDonate() {
        return stallDonate;
    }

    public void setStallDonate(boolean stallDonate) {
        this.stallDonate = stallDonate;
    }

    public boolean isStallInviteRegister() {
        return stallInviteRegister;
    }

    public void setStallInviteRegister(boolean stallInviteRegister) {
        this.stallInviteRegister = stallInviteRegister;
    }

    public boolean isStallThrowManure() {
        return stallThrowManure;
    }

    public void setStallThrowManure(boolean stallThrowManure) {
        this.stallThrowManure = stallThrowManure;
    }

    public List<String> getStallInviteShopList() {
        return stallInviteShopList;
    }

    public void setStallInviteShopList(List<String> stallInviteShopList) {
        this.stallInviteShopList = stallInviteShopList;
    }

    public boolean isReceivePoint() {
        return receivePoint;
    }

    public void setReceivePoint(boolean receivePoint) {
        this.receivePoint = receivePoint;
    }

    public boolean isOpenTreasureBox() {
        return openTreasureBox;
    }

    public void setOpenTreasureBox(boolean openTreasureBox) {
        this.openTreasureBox = openTreasureBox;
    }

    public boolean isReceiveCoinAsset() {
        return receiveCoinAsset;
    }

    public void setReceiveCoinAsset(boolean receiveCoinAsset) {
        this.receiveCoinAsset = receiveCoinAsset;
    }

    public boolean isDonateCharityCoin() {
        return donateCharityCoin;
    }

    public void setDonateCharityCoin(boolean donateCharityCoin) {
        this.donateCharityCoin = donateCharityCoin;
    }

    public int getMinExchangeCount() {
        return minExchangeCount;
    }

    public void setMinExchangeCount(int minExchangeCount) {
        this.minExchangeCount = minExchangeCount;
    }

    public int getLatestExchangeTime() {
        return latestExchangeTime;
    }

    public void setLatestExchangeTime(int latestExchangeTime) {
        this.latestExchangeTime = latestExchangeTime;
    }

    public int getSyncStepCount() {
        return syncStepCount;
    }

    public void setSyncStepCount(int syncStepCount) {
        this.syncStepCount = syncStepCount;
    }

    public boolean isKbSignIn() {
        return kbSignIn;
    }

    public void setKbSignIn(boolean kbSignIn) {
        this.kbSignIn = kbSignIn;
    }

    public boolean isEcoLifeTick() {
        return ecoLifeTick;
    }

    public void setEcoLifeTick(boolean ecoLifeTick) {
        this.ecoLifeTick = ecoLifeTick;
    }

    public boolean isTiyubiz() {
        return tiyubiz;
    }

    public void setTiyubiz(boolean tiyubiz) {
        this.tiyubiz = tiyubiz;
    }

    public boolean isInsBlueBeanExchange() {
        return insBlueBeanExchange;
    }

    public void setInsBlueBeanExchange(boolean insBlueBeanExchange) {
        this.insBlueBeanExchange = insBlueBeanExchange;
    }

    public boolean isCollectSesame() {
        return collectSesame;
    }

    public void setCollectSesame(boolean collectSesame) {
        this.collectSesame = collectSesame;
    }

    public boolean isZcjSignIn() {
        return zcjSignIn;
    }

    public void setZcjSignIn(boolean zcjSignIn) {
        this.zcjSignIn = zcjSignIn;
    }

    public boolean isMerchantKmdk() {
        return merchantKmdk;
    }

    public void setMerchantKmdk(boolean merchantKmdk) {
        this.merchantKmdk = merchantKmdk;
    }

    public boolean isGreenFinance() {
        return greenFinance;
    }

    public void setGreenFinance(boolean greenFinance) {
        this.greenFinance = greenFinance;
    }

    public boolean isAntBookRead() {
        return antBookRead;
    }

    public void setAntBookRead(boolean antBookRead) {
        this.antBookRead = antBookRead;
    }

    public boolean isConsumeGold() {
        return consumeGold;
    }

    public void setConsumeGold(boolean consumeGold) {
        this.consumeGold = consumeGold;
    }

    public boolean isOmegakoiTown() {
        return omegakoiTown;
    }

    public void setOmegakoiTown(boolean omegakoiTown) {
        this.omegakoiTown = omegakoiTown;
    }

    public static int getTmpStepCount() {
        return tmpStepCount;
    }

    public static void setTmpStepCount(int tmpStepCount) {
        Config.tmpStepCount = tmpStepCount;
    }
}
