package tkaxv7s.xposed.sesame.data.modelFieldExt;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.util.JsonUtil;

public class BooleanModelField extends ModelField {

    public BooleanModelField(String code, String name, Boolean value) {
        super(code, name, value);
        switch (code) {
        //森林 AntForestV2
        case "collectEnergy"://收集能量
        case "batchRobEnergy"://一键收取
        case "doubleCard"://双击卡 | 使用
        case "helpFriendCollect"://复活能量 | 开启
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
        case "receiveFarmToolReward"://收取道具奖励
        case "recordFarmGame"://游戏改分
        case "kitchen"://小鸡厨房
        case "useSpecialFood"://使用特殊食品
        case "harvestProduce"://收获爱心鸡蛋
        case "answerQuestion"://开启答题
        case "receiveFarmTaskAward"://收取饲料奖励
        case "feedAnimal"://喂小鸡
        case "useAccelerateTool"://使用加速卡
        case "feedFriendAnimalList"://喂好友小鸡列表
        case "acceptGift"://收麦子
        case "chickenDiary"://小鸡日记
        case "enableChouchoule"://开启小鸡抽抽乐
        case "enableHireAnimal"://雇佣小鸡
        case "enableDdrawGameCenterAward"://开宝箱
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
          setValue(true);
          break;
        default:
          break;
      }
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            value = defaultValue;
        }
        this.value = JsonUtil.parseObject(value, Boolean.class);
    }

    @Override
    public Boolean getValue() {
        return (Boolean) value;
    }

    @Override
    public View getView(Context context) {
        Switch sw = new Switch(context);
        sw.setText(getName());
        sw.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        sw.setMinHeight(150);
        sw.setMaxHeight(180);
        sw.setPaddingRelative(40, 0, 40, 0);
        sw.setChecked(getValue());
        sw.setOnClickListener(v -> setValue(((Switch) v).isChecked()));
        return sw;
    }

}
