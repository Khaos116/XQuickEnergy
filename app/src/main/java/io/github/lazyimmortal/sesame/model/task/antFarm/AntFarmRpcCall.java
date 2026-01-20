package io.github.lazyimmortal.sesame.model.task.antFarm;

import org.json.*;

import io.github.lazyimmortal.sesame.hook.RequestManager;

/**
 * 存放GR和TK冲突的方法
 * 相同的方法会先执行AntFarmRpcCall、其次AntFarmRpcCallTk、最后AntFarmRpcCallTK
 * https://github.com/Dragon813/Sesame-GR/blob/main/app/src/main/java/io/github/lazyimmortal/sesame/model/task/antFarm/AntFarmRpcCall.java
 */
public class AntFarmRpcCall extends AntFarmRpcCallTK {
  //py脚本找出来的GR和TK不同的方法，主要是参数数量一致，但是参数顺序变了的方法，需要重写
  /*
  方法名: enterFarm                      | TK: ['userId', 'targetUserId'] | GR: ['farmId', 'userId']
  方法名: familyTaskTips                 | TK: ['animals'] | GR: ['familyAnimalsExceptUser']
  方法名: deliverSubjectRecommend        | TK: ['friendUserIdList'] | GR: ['userIds']
  方法名: deliverContentExpand           | TK: ['ariverRpcTraceId', 'eventId', 'eventName', 'memo', 'resultCode', 'sceneId', 'sceneName', 'success', 'friendUserIdList'] | GR: ['ariverRpcTraceId', 'eventId', 'eventName', 'memo', 'resultCode', 'sceneId', 'sceneName', 'success', 'userIds']
  方法名: deliverMsgSend                 | TK: ['groupId', 'friendUserIds', 'content', 'deliverId'] | GR: ['groupId', 'userIds', 'content', 'deliverId']
  方法名: familyEatTogether              | TK: ['groupId', 'friendUserIdList', 'cuisines'] | GR: ['groupId', 'cuisines', 'EatTogetherUserIds']
  */

  public static String enterFarm(String farmId, String userId) {
    return queryDrawMachineActivity_tk(userId, farmId);
  }

  public static String familyEatTogether(String groupId, JSONArray cuisines, JSONArray friendUserIdList) {
    return familyEatTogether_tk(groupId, friendUserIdList, cuisines);
  }

  public static String queryDrawMachineActivity(String otherScenes, String scene) {
    return queryDrawMachineActivity_tk(scene, otherScenes);
  }

  //=============================下面是原TK的方法，因为参数顺序/或者更新了新方法=============================//
  private static String enterFarm_tk(String userId, String targetUserId) throws JSONException {
    JSONObject args = new JSONObject();
    args.put("animalId", "");
    args.put("bizCode", "");
    args.put("gotoneScene", "");
    args.put("gotoneTemplateId", "");
    args.put("groupId", "");
    args.put("growthExtInfo", "");
    args.put("inviteUserId", "");
    args.put("masterFarmId", "");
    args.put("queryLastRecordNum", true);
    args.put("recall", false);
    args.put("requestType", "NORMAL");
    args.put("sceneCode", "ANTFARM");
    args.put("shareId", "");
    args.put("shareUniqueId", System.currentTimeMillis() + "_" + targetUserId);
    args.put("source", "ANTFOREST");
    args.put("starFarmId", "");
    args.put("subBizCode", "");
    args.put("touchRecordId", "");
    args.put("userId", userId);
    args.put("userToken", "");
    args.put("version", VERSION);
    String paras = "[" + args + "]";
    return RequestManager.requestString("com.alipay.antfarm.enterFarm", paras);
  }

  private static String familyEatTogether_tk(String groupId, JSONArray friendUserIdList, JSONArray cuisines) {
    String args = "[{\"cuisines\":" + cuisines + ",\"friendUserIds\":" + friendUserIdList + ",\"groupId\":\"" + groupId + "\",\"requestType\":\"NORMAL\",\"sceneCode\":\"ANTFARM\",\"source\":\"H5\",\"spaceType\":\"ChickFamily\"}]";
    return RequestManager.requestString("com.alipay.antfarm.familyEatTogether", args);
  }

  private static String queryDrawMachineActivity_tk(String scene, String otherScene) {
    return RequestManager.requestString(
        "com.alipay.antfarm.queryDrawMachineActivity",
        "[{\"otherScenes\":[\"" + otherScene + "\"],"
            + "\"requestType\":\"RPC\","
            + "\"scene\":\"" + scene + "\","
            + "\"sceneCode\":\"ANTFARM\","
            + "\"source\":\"antfarm_villa\"}]");
  }
}