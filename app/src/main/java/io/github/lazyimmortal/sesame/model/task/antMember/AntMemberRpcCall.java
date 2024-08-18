package io.github.lazyimmortal.sesame.model.task.antMember;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.github.lazyimmortal.sesame.entity.AlipayVersion;
import io.github.lazyimmortal.sesame.entity.RpcEntity;
import io.github.lazyimmortal.sesame.hook.ApplicationHook;
import io.github.lazyimmortal.sesame.util.Log;
import io.github.lazyimmortal.sesame.util.RandomUtil;

public class AntMemberRpcCall {

    private static String getUniqueId() {
        return String.valueOf(System.currentTimeMillis()) + RandomUtil.nextLong();
    }

    public static Boolean check() {
        RpcEntity rpcEntity = ApplicationHook.requestObject("alipay.antmember.biz.rpc.member.h5.queryPointCert",
                "[{\"page\":" + 1 + ",\"pageSize\":" + 8 + "}]", 1, 0);
        return rpcEntity != null && !rpcEntity.getHasError();
    }

    /* ant member point */
    public static String queryPointCert(int page, int pageSize) {
        String args1 = "[{\"page\":" + page + ",\"pageSize\":" + pageSize + "}]";
        return ApplicationHook.requestString("alipay.antmember.biz.rpc.member.h5.queryPointCert", args1);
    }

    public static String receivePointByUser(String certId) {
        String args1 = "[{\"certId\":" + certId + "}]";
        return ApplicationHook.requestString("alipay.antmember.biz.rpc.member.h5.receivePointByUser", args1);
    }

    public static String queryMemberSigninCalendar() {
        return ApplicationHook.requestString("com.alipay.amic.biz.rpc.signin.h5.queryMemberSigninCalendar",
                "[{\"autoSignIn\":true,\"invitorUserId\":\"\",\"sceneCode\":\"QUERY\"}]");
    }

    /* 商家服务 */
    public static String transcodeCheck() {
        return ApplicationHook.requestString("alipay.mrchservbase.mrchbusiness.sign.transcode.check",
                "[{}]");
    }

    public static String zcjSignInQuery() {
        return ApplicationHook.requestString("alipay.mrchservbase.zcj.view.invoke",
                "[{\"compId\":\"ZCJ_SIGN_IN_QUERY\"}]");
    }

    public static String zcjSignInExecute() {
        return ApplicationHook.requestString("alipay.mrchservbase.zcj.view.invoke",
                "[{\"compId\":\"ZCJ_SIGN_IN_EXECUTE\"}]");
    }

    public static String taskListQuery() {
        return ApplicationHook.requestString("alipay.mrchservbase.zcj.taskList.query",
                "[{\"compId\":\"ZCJ_TASK_LIST\",\"params\":{\"activityCode\":\"ZCJ\",\"clientVersion\":\"10.3.36\",\"extInfo\":{},\"platform\":\"Android\",\"underTakeTaskCode\":\"\"}}]");
    }

    public static String queryActivity() {
        return ApplicationHook.requestString("alipay.merchant.kmdk.query.activity",
                "[{\"scene\":\"activityCenter\"}]");
    }

    public static String signIn(String activityNo) {
        return ApplicationHook.requestString("alipay.merchant.kmdk.signIn",
                "[{\"activityNo\":\"" + activityNo + "\"}]");
    }

    public static String signUp(String activityNo) {
        return ApplicationHook.requestString("alipay.merchant.kmdk.signUp",
                "[{\"activityNo\":\"" + activityNo + "\"}]");
    }

    /* 商家服务任务 */

    public static String taskFinish(String bizId) {
        return ApplicationHook.requestString("com.alipay.adtask.biz.mobilegw.service.task.finish",
                "[{\"bizId\":\"" + bizId + "\"}]");
    }

