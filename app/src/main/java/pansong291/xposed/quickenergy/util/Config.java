package pansong291.xposed.quickenergy.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    public boolean immediateEffect = true;
    public boolean recordLog;
    public boolean showToast;
    public int toastOffsetY;
    public int checkInterval;
    public boolean stayAwake;
    public boolean timeoutRestart;
    public boolean startAt0 = true;
    public boolean startAt7;
    public boolean enableOnGoing;
    public boolean batteryPerm = true;
    public boolean newRpc = true;
    public boolean debugMode = false;
    public boolean languageSimplifiedChinese;


    /* forest */
    public boolean collectEnergy;

    public boolean collectWateringBubble;

    public boolean batchRobEnergy;
    public boolean collectProp;
    public boolean limitCollect;
    public int limitCount;
    public boolean doubleCard;
    public List<String> doubleCardTime;
    public int doubleCountLimit;
    public int advanceTime;
    public int collectInterval = 350;
    public int collectTimeout;
    public int returnWater33;
    public int returnWater18;
    public int returnWater10;
    public boolean helpFriendCollect;
    public Set<String> dontCollectSet = new HashSet<>();
    public List<String> dontHelpCollectList;
    public boolean receiveForestTaskAward;
    public List<String> waterFriendList;
    public int waterFriendCount;
    public List<Integer> waterCountList;
    public boolean cooperateWater;
    public List<String> cooperateWaterList;
    public List<Integer> cooperateWaterNumList;
    public boolean ancientTree;
    public List<String> ancientTreeCityCodeList;
    public boolean energyRain;
    public boolean reserve;
    public List<String> reserveList;
    public List<Integer> reserveCountList;
    public boolean beach;
    public List<String> beachList;
    public List<Integer> beachCountList;
    public boolean ancientTreeOnlyWeek;

    public List<String> giveEnergyRainList;

    public int waitWhenException;

    public boolean exchangeEnergyDoubleClick;
    public int exchangeEnergyDoubleClickCount;
    public boolean antdodoCollect;
    public boolean antOcean;
    public boolean userPatrol;
    public boolean animalConsumeProp;
    public boolean collectGiftBox;
    public boolean totalCertCount;

    /* farm */
    public boolean enableFarm;
    public boolean rewardFriend;
    public boolean sendBackAnimal;
    public int sendType;
    public List<String> dontSendFriendList;
    public int recallAnimalType;
    public boolean receiveFarmToolReward;
    public boolean recordFarmGame;
    public boolean kitchen;
    public boolean useSpecialFood;
    public boolean useNewEggTool;
    public boolean harvestProduce;
    public boolean donation;
    public boolean answerQuestion;
    public boolean receiveFarmTaskAward;
    public boolean feedAnimal;
    public boolean useAccelerateTool;
    public List<String> feedFriendAnimalList;
    public List<Integer> feedFriendCountList;

    public List<String> farmGameTime;
    public List<String> animalSleepTime;
    public boolean notifyFriend;
    public boolean enableChouchoule = true;
    public List<String> dontNotifyFriendList;
    public List<String> whoYouWantGiveTo;
    public List<String> sendFriendCard;
    public boolean acceptGift;
    public List<String> visitFriendList;
    public List<Integer> visitFriendCountList;
    public boolean chickenDiary;
    public boolean antOrchard;
    public boolean receiveOrchardTaskAward;
    public int orchardSpreadManureCount;

    public boolean enableStall;
    public boolean stallAutoClose;
    public boolean stallAutoOpen;
    public boolean stallAutoTask;
    public boolean stallReceiveAward;
    public boolean stallOpenType;
    public List<String> stallOpenList;
    public List<String> stallWhiteList;
    public List<String> stallBlackList;
    public int stallAllowOpenTime;
    public int stallSelfOpenTime;
    public boolean stallDonate;
    public boolean stallInviteRegister;
    public boolean stallThrowManure;
    public List<String> stallInviteShopList;

    /* other */
    public boolean receivePoint;
    public boolean openTreasureBox;
    public boolean receiveCoinAsset;
    public boolean donateCharityCoin;
    public int minExchangeCount;
    public int latestExchangeTime;
    public int syncStepCount;
    public boolean kbSignIn;
    public boolean ecoLifeTick;
    public boolean tiyubiz;
    public boolean insBlueBeanExchange;
    public boolean collectSesame;
    public boolean zcjSignIn;
    public boolean merchantKmdk;
    public boolean greenFinance;
    public boolean antBookRead;
    public boolean consumeGold;
    public boolean omegakoiTown;

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
        pansong291.xposed.quickenergy.util.MyChangeUtils.useMyConfig(c);
        return c;
    }

    public interface RecallAnimalType {

        int ALWAYS = 0;
        int WHEN_THIEF = 1;
        int WHEN_HUNGRY = 2;
        int NEVER = 3;

        CharSequence[] nickNames = {"始终召回", "偷吃时召回", "饥饿时召回", "不召回"};
    }

}
