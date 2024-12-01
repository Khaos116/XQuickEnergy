package fansirsqi.xposed.sesame.task.antDodo;

import org.json.JSONArray;
import org.json.JSONObject;
import fansirsqi.xposed.sesame.model.ModelFields;
import fansirsqi.xposed.sesame.model.ModelGroup;
import fansirsqi.xposed.sesame.model.modelFieldExt.BooleanModelField;
import fansirsqi.xposed.sesame.model.modelFieldExt.ChoiceModelField;
import fansirsqi.xposed.sesame.model.modelFieldExt.SelectModelField;
import fansirsqi.xposed.sesame.task.ModelTask;
import fansirsqi.xposed.sesame.entity.AlipayUser;
import fansirsqi.xposed.sesame.task.TaskCommon;
import fansirsqi.xposed.sesame.task.antFarm.AntFarm.TaskStatus;
import fansirsqi.xposed.sesame.util.LogUtil;
import fansirsqi.xposed.sesame.util.TimeUtil;
import fansirsqi.xposed.sesame.util.UserIdMapUtil;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class AntDodo extends ModelTask {
    private static final String TAG = AntDodo.class.getSimpleName();

    @Override
    public String getName() {
        return "神奇物种";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.FOREST;
    }

    private BooleanModelField collectToFriend;
    private ChoiceModelField collectToFriendType;
    private SelectModelField collectToFriendList;
    private SelectModelField sendFriendCard;
    private BooleanModelField useProp;
    private BooleanModelField usePropCollectTimes7Days;
    private BooleanModelField usePropCollectHistoryAnimal7Days;
    private BooleanModelField usePropCollectToFriendTimes7Days;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(collectToFriend = new BooleanModelField("collectToFriend", "帮抽卡 | 开启", false));
        modelFields.addField(collectToFriendType = new ChoiceModelField("collectToFriendType", "帮抽卡 | 动作", CollectToFriendType.COLLECT, CollectToFriendType.nickNames));
        modelFields.addField(collectToFriendList = new SelectModelField("collectToFriendList", "帮抽卡 | 好友列表", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(sendFriendCard = new SelectModelField("sendFriendCard", "送卡片好友列表(当前图鉴所有卡片)", new LinkedHashSet<>(), AlipayUser::getList));
        modelFields.addField(useProp = new BooleanModelField("useProp", "使用道具 | 所有", false));
        modelFields.addField(usePropCollectTimes7Days = new BooleanModelField("usePropCollectTimes7Days", "使用道具 | 抽卡道具", false));
        modelFields.addField(usePropCollectHistoryAnimal7Days = new BooleanModelField("usePropCollectHistoryAnimal7Days", "使用道具 | 抽历史卡道具", false));
        modelFields.addField(usePropCollectToFriendTimes7Days = new BooleanModelField("usePropCollectToFriendTimes7Days", "使用道具 | 抽好友卡道具", false));
        return modelFields;
    }

    @Override
    public Boolean check() {
        return !TaskCommon.IS_ENERGY_TIME;
    }

    @Override
    public void run() {
        try {
            receiveTaskAward();
            propList();
            collect();
            if (collectToFriend.getValue()) {
                collectToFriend();
            }
        } catch (Throwable t) {
            LogUtil.runtime(TAG, "start.run err:");
            LogUtil.printStackTrace(TAG, t);
        }
    }

    /*
     * 神奇物种
     */
    private boolean lastDay(String endDate) {
        long timeStemp = System.currentTimeMillis();
        long endTimeStemp = LogUtil.timeToStamp(endDate);
        return timeStemp < endTimeStemp && (endTimeStemp - timeStemp) < 86400000L;
    }

    public boolean in8Days(String endDate) {
        long timeStemp = System.currentTimeMillis();
        long endTimeStemp = LogUtil.timeToStamp(endDate);
        return timeStemp < endTimeStemp && (endTimeStemp - timeStemp) < 691200000L;
    }

    private void collect() {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.queryAnimalStatus());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject data = jo.getJSONObject("data");
                if (data.getBoolean("collect")) {
                    LogUtil.record("神奇物种卡片今日收集完成！");
                } else {
                    collectAnimalCard();
                }
            } else {
                LogUtil.runtime(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            LogUtil.runtime(TAG, "AntDodo Collect err:");
            LogUtil.printStackTrace(TAG, t);
        }
    }

    private void collectAnimalCard() {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.homePage());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONObject data = jo.getJSONObject("data");
                JSONObject animalBook = data.getJSONObject("animalBook");
                String bookId = animalBook.getString("bookId");
                String endDate = animalBook.getString("endDate") + " 23:59:59";
                receiveTaskAward();
                if (!in8Days(endDate) || lastDay(endDate))
                    propList();
                JSONArray ja = data.getJSONArray("limit");
                int index = -1;
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    if ("DAILY_COLLECT".equals(jo.getString("actionCode"))) {
                        index = i;
                        break;
                    }
                }
                Set<String> set = sendFriendCard.getValue();
                if (index >= 0) {
                    int leftFreeQuota = jo.getInt("leftFreeQuota");
                    for (int j = 0; j < leftFreeQuota; j++) {
                        jo = new JSONObject(AntDodoRpcCall.collect());
                        if ("SUCCESS".equals(jo.getString("resultCode"))) {
                            data = jo.getJSONObject("data");
                            JSONObject animal = data.getJSONObject("animal");
                            String ecosystem = animal.getString("ecosystem");
                            String name = animal.getString("name");
                            LogUtil.forest("神奇物种🦕[" + ecosystem + "]#" + name);
                            if (!set.isEmpty()) {
                                for (String userId : set) {
                                    if (!UserIdMapUtil.getCurrentUid().equals(userId)) {
                                        int fantasticStarQuantity = animal.optInt("fantasticStarQuantity", 0);
                                        if (fantasticStarQuantity == 3) {
                                            sendCard(animal, userId);
                                        }
                                        break;
                                    }
                                }
                            }
                        } else {
                            LogUtil.runtime(TAG, jo.getString("resultDesc"));
                        }
                    }
                }
                if (!set.isEmpty()) {
                    for (String userId : set) {
                        if (!UserIdMapUtil.getCurrentUid().equals(userId)) {
                            sendAntDodoCard(bookId, userId);
                            break;
                        }
                    }
                }
            } else {
                LogUtil.runtime(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            LogUtil.runtime(TAG, "AntDodo CollectAnimalCard err:");
            LogUtil.printStackTrace(TAG, t);
        }
    }

    /**
     * 获得任务奖励
     */
    private void receiveTaskAward() {
        try {
            // 标签用于循环控制，确保在任务完成后可以继续处理
            th: do {
                String response = AntDodoRpcCall.taskList(); // 调用任务列表接口
                JSONObject jsonResponse = new JSONObject(response); // 解析响应为 JSON 对象

                // 检查响应结果码是否成功
                if ("SUCCESS".equals(jsonResponse.getString("resultCode"))) {
                    // 获取任务组信息列表
                    JSONArray taskGroupInfoList = jsonResponse.getJSONObject("data").optJSONArray("taskGroupInfoList");
                    if (taskGroupInfoList == null) return; // 如果任务组为空则返回

                    // 遍历每个任务组
                    for (int i = 0; i < taskGroupInfoList.length(); i++) {
                        JSONObject antDodoTask = taskGroupInfoList.getJSONObject(i);
                        JSONArray taskInfoList = antDodoTask.getJSONArray("taskInfoList"); // 获取任务信息列表

                        // 遍历每个任务
                        for (int j = 0; j < taskInfoList.length(); j++) {
                            JSONObject taskInfo = taskInfoList.getJSONObject(j);
                            JSONObject taskBaseInfo = taskInfo.getJSONObject("taskBaseInfo"); // 获取任务基本信息
                            JSONObject bizInfo = new JSONObject(taskBaseInfo.getString("bizInfo")); // 获取业务信息
                            String taskType = taskBaseInfo.getString("taskType"); // 获取任务类型
                            String taskTitle = bizInfo.optString("taskTitle", taskType); // 获取任务标题
                            String awardCount = bizInfo.optString("awardCount", "1"); // 获取奖励数量
                            String sceneCode = taskBaseInfo.getString("sceneCode"); // 获取场景代码
                            String taskStatus = taskBaseInfo.getString("taskStatus"); // 获取任务状态

                            // 如果任务已完成，领取任务奖励
                            if (TaskStatus.FINISHED.name().equals(taskStatus)) {
                                JSONObject joAward = new JSONObject(
                                        AntDodoRpcCall.receiveTaskAward(sceneCode, taskType)); // 领取奖励请求
                                if (joAward.optBoolean("success")) {
                                    LogUtil.forest("任务奖励🎖️[" + taskTitle + "]#" + awardCount + "个");
                                } else {
                                    LogUtil.record("领取失败，" + response); // 记录领取失败信息
                                }
                                LogUtil.runtime(joAward.toString()); // 打印奖励响应
                            }
                            // 如果任务待完成，处理特定类型的任务
                            else if (TaskStatus.TODO.name().equals(taskStatus)) {
                                if ("SEND_FRIEND_CARD".equals(taskType)) {
                                    // 尝试完成任务
                                    JSONObject joFinishTask = new JSONObject(
                                            AntDodoRpcCall.finishTask(sceneCode, taskType)); // 完成任务请求
                                    if (joFinishTask.optBoolean("success")) {
                                        LogUtil.forest("物种任务🧾️[" + taskTitle + "]");
                                        continue th; // 成功完成任务，返回外层循环
                                    } else {
                                        LogUtil.record("完成任务失败，" + taskTitle); // 记录完成任务失败信息
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LogUtil.record(jsonResponse.getString("resultDesc")); // 记录失败描述
                    LogUtil.runtime(response); // 打印响应内容
                }
                break; // 退出循环
            } while (true);
        } catch (Throwable t) {
            LogUtil.runtime(TAG, "AntDodo ReceiveTaskAward 错误:");
            LogUtil.printStackTrace(TAG, t); // 打印异常栈
        }
    }


    private void propList() {
        try {
            th:
            do {
                JSONObject jo = new JSONObject(AntDodoRpcCall.propList());
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    JSONArray propList = jo.getJSONObject("data").optJSONArray("propList");
                    if (propList == null) {
                        return;
                    }
                    for (int i = 0; i < propList.length(); i++) {
                        JSONObject prop = propList.getJSONObject(i);
                        String propType = prop.getString("propType");

                        boolean usePropType = isUsePropType(propType);
                        if (!usePropType) {
                            continue;
                        }
                        JSONArray propIdList = prop.getJSONArray("propIdList");
                        String propId = propIdList.getString(0);
                        String propName = prop.getJSONObject("propConfig").getString("propName");
                        int holdsNum = prop.optInt("holdsNum", 0);
                        jo = new JSONObject(AntDodoRpcCall.consumeProp(propId, propType));
                        TimeUtil.sleep(300);
                        if (!"SUCCESS".equals(jo.getString("resultCode"))) {
                            LogUtil.record(jo.getString("resultDesc"));
                            LogUtil.runtime(jo.toString());
                            continue;
                        }
                        if ("COLLECT_TIMES_7_DAYS".equals(propType)) {
                            JSONObject useResult = jo.getJSONObject("data").getJSONObject("useResult");
                            JSONObject animal = useResult.getJSONObject("animal");
                            String ecosystem = animal.getString("ecosystem");
                            String name = animal.getString("name");
                            LogUtil.forest("使用道具🎭[" + propName + "]#" + ecosystem + "-" + name);
                            Set<String> map = sendFriendCard.getValue();
                            for (String userId : map) {
                                if (!UserIdMapUtil.getCurrentUid().equals(userId)) {
                                    int fantasticStarQuantity = animal.optInt("fantasticStarQuantity", 0);
                                    if (fantasticStarQuantity == 3) {
                                        sendCard(animal, userId);
                                    }
                                    break;
                                }
                            }
                        } else {
                            LogUtil.forest("使用道具🎭[" + propName + "]");
                        }
                        if (holdsNum > 1) {
                            continue th;
                        }
                    }
                }
                break;
            } while (true);
        } catch (Throwable th) {
            LogUtil.runtime(TAG, "AntDodo PropList err:");
            LogUtil.printStackTrace(TAG, th);
        }
    }

    /**
     * 判断是否存在使用道具类型
     * @param propType 道具类型
     * @return 是否使用
     */
    private boolean isUsePropType(String propType) {
        boolean usePropType = useProp.getValue();
        switch (propType) {
            case "COLLECT_TIMES_7_DAYS":
                usePropType = usePropType || usePropCollectTimes7Days.getValue();
                break;
            case "COLLECT_HISTORY_ANIMAL_7_DAYS":
                usePropType = usePropType || usePropCollectHistoryAnimal7Days.getValue();
                break;
            case "COLLECT_TO_FRIEND_TIMES_7_DAYS":
                usePropType = usePropType || usePropCollectToFriendTimes7Days.getValue();
                break;
        }
        return usePropType;
    }

    /**
     * 发送神奇物种卡片
     * @param bookId 卡片图鉴ID
     * @param targetUser 目标用户ID
     */
    private void sendAntDodoCard(String bookId, String targetUser) {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.queryBookInfo(bookId));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                JSONArray animalForUserList = jo.getJSONObject("data").optJSONArray("animalForUserList");
                for (int i = 0; i < Objects.requireNonNull(animalForUserList).length(); i++) {
                    JSONObject animalForUser = animalForUserList.getJSONObject(i);
                    int count = animalForUser.getJSONObject("collectDetail").optInt("count");
                    if (count <= 0)
                        continue;
                    JSONObject animal = animalForUser.getJSONObject("animal");
                    for (int j = 0; j < count; j++) {
                        sendCard(animal, targetUser);
                        TimeUtil.sleep(500L);
                    }
                }
            }
        } catch (Throwable th) {
            LogUtil.runtime(TAG, "AntDodo SendAntDodoCard err:");
            LogUtil.printStackTrace(TAG, th);
        }
    }

    private void sendCard(JSONObject animal, String targetUser) {
        try {
            String animalId = animal.getString("animalId");
            String ecosystem = animal.getString("ecosystem");
            String name = animal.getString("name");
            JSONObject jo = new JSONObject(AntDodoRpcCall.social(animalId, targetUser));
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                LogUtil.forest("赠送卡片🦕[" + UserIdMapUtil.getMaskName(targetUser) + "]#" + ecosystem + "-" + name);
            } else {
                LogUtil.runtime(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable th) {
            LogUtil.runtime(TAG, "AntDodo SendCard err:");
            LogUtil.printStackTrace(TAG, th);
        }
    }

    private void collectToFriend() {
        try {
            JSONObject jo = new JSONObject(AntDodoRpcCall.queryFriend());
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                int count = 0;
                JSONArray limitList = jo.getJSONObject("data").getJSONObject("extend").getJSONArray("limit");
                for (int i = 0; i < limitList.length(); i++) {
                    JSONObject limit = limitList.getJSONObject(i);
                    if (limit.getString("actionCode").equals("COLLECT_TO_FRIEND")) {
                        if (limit.getLong("startTime") > System.currentTimeMillis()) {
                            return;
                        }
                        count = limit.getInt("leftLimit");
                        break;
                    }

                }
                JSONArray friendList = jo.getJSONObject("data").getJSONArray("friends");
                for (int i = 0; i < friendList.length() && count > 0; i++) {
                    JSONObject friend = friendList.getJSONObject(i);
                    if (friend.getBoolean("dailyCollect")) {
                        continue;
                    }
                    String useId = friend.getString("userId");
                    boolean isCollectToFriend = collectToFriendList.getValue().contains(useId);
                    if (collectToFriendType.getValue() == CollectToFriendType.DONT_COLLECT) {
                        isCollectToFriend = !isCollectToFriend;
                    }
                    if (!isCollectToFriend) {
                        continue;
                    }
                    jo = new JSONObject(AntDodoRpcCall.collect(useId));
                    if ("SUCCESS".equals(jo.getString("resultCode"))) {
                        String ecosystem = jo.getJSONObject("data").getJSONObject("animal").getString("ecosystem");
                        String name = jo.getJSONObject("data").getJSONObject("animal").getString("name");
                        String userName = UserIdMapUtil.getMaskName(useId);
                        LogUtil.forest("神奇物种🦕帮好友[" + userName + "]抽卡[" + ecosystem + "]#" + name);
                        count--;
                    } else {
                        LogUtil.runtime(TAG, jo.getString("resultDesc"));
                    }
                }

            } else {
                LogUtil.runtime(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            LogUtil.runtime(TAG, "AntDodo CollectHelpFriend err:");
            LogUtil.printStackTrace(TAG, t);
        }
    }

    public interface CollectToFriendType {

        int COLLECT = 0;
        int DONT_COLLECT = 1;

        String[] nickNames = {"选中帮抽卡", "选中不帮抽卡"};

    }
}