    public static String taskReceive(String taskCode) {
        return ApplicationHook.requestString("alipay.mrchservbase.sqyj.task.receive",
                "[{\"compId\":\"ZTS_TASK_RECEIVE\",\"extInfo\":{\"taskCode\":\"" + taskCode + "\"}}]");
    }

    public static String actioncode(String actionCode) {
        return ApplicationHook.requestString("alipay.mrchservbase.task.query.by.actioncode",
                "[{\"actionCode\":\"" + actionCode + "\"}]");
    }

    public static String produce(String actionCode) {
        return ApplicationHook.requestString("alipay.mrchservbase.biz.task.action.produce",
                "[{\"actionCode\":\"" + actionCode + "\"}]");
    }

    public static String ballReceive(String ballIds) {
        return ApplicationHook.requestString("alipay.mrchservbase.mrchpoint.ball.receive",
                "[{\"ballIds\":[\"" + ballIds
                        + "\"],\"channel\":\"MRCH_SELF\",\"outBizNo\":\"" + getUniqueId() + "\"}]");
    }

    /* 会员任务 */
    public static String signPageTaskList() {
        return ApplicationHook.requestString("alipay.antmember.biz.rpc.membertask.h5.signPageTaskList",
                "[{\"sourceBusiness\":\"antmember\",\"spaceCode\":\"ant_member_xlight_task\"}]");
    }

    public static String applyTask(String darwinName, Long taskConfigId) {
        return ApplicationHook.requestString("alipay.antmember.biz.rpc.membertask.h5.applyTask",
                "[{\"darwinExpParams\":{\"darwinName\":\"" + darwinName
                        + "\"},\"sourcePassMap\":{\"innerSource\":\"\",\"source\":\"myTab\",\"unid\":\"\"},\"taskConfigId\":"
                        + taskConfigId + "}]");
    }

    public static String executeTask(String bizParam, String bizSubType) {
        return ApplicationHook.requestString("alipay.antmember.biz.rpc.membertask.h5.executeTask",
                "[{\"bizOutNo\":\"" + (System.currentTimeMillis() - 16000L) + "\",\"bizParam\":\""
                        + bizParam + "\",\"bizSubType\":\"" + bizSubType + "\",\"bizType\":\"BROWSE\"}]");
    }

    public static String queryAllStatusTaskList() {
        return ApplicationHook.requestString("alipay.antmember.biz.rpc.membertask.h5.signPageTaskList",
                "[{\"sourceBusiness\":\"signInAd\",\"sourcePassMap\":{\"innerSource\":\"\",\"source\":\"myTab\",\"unid\":\"\"}}]");
    }

    /**
     * 黄金票收取
     *
     * @param str signInfo
     * @return 结果
     */
    public static String goldBillCollect(String str) {
        return ApplicationHook.requestString("com.alipay.wealthgoldtwa.goldbill.v2.index.collect",
                "[{" + str + "\"trigger\":\"Y\"}]");
    }

    /**
     * 游戏中心签到查询
     */
    public static String querySignInBall() {
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.v3.querySignInBall",
                "[{\"source\":\"ch_appcenter__chsub_9patch\"}]");
    }

    /**
     * 游戏中心签到
     */
    public static String continueSignIn() {
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.continueSignIn",
                "[{\"sceneId\":\"GAME_CENTER\",\"signType\":\"NORMAL_SIGN\",\"source\":\"ch_appcenter__chsub_9patch\"}]");
    }

    /**
     * 游戏中心查询待领取乐豆列表
     */
    public static String queryPointBallList() {
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.v3.queryPointBallList",
                "[{\"source\":\"ch_appcenter__chsub_9patch\"}]");
    }

    /**
     * 游戏中心全部领取
     */
    public static String batchReceivePointBall() {
        return ApplicationHook.requestString("com.alipay.gamecenteruprod.biz.rpc.v3.batchReceivePointBall",
                "[{}]");
    }

    /**
     * 查询可收取的芝麻粒
     *
     * @return 结果
     */
    public static String queryCreditFeedback() {
        return ApplicationHook.requestString(
                "com.antgroup.zmxy.zmcustprod.biz.rpc.home.creditaccumulate.api.CreditAccumulateRpcManager.queryCreditFeedback",
                "[{\"queryPotential\":false,\"size\":20,\"status\":\"UNCLAIMED\"}]");
    }

    /**
     * 芝麻信用首页
     *
     * @return 结果
     */
    public static String queryHome() {
        return ApplicationHook.requestString("com.antgroup.zmxy.zmcustprod.biz.rpc.home.api.HomeV6RpcManager.queryHome",
                "[{\"miniZmGrayInside\":\"\"}]");
    }

    /**
     * 收取芝麻粒
     *
     * @param creditFeedbackId creditFeedbackId
     * @return 结果
     */
    public static String collectCreditFeedback(String creditFeedbackId) {
        return ApplicationHook.requestString(
                "com.antgroup.zmxy.zmcustprod.biz.rpc.home.creditaccumulate.api.CreditAccumulateRpcManager.collectCreditFeedback",
                "[{\"collectAll\":false,\"creditFeedbackId\":\"" + creditFeedbackId + "\",\"status\":\"UNCLAIMED\"}]");
    }

    /**
     * 查询生活记录
     *
     * @return 结果
     */
    public static String promiseQueryHome() {
        return ApplicationHook.requestString("com.antgroup.zmxy.zmmemberop.biz.rpc.promise.PromiseRpcManager.queryHome", null);
    }

    /**
     * 查询生活记录明细
     *
     * @param recordId recordId
     * @return 结果
     */
    public static String promiseQueryDetail(String recordId) {
        return ApplicationHook.requestString("com.antgroup.zmxy.zmmemberop.biz.rpc.promise.PromiseRpcManager.queryDetail",
                "[{\"recordId\":\"" + recordId + "\"}]");
    }

    /**
     * 生活记录加入新纪录
     *
     * @param data data
     * @return 结果
     */
    public static String promiseJoin(String data) {
        return ApplicationHook.requestString("com.antgroup.zmxy.zmmemberop.biz.rpc.promise.PromiseRpcManager.join",
                "[" + data + "]");
    }

    /**
     * 查询待领取的保障金
     *
     * @return 结果
     */
    public static String queryMultiSceneWaitToGainList() {
        return ApplicationHook.requestString("com.alipay.insgiftbff.insgiftMain.queryMultiSceneWaitToGainList",
                "[{\"entrance\":\"jkj_zhima_dairy66\",\"eventToWaitParamDTO\":{\"giftProdCode\":\"GIFT_UNIVERSAL_COVERAGE\"," +
                        "\"rightNoList\":[\"UNIVERSAL_ACCIDENT\",\"UNIVERSAL_HOSPITAL\",\"UNIVERSAL_OUTPATIENT\"," +
                        "\"UNIVERSAL_SERIOUSNESS\",\"UNIVERSAL_WEALTH\",\"UNIVERSAL_TRANS\",\"UNIVERSAL_FRAUD_LIABILITY\"]}," +
                        "\"helpChildParamDTO\":{\"giftProdCode\":\"GIFT_HEALTH_GOLD_CHILD\",\"rightNoList\":[\"UNIVERSAL_ACCIDENT\"," +
                        "\"UNIVERSAL_HOSPITAL\",\"UNIVERSAL_OUTPATIENT\",\"UNIVERSAL_SERIOUSNESS\",\"UNIVERSAL_WEALTH\"," +
                        "\"UNIVERSAL_TRANS\",\"UNIVERSAL_FRAUD_LIABILITY\"]},\"priorityChannelParamDTO\":{\"giftProdCode\":" +
                        "\"GIFT_UNIVERSAL_COVERAGE\",\"rightNoList\":[\"UNIVERSAL_ACCIDENT\",\"UNIVERSAL_HOSPITAL\"," +
                        "\"UNIVERSAL_OUTPATIENT\",\"UNIVERSAL_SERIOUSNESS\",\"UNIVERSAL_WEALTH\",\"UNIVERSAL_TRANS\"," +
                        "\"UNIVERSAL_FRAUD_LIABILITY\"]},\"signInParamDTO\":{\"giftProdCode\":\"GIFT_UNIVERSAL_COVERAGE\"," +
                        "\"rightNoList\":[\"UNIVERSAL_ACCIDENT\",\"UNIVERSAL_HOSPITAL\",\"UNIVERSAL_OUTPATIENT\"," +
                        "\"UNIVERSAL_SERIOUSNESS\",\"UNIVERSAL_WEALTH\",\"UNIVERSAL_TRANS\",\"UNIVERSAL_FRAUD_LIABILITY\"]}}]");
    }

    /**
     * 领取保障金
     *
     * @param jsonObject jsonObject
     * @return 结果
     */
    public static String gainMyAndFamilySumInsured(JSONObject jsonObject) throws JSONException {
        jsonObject.put("disabled", false);
        jsonObject.put("entrance", "jkj_zhima_dairy66");
        return ApplicationHook.requestString("com.alipay.insgiftbff.insgiftMain.gainMyAndFamilySumInsured",
                "[" + jsonObject + "]");
    }

    // 安心豆
    public static String querySignInProcess(String appletId, String scene) {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.bean.querySignInProcess",
                "[{\"appletId\":\"" + appletId + "\",\"scene\":\"" + scene + "\"}]");
    }

    public static String signInTrigger(String appletId, String scene) {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.bean.signInTrigger",
                "[{\"appletId\":\"" + appletId + "\",\"scene\":\"" + scene + "\"}]");
    }

    public static String beanExchangeDetail(String itemId) {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.onestop.planTrigger",
                "[{\"extParams\":{\"itemId\":\"" + itemId + "\"},"
                        + "\"planCode\":\"bluebean_onestop\",\"planOperateCode\":\"exchangeDetail\"}]");
    }

    public static String beanExchange(String itemId, int pointAmount) {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.onestop.planTrigger",
                "[{\"extParams\":{\"itemId\":\"" + itemId + "\",\"pointAmount\":\"" + Integer.toString(pointAmount) + "\"},"
                        + "\"planCode\":\"bluebean_onestop\",\"planOperateCode\":\"exchange\"}]");
    }

    public static String queryUserAccountInfo(String pointProdCode) {
        return ApplicationHook.requestString("com.alipay.insmarketingbff.point.queryUserAccountInfo",
                "[{\"channel\":\"HiChat\",\"pointProdCode\":\"" + pointProdCode + "\",\"pointUnitType\":\"COUNT\"}]");
    }

    /**
     * 查询会员积分兑换福利列表方法1
     * @param userId userId
     * @param naviCode 导航分类码
     *                 皮肤："bb82b"、0元起："全积分"、影音："13"
     * @return 分类下商品列表
     */
    public static String queryDeliveryZoneDetail(String userId, String naviCode) {
        String args = "[\n" +
                "    {\n" +
                "        \"cityCode\": \"\",\n" +
                "        \"deliveryId\": \"94000SR2023102305988003\",\n" +
                "        \"pageNum\": 1,\n" +
                "        \"pageSize\": 50,\n" +
                "        \"sourcePassMap\": {\n" +
                "            \"innerSource\": \"\",\n" +
                "            \"source\": \"myTab\",\n" +
                "            \"unid\": \"\"\n" +
                "        },\n" +
                "        \"topIdList\": [],\n" +
                "        \"uniqueId\": \"" + System.currentTimeMillis() + naviCode + "0and99999999INTELLIGENT_SORT" + userId + "\"\n" +
                "    }\n" +
                "]";
        return ApplicationHook.requestString("com.alipay.alipaymember.biz.rpc.config.h5.queryDeliveryZoneDetail", args);
    }

    /**
     * 查询会员积分兑换福利列表方法2
     * @param userId userId
     * @param naviCode 导航分类码
     *                 特色："14"、出行："1"、美食："11"、日用："12"、上新：""
     * @return 分类下商品列表
     */
    public static String queryIndexNaviBenefitFlowV2(String userId, String naviCode) {
        String sortStrategy = "INTELLIGENT_SORT";
        String upperPoint = "99999999";
        String uniqueId = System.currentTimeMillis() + naviCode + "0and" + upperPoint + sortStrategy + userId;
        String args = "[\n" +
                "        {\n" +
                "            \"adCopyId\": \"\",\n" +
                "            \"benefitFlowSource\": \"REC\",\n" +
                "            \"cityCode\": \"\",\n" +
                "            \"excludeIds\": \"\",\n" +
                "            \"exposeChannel\": \"antmember\",\n" +
                "            \"fastTag\": \"\",\n" +
                "            \"lowerPoint\": 0,\n" +
                "            \"naviCode\": \"" + naviCode + "\",\n" +
                "            \"pageNum\": 1,\n" +
                "            \"pageSize\": 50,\n" +
                "            \"requestSourceInfo\": \"-|feeds\",\n" +
                "            \"sortStrategy\": \"" + sortStrategy + "\",\n" +
                "            \"sourcePassMap\": {\n" +
                "                \"innerSource\": \"\",\n" +
                "                \"source\": \"myTab\",\n" +
                "                \"unid\": \"\"\n" +
                "            },\n" +
                "            \"stickyIdList\": [],\n" +
                "            \"tagCodeIdx\": -1,\n" +
                "            \"uniqueId\": \"" + uniqueId + "\",\n" +
                "            \"upperPoint\": " + upperPoint + ",\n" +
                "            \"withPointRange\": false\n" +
                "        }\n" +
                "    ]";
        return ApplicationHook.requestString("com.alipay.alipaymember.biz.rpc.config.h5.queryIndexNaviBenefitFlowV2", args);
    }

    /**
     * 会员积分兑换福利
     * @param benefitId benefitId
     * @param itemId itemId
     * @return 结果
     */
    public static String exchangeBenefit(String benefitId, String itemId) {
        String args = "[\n" +
                "        {\n" +
                "            \"benefitId\": \"" + benefitId + "\",\n" +
                "            \"cityCode\": \"\",\n" +
                "            \"exchangeType\": \"POINT_PAY\",\n" +
                "            \"itemId\": \"" + itemId + "\",\n" +
                "            \"miniAppId\": \"\",\n" +
                "            \"orderSource\": \"\",\n" +
                "            \"requestId\": \"requestId" + System.currentTimeMillis() +"\",\n" +
                "            \"requestSourceInfo\": \"\",\n" +
                "            \"sourcePassMap\": {\n" +
                "                \"alipayClientVersion\": \"10.6.10.9100\",\n" +
                "                \"innerSource\": \"\",\n" +
                "                \"mobileOsType\": \"Android\",\n" +
                "                \"source\": \"\",\n" +
                "                \"unid\": \"\"\n" +
                "            },\n" +
                "            \"userOutAccount\": \"\"\n" +
                "        }\n" +
                "    ]";
        return ApplicationHook.requestString("com.alipay.alipaymember.biz.rpc.exchange.h5.exchangeBenefit", args);
    }

    public static String queryDeliveryZoneDetail(String userId) {
        String args1 = "[\n" +
                "    {\n" +
                "        \"cityCode\": \"450100\",\n" +
                "        \"deliveryId\": \"94000SR2023102305988003\",\n" +
                "        \"pageNum\": 1,\n" +
                "        \"pageSize\": 18,\n" +
                "        \"sourcePassMap\": {\n" +
                "            \"innerSource\": \"\",\n" +
                "            \"source\": \"ch_appcenter__chsub_9patch\",\n" +
                "            \"unid\": \"\"\n" +
                "        },\n" +
                "        \"topIdList\": [],\n" +
                "        \"uniqueId\": \"" + System.currentTimeMillis() + "全积分0and99999999INTELLIGENT_SORT" + userId + "\"\n" +
                "    }\n" +
                "]";


        return ApplicationHook.requestString("com.alipay.alipaymember.biz.rpc.config.h5.queryDeliveryZoneDetail", args1);
    }

    public static String exchangeBenefit(String benefitId, String itemId, String userId, int index) {
        AlipayVersion alipayVersion = ApplicationHook.getAlipayVersion();
        String args1 = "[\n" +
                "    {\n" +
                "        \"benefitId\": \"" + benefitId + "\",\n" +
                "        \"cityCode\": \"450100\",\n" +
                "        \"exchangeType\": \"POINT_PAY\",\n" +
                "        \"itemId\": \"" + itemId + "\",\n" +
                "        \"miniAppId\": \"\",\n" +
                "        \"orderSource\": \"\",\n" +
                "        \"requestId\": \"requestId" + System.currentTimeMillis() + "\",\n" +
                "        \"requestSourceInfo\": \"SID:" + System.currentTimeMillis() + "全积分0and99999999INTELLIGENT_SORT" + userId + "|" + index + "\",\n" +
                "        \"sourcePassMap\": {\n" +
                "            \"alipayClientVersion\": \"" + alipayVersion.getVersionString() + "\",\n" +
                "            \"bid\": \"\",\n" +
                "            \"feedsIndex\": \"" + index + "\",\n" +
                "            \"innerSource\": \"feeds\",\n" +
                "            \"isCpc\": \"\",\n" +
                "            \"mobileOsType\": \"Android\",\n" +
                "            \"source\": \"ch_appcenter__chsub_9patch\",\n" +
                "            \"unid\": \"" + generateFormattedUUID() + "\",\n" +
                "            \"uniqueId\": \"" + System.currentTimeMillis() + "全积分0and99999999INTELLIGENT_SORT" + userId + "\"\n" +
                "        },\n" +
                "        \"userOutAccount\": \"\"\n" +
                "    }\n" +
                "]";
        return ApplicationHook.requestString("com.alipay.alipaymember.biz.rpc.exchange.h5.exchangeBenefit", args1);
    }

    public static String generateFormattedUUID() {
        // 生成随机的UUID
        UUID uuid = UUID.randomUUID();

        // 格式化UUID为指定的格式
        String formattedUUID = String.format("%s-%s-%s-%s-%s",
                uuid.toString().substring(0, 8),
                uuid.toString().substring(8, 12),
                uuid.toString().substring(12, 16),
                uuid.toString().substring(16, 20),
                uuid.toString().substring(20));

        return formattedUUID;
    }

    // 我的快递任务
    public static String queryRecommendTask() {
        String args1 = "[{\"consultAccessFlag\":true,\"extInfo\":{\"componentCode\":\"musi_test\"},\"taskCenInfo\":\"MZVPQ0DScvD6NjaPJzk8iCCWtq%2FRt4kh\"}]";
        return ApplicationHook.requestString("alipay.promoprod.task.listQuery", args1);
    }

    // 积分、肥料
    public static String trigger(String appletId) {
        String args1 = "[{\"appletId\":\"" + appletId + "\",\"taskCenInfo\":\"MZVPQ0DScvD6NjaPJzk8iNRgSSvWpCuA\",\"stageCode\":\"send\"}]";
        return ApplicationHook.requestString("alipay.promoprod.applet.trigger", args1);
    }

    // 森林活力值
    public static String queryforestHomePage() {
        String args1 = "[{\"activityParam\":{},\"configVersionMap\":{\"wateringBubbleConfig\":\"0\"},\"skipWhackMole\":false,\"source\":\"kuaidivitality\",\"version\":\"20240606\"}]";
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryHomePage", args1);
    }

    public static String forestTask() {
        String args1 = "[{\"extend\":{\"firstTaskType\":\"KUAIDI_VITALITY\"},\"fromAct\":\"home_task_list\",\"source\":\"kuaidivitality\",\"version\":\"20240105\"}]";
        return ApplicationHook.requestString("alipay.antforest.forest.h5.queryTaskList", args1);
    }

    public static String forestreceiveTaskAward() {
        String args1 = "[{\"ignoreLimit\":false,\"requestType\":\"H5\",\"sceneCode\":\"ANTFOREST_VITALITY_TASK\",\"source\":\"ANTFOREST\",\"taskType\":\"KUAIDI_VITALITY\"}]";
        return ApplicationHook.requestString("com.alipay.antiep.receiveTaskAward", args1);
    }

    // 海洋碎片
    public static String queryoceanHomePage() {
        String args1 = "[{\"firstTaskType\":\"DAOLIU_WODEKUAIDIQUANYI\",\"source\":\"wodekuaidiquanyi\",\"uniqueId\":\"" + getUniqueId() + "\",\"version\":\"20240115\"}]";
        return ApplicationHook.requestString("alipay.antocean.ocean.h5.queryHomePage", args1);
    }

    public static String oceanTask() {
        String args1 = "[{\"extend\":{\"firstTaskType\":\"DAOLIU_WODEKUAIDIQUANYI\"},\"fromAct\":\"dynamic_task\",\"sceneCode\":\"ANTOCEAN_TASK\",\"source\":\"wodekuaidiquanyi\",\"uniqueId\":\"" +
                getUniqueId() + "\",\"version\":\"20240115\"}]";
        return ApplicationHook.requestString("alipay.antocean.ocean.h5.queryTaskList", args1);
    }

    public static String oceanreceiveTaskAward() {
        String args1 = "[{\"ignoreLimit\":false,\"requestType\":\"RPC\",\"sceneCode\":\"ANTOCEAN_TASK\",\"source\":\"ANT_FOREST\",\"taskType\":\"DAOLIU_WODEKUAIDIQUANYI\",\"uniqueId\":\"" + getUniqueId() + "\"}]";
        return ApplicationHook.requestString("com.alipay.antiep.receiveTaskAward", args1);
    }

    // 普通任务
    public static String queryOrdinaryTask() {
        String args1 = "[{\"consultAccessFlag\":true,\"taskCenInfo\":\"MZVPQ0DScvD6NjaPJzk8iNRgSSvWpCuA\"}]";
        return ApplicationHook.requestString("alipay.promoprod.task.listQuery", args1);
    }

    public static String signuptrigger(String appletId) {
        String args1 = "[{\"appletId\":\"" + appletId + "\",\"taskCenInfo\":\"MZVPQ0DScvD6NjaPJzk8iNRgSSvWpCuA\",\"stageCode\":\"signup\"}]";
        return ApplicationHook.requestString("alipay.promoprod.applet.trigger", args1);
    }

    public static String sendtrigger(String appletId) {
        String args1 = "[{\"appletId\":\"" + appletId + "\",\"taskCenInfo\":\"MZVPQ0DScvD6NjaPJzk8iNRgSSvWpCuA\",\"stageCode\":\"send\"}]";
        return ApplicationHook.requestString("alipay.promoprod.applet.trigger", args1);
    }

    // 消费金签到
    public static String signinCalendar() {
        return ApplicationHook.requestString("alipay.mobile.ipsponsorprod.consume.gold.task.signin.calendar",
                "[{}]");
    }

    public static String openBoxAward() {
        return ApplicationHook.requestString("alipay.mobile.ipsponsorprod.consume.gold.task.openBoxAward",
                "[{\"actionAwardDetails\":[{\"actionType\":\"date_sign_start\"}],\"bizType\":\"CONSUME_GOLD\",\"boxType\":\"CONSUME_GOLD_SIGN_DATE\",\"clientVersion\":\"6.3.0\",\"timeScaleType\":0,\"userType\":\"new\"}]");
    }
}