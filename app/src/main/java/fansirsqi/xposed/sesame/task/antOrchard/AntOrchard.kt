package fansirsqi.xposed.sesame.task.antOrchard

import android.util.Base64
import fansirsqi.xposed.sesame.data.Status
import fansirsqi.xposed.sesame.data.StatusFlags
import fansirsqi.xposed.sesame.entity.AlipayUser
import fansirsqi.xposed.sesame.entity.MapperEntity
import fansirsqi.xposed.sesame.hook.internal.SecurityBodyHelper
import fansirsqi.xposed.sesame.model.ModelFields
import fansirsqi.xposed.sesame.model.ModelGroup
import fansirsqi.xposed.sesame.model.modelFieldExt.BooleanModelField
import fansirsqi.xposed.sesame.model.modelFieldExt.IntegerModelField
import fansirsqi.xposed.sesame.model.modelFieldExt.SelectModelField
import fansirsqi.xposed.sesame.util.TaskBlacklist
import fansirsqi.xposed.sesame.task.ModelTask
import fansirsqi.xposed.sesame.util.CoroutineUtils
import fansirsqi.xposed.sesame.util.Log
import fansirsqi.xposed.sesame.util.RandomUtil
import fansirsqi.xposed.sesame.util.ResChecker
import fansirsqi.xposed.sesame.util.maps.UserMap
import org.json.JSONObject

class AntOrchard : ModelTask() {
    companion object {
        private val TAG = AntOrchard::class.java.simpleName
    }

    private var userId: String? = UserMap.currentUid
    private var treeLevel: String? = null
    private var executeIntervalInt: Int = 0

    private lateinit var executeInterval: IntegerModelField
    private lateinit var receiveSevenDayGift: BooleanModelField
    private lateinit var receiveOrchardTaskAward: BooleanModelField
    private lateinit var orchardSpreadManureCount: IntegerModelField
    private lateinit var assistFriendList: SelectModelField
    //模式选择
    private lateinit var plantModeField: SelectModelField

    private class ModeOption(key: String, label: String) : MapperEntity() {
        init {
            this.id = key
            this.name = label
        }
    }

    override fun getName(): String = "农场"

    override fun getGroup(): ModelGroup = ModelGroup.ORCHARD

    override fun getIcon(): String = "AntOrchard.png"

    override fun getFields(): ModelFields {
        val modelFields = ModelFields()

        // 构建种植模式选项
        val modeOptions = mutableListOf<MapperEntity>(
            ModeOption("MAIN", "种果树(Main)"),
            ModeOption("YEB", "种摇钱树(Yeb)"),
            ModeOption("HYBRID", "混合模式(先摇钱树后果树)")
        )

        modelFields.addField(
            SelectModelField("plantMode", "种植模式",
                mutableSetOf("MAIN"),
                modeOptions
            ).also { plantModeField = it }
        )

        modelFields.addField(
            IntegerModelField("executeInterval", "执行间隔(毫秒)", 500).also { executeInterval = it }
        )
        modelFields.addField(
            BooleanModelField("receiveSevenDayGift", "收取七日礼包", true).also { receiveSevenDayGift = it }
        )
        modelFields.addField(
            BooleanModelField("receiveOrchardTaskAward", "收取农场任务奖励", false).also { receiveOrchardTaskAward = it }
        )
        modelFields.addField(
            IntegerModelField("orchardSpreadManureCount", "农场每日施肥次数", 0).also { orchardSpreadManureCount = it }
        )
        modelFields.addField(
            SelectModelField("assistFriendList", "助力好友列表", LinkedHashSet(), AlipayUser::getList).also { assistFriendList = it }
        )

        return modelFields
    }

    override suspend fun runSuspend() {
        try {
            Log.record(TAG, "执行开始-$name")
            executeIntervalInt = maxOf(executeInterval.value, 500)

            val indexResponse = AntOrchardRpcCall.orchardIndex()
            val indexJson = JSONObject(indexResponse)

            if (indexJson.optString("resultCode") != "100") {
                Log.record(TAG, indexJson.optString("resultDesc", "orchardIndex 调用失败"))
                return
            }

            if (!indexJson.optBoolean("userOpenOrchard", false)) {
                enableField.value = false
                Log.farm("请先开启芭芭农场！")
                return
            }

            val taobaoDataStr = indexJson.optString("taobaoData")
            if (taobaoDataStr.isNotEmpty()) {
                val taobaoData = JSONObject(taobaoDataStr)
                treeLevel = taobaoData.optJSONObject("gameInfo")
                    ?.optJSONObject("plantInfo")
                    ?.optJSONObject("seedStage")
                    ?.optInt("stageLevel")
                    ?.toString()
            }

            if (userId == null) {
                userId = UserMap.currentUid
            }

            // 七日礼包
            if (receiveSevenDayGift.value) {
                if (indexJson.has("lotteryPlusInfo")) {
                    drawLotteryPlus(indexJson.getJSONObject("lotteryPlusInfo"))
                } else {
                    checkLotteryPlus()
                }
            }

            // 每日肥料 (Entry入口)
            extraInfoGet("entry")

            // 砸金蛋
            val goldenEggInfo = indexJson.optJSONObject("goldenEggInfo")
            if (goldenEggInfo != null) {
                val unsmashedGoldenEggs = goldenEggInfo.optInt("unsmashedGoldenEggs")
                if (unsmashedGoldenEggs > 0) {
                    smashedGoldenEgg(unsmashedGoldenEggs)
                }
            }

            // 农场任务
            if (receiveOrchardTaskAward.value) {
                doOrchardDailyTask(userId!!)
                triggerTbTask()
            }

            // 回访奖励
            if (!Status.hasFlagToday(StatusFlags.FLAG_ANTORCHARD_WIDGET_DAILY_AWARD)) {
                receiveOrchardVisitAward()
            }

            limitedTimeChallenge()

            // 施肥逻辑
            val orchardSpreadManureCountValue = orchardSpreadManureCount.value
            val watered = Status.getIntFlagToday(StatusFlags.FLAG_ANTORCHARD_SPREAD_MANURE_COUNT) ?: 0
            if (orchardSpreadManureCountValue > 0 && watered < 200) {
                CoroutineUtils.sleepCompat(200)
                orchardSpreadManure()
            }

            // 许愿
            if (orchardSpreadManureCountValue in 3..<10) {
                querySubplotsActivity(3)
            } else if (orchardSpreadManureCountValue >= 10) {
                querySubplotsActivity(10)
            }

            // 助力
            orchardAssistFriend()

        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "start.run err:", t)
        } finally {
            Log.record(TAG, "执行结束-$name")
        }
    }

    private suspend fun orchardSpreadManure() {
        try {
            val sourceList = listOf(
                "DNHZ_NC_zhimajingnangSF",
                "widget_shoufei",
                "ch_appcenter__chsub_9patch"
            )
            var loopCount = 0
            var totalWatered = Status.getIntFlagToday(StatusFlags.FLAG_ANTORCHARD_SPREAD_MANURE_COUNT) ?: 0

            // 1. 解析模式
            val modeSet = plantModeField.value
            val mode = when {
                modeSet.contains("YEB") -> "YEB"
                modeSet.contains("HYBRID") -> "HYBRID"
                else -> "MAIN"
            }

            // 2. 初始化目标场景
            var targetScene = if (mode == "YEB" || mode == "HYBRID") "yeb" else "main"
            var isYebFull = false // 摇钱树是否已满标记

            if (totalWatered >= orchardSpreadManureCount.value) {
                Log.record(TAG, "今日已完成施肥目标：$totalWatered/${orchardSpreadManureCount.value}")
                return
            }

            Log.record(TAG, "开始施肥任务，模式: $mode, 首选场景: $targetScene")

            do {
                try {
                    loopCount++
                    /*
                    *神说要有爱
                    if (loopCount > 30) {
                        Log.record(TAG, "循环次数达到上限 $loopCount")
                        return
                    }
                     */

                    // 3. 混合模式动态切换
                    if (mode == "HYBRID") {
                        targetScene = if (isYebFull) "main" else "yeb"
                    }

                    // 4. 切换场景 (服务端状态同步)
                    try {
                        AntOrchardRpcCall.switchPlantScene(targetScene)
                    } catch (ignore: Throwable) {}

                    // 5. 获取肥料余量
                    val orchardIndexData = JSONObject(AntOrchardRpcCall.orchardIndex())
                    if (orchardIndexData.optString("resultCode") != "100") break

                    val taobaoDataStr = orchardIndexData.optString("taobaoData")
                    if (taobaoDataStr.isEmpty()) break

                    val gameInfo = JSONObject(taobaoDataStr).optJSONObject("gameInfo")
                    val accountInfo = gameInfo?.optJSONObject("accountInfo")

                    if (accountInfo != null) {
                        val happyPoint = accountInfo.optInt("happyPoint", 0)
                        val wateringCost = 600 // 默认

                        if (happyPoint < wateringCost) {
                            Log.record(TAG, "肥料不足: 当前 $happyPoint < 消耗 $wateringCost")
                            return
                        }
                    }

                    val remainingTarget = orchardSpreadManureCount.value - totalWatered
                    if (remainingTarget <= 0) return

                    // 6. 执行施肥
                    val wua = SecurityBodyHelper.getSecurityBodyData(4).toString()
                    val randomSource = sourceList.random()
                    val useQuickWater = false
                    val actualWaterTimes = 1

                    // 关键调用：传入 plantScene
                    val spreadResponse = AntOrchardRpcCall.orchardSpreadManure(wua, randomSource, useQuickWater, targetScene)
                    val spreadJson = JSONObject(spreadResponse)
                    val resultCode = spreadJson.optString("resultCode")

                    // 7. 错误处理与模式切换 (P14 = 摇钱树上限)
                    if (resultCode == "P14") {
                        Log.record(TAG, "摇钱树(Yeb)施肥已达持仓金额上限")
                        if (mode == "HYBRID") {
                            Log.record(TAG, "混合模式：切换至果树(Main)继续")
                            isYebFull = true
                            continue // 立即重试，切换到 Main
                        } else if (mode == "YEB") {
                            Log.record(TAG, "摇钱树模式：已满，任务结束")
                            return
                        }
                    }

                    if (resultCode != "100") {
                        Log.error(TAG, "施肥失败($targetScene): ${spreadJson.optString("resultDesc")}")
                        return
                    }

                    // 8. 成功处理
                    val spreadTaobaoDataStr = spreadJson.optString("taobaoData")
                    if (spreadTaobaoDataStr.isNotEmpty()) {
                        val spreadTaobaoData = JSONObject(spreadTaobaoDataStr)
                        var dailyCount = 0

                        if (spreadTaobaoData.has("statistics")) {
                            dailyCount = spreadTaobaoData.getJSONObject("statistics").optInt("dailyAppWateringCount")
                        }
                        // 如果是摇钱树，数据结构可能不同，视情况兼容

                        totalWatered += actualWaterTimes
                        if (dailyCount > 0) totalWatered = dailyCount
                        Status.setIntFlagToday(StatusFlags.FLAG_ANTORCHARD_SPREAD_MANURE_COUNT, totalWatered)

                        val stageText = spreadTaobaoData.optJSONObject("currentStage")?.optString("stageText") ?: ""
                        Log.farm("施肥💩[$targetScene] $stageText|累计:$totalWatered")
                    } else {
                        // 兜底
                        totalWatered += actualWaterTimes
                        Status.setIntFlagToday(StatusFlags.FLAG_ANTORCHARD_SPREAD_MANURE_COUNT, totalWatered)
                        Log.farm("施肥💩[$targetScene] 成功|累计:$totalWatered")
                    }

                    // 9. 施肥后检测肥料礼盒
                    CoroutineUtils.sleepCompat(500)
                    checkFertilizerBox(targetScene)

                } finally {
                    CoroutineUtils.sleepCompat(executeIntervalInt.toLong())
                }
            } while (totalWatered < orchardSpreadManureCount.value)

            Log.record(TAG, "施肥任务完成，总计: $totalWatered")

        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "orchardSpreadManure err:", t)
        }
    }

    // 修复：添加检测礼盒的辅助方法
    private suspend fun checkFertilizerBox(currentPlantScene: String) {
        extraInfoGet(from = "water")
    }

    /**
     * 获取额外信息（包含每日肥料、施肥礼盒）
     * @param from "entry" 或 "water"
     */
    private suspend fun extraInfoGet(from: String = "entry") {
        try {
            val response = AntOrchardRpcCall.extraInfoGet(from)
            val jo = JSONObject(response)

            if (jo.getString("resultCode") == "100") {
                val data = jo.optJSONObject("data") ?: return
                val extraData = data.optJSONObject("extraData") ?: return
                val fertilizerPacket = extraData.optJSONObject("fertilizerPacket") ?: return

                // 状态为 waitTake 时领取
                if (fertilizerPacket.optString("status") == "todayFertilizerWaitTake") {
                    val num = fertilizerPacket.optInt("todayFertilizerNum")
                    val setResponse = JSONObject(AntOrchardRpcCall.extraInfoSet())
                    if (setResponse.getString("resultCode") == "100") {
                        val typeName = if (from == "water") "礼盒" else "每日"
                        Log.farm("领取${typeName}肥料💩[${num}g]")
                    }
                }
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "extraInfoGet err:", t)
        }
    }

    private suspend fun checkLotteryPlus() {
        try {
            if (treeLevel == null) return
            val response = AntOrchardRpcCall.querySubplotsActivity(treeLevel!!)
            val json = JSONObject(response)
            if (!ResChecker.checkRes(TAG, json)) return

            val subplots = json.optJSONArray("subplotsActivityList") ?: return
            for (i in 0 until subplots.length()) {
                val activity = subplots.getJSONObject(i)
                if (activity.optString("activityType") == "LOTTERY_PLUS") {
                    val extendStr = activity.optString("extend")
                    if (extendStr.isNotEmpty()) {
                        val lotteryPlusInfo = JSONObject(extendStr)
                        drawLotteryPlus(lotteryPlusInfo)
                    }
                    break
                }
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "checkLotteryPlus err", t)
        }
    }

    private suspend fun drawLotteryPlus(lotteryPlusInfo: JSONObject) {
        try {
            if (!lotteryPlusInfo.has("userSevenDaysGiftsItem")) return

            val itemId = lotteryPlusInfo.getString("itemId")
            val jo = lotteryPlusInfo.getJSONObject("userSevenDaysGiftsItem")
            val ja = jo.getJSONArray("userEverydayGiftItems")

            for (i in 0 until ja.length()) {
                val jo2 = ja.getJSONObject(i)
                if (jo2.getString("itemId") == itemId) {
                    if (!jo2.getBoolean("received")) {
                        Log.record(TAG, "七日礼包: 发现未领取奖励 (itemId=$itemId)")
                        val jo3 = JSONObject(AntOrchardRpcCall.drawLottery())
                        if (jo3.getString("resultCode") == "100") {
                            val userEverydayGiftItems = jo3.getJSONObject("lotteryPlusInfo")
                                .getJSONObject("userSevenDaysGiftsItem")
                                .getJSONArray("userEverydayGiftItems")

                            for (j in 0 until userEverydayGiftItems.length()) {
                                val jo4 = userEverydayGiftItems.getJSONObject(j)
                                if (jo4.getString("itemId") == itemId) {
                                    val awardCount = jo4.optInt("awardCount", 1)
                                    Log.farm("七日礼包🎁[获得肥料]#${awardCount}g")
                                    break
                                }
                            }
                        } else {
                            Log.record(TAG, jo3.toString())
                        }
                    } else {
                        Log.record(TAG, "七日礼包: 今日已领取")
                    }
                    break
                }
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "drawLotteryPlus err:", t)
        }
    }

    private suspend fun doOrchardDailyTask(userId: String) {
        try {
            val response = AntOrchardRpcCall.orchardListTask()
            val responseJson = JSONObject(response)

            if (responseJson.optString("resultCode") != "100") {
                Log.error("doOrchardDailyTask响应异常", response)
                return
            }

            val inTeam = responseJson.optBoolean("inTeam", false)
            Log.record(TAG, if (inTeam) "当前为农场 team 模式（合种/帮帮种已开启）" else "当前为普通单人农场模式")

            if (responseJson.has("signTaskInfo")) {
                val signTaskInfo = responseJson.getJSONObject("signTaskInfo")
                orchardSign(signTaskInfo)
            }

            val taskList = responseJson.getJSONArray("taskList")
            for (i in 0 until taskList.length()) {
                val task = taskList.getJSONObject(i)
                if (task.optString("taskStatus") != "TODO") continue

                val actionType = task.optString("actionType")
                val sceneCode = task.optString("sceneCode")
                val taskId = task.optString("taskId")
                val groupId = task.optString("groupId")

                val title = if (task.has("taskDisplayConfig")) {
                    task.getJSONObject("taskDisplayConfig").optString("title", "未知任务")
                } else {
                    "未知任务"
                }

                if (TaskBlacklist.isTaskInBlacklist(groupId)) {
                    Log.record(TAG, "跳过黑名单任务[$title] groupId=$groupId")
                    continue
                }

                if (actionType == "VISIT" || actionType == "XLIGHT") {
                    val rightsTimes = task.optInt("rightsTimes", 0)
                    var rightsTimesLimit = task.optInt("rightsTimesLimit", 0)

                    val extend = task.optJSONObject("extend")
                    if (extend != null && rightsTimesLimit <= 0) {
                        val limitStr = extend.optString("rightsTimesLimit", "")
                        if (limitStr.isNotEmpty()) {
                            try {
                                rightsTimesLimit = limitStr.toInt()
                            } catch (ignored: Throwable) {
                            }
                        }
                    }

                    val timesToDo = if (rightsTimesLimit > 0) {
                        val remaining = rightsTimesLimit - rightsTimes
                        if (remaining <= 0) continue else remaining
                    } else {
                        1
                    }

                    for (cnt in 0 until timesToDo) {
                        val finishResponse = JSONObject(AntOrchardRpcCall.finishTask(userId, sceneCode, taskId))
                        if (ResChecker.checkRes(TAG, finishResponse)) {
                            Log.farm("农场广告任务📺[$title] 第${rightsTimes + cnt + 1}次")
                        } else {
                            val errorCode = finishResponse.optString("code", "")
                            if (!errorCode.isEmpty()) {
                                TaskBlacklist.autoAddToBlacklist(groupId, title, errorCode)
                            }
                            break
                        }
                        CoroutineUtils.sleepCompat(executeIntervalInt.toLong())
                    }
                    continue
                }

                if (actionType == "TRIGGER" || actionType == "ADD_HOME" || actionType == "PUSH_SUBSCRIBE") {
                    val finishResponse = JSONObject(AntOrchardRpcCall.finishTask(userId, sceneCode, taskId))
                    if (ResChecker.checkRes(TAG, finishResponse)) {
                        Log.farm("农场任务🧾[$title]")
                    } else {
                        Log.error(TAG, "农场任务🧾[$title]${finishResponse.optString("desc")}")
                    }
                }
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "doOrchardDailyTask err:", t)
        }
    }

    private suspend fun orchardSign(signTaskInfo: JSONObject) {
        try {
            val currentSignItem = signTaskInfo.getJSONObject("currentSignItem")
            if (!currentSignItem.getBoolean("signed")) {
                val joSign = JSONObject(AntOrchardRpcCall.orchardSign())
                if (joSign.getString("resultCode") == "100") {
                    val awardCount = joSign.getJSONObject("signTaskInfo")
                        .getJSONObject("currentSignItem")
                        .getInt("awardCount")
                    Log.farm("农场签到📅[获得肥料]#${awardCount}g")
                } else {
                    Log.record(TAG, joSign.toString())
                }
            } else {
                Log.record(TAG, "农场今日已签到")
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "orchardSign err:", t)
        }
    }

    private suspend fun smashedGoldenEgg(count: Int) {
        try {
            val response = AntOrchardRpcCall.smashedGoldenEgg(count)
            val jo = JSONObject(response)

            if (ResChecker.checkRes(TAG, jo)) {
                val batchSmashedList = jo.getJSONArray("batchSmashedList")
                for (i in 0 until batchSmashedList.length()) {
                    val smashedItem = batchSmashedList.getJSONObject(i)
                    val manureCount = smashedItem.optInt("manureCount", 0)
                    val jackpot = smashedItem.optBoolean("jackpot", false)
                    Log.farm("砸出肥料 🎖️: $manureCount g" + if (jackpot) "（触发大奖）" else "")
                }
            } else {
                Log.record(TAG, jo.optString("resultDesc", "未知错误"))
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "smashedGoldenEgg err:", t)
        }
    }

    private suspend fun triggerTbTask() {
        try {
            val response = AntOrchardRpcCall.orchardListTask()
            val jo = JSONObject(response)

            if (jo.getString("resultCode") == "100") {
                val jaTaskList = jo.getJSONArray("taskList")
                for (i in 0 until jaTaskList.length()) {
                    val jo2 = jaTaskList.getJSONObject(i)
                    if (jo2.getString("taskStatus") != "FINISHED") continue

                    val title = jo2.getJSONObject("taskDisplayConfig").getString("title")
                    val awardCount = jo2.optInt("awardCount", 0)
                    val taskId = jo2.getString("taskId")
                    val taskPlantType = jo2.getString("taskPlantType")

                    val jo3 = JSONObject(AntOrchardRpcCall.triggerTbTask(taskId, taskPlantType))
                    if (jo3.getString("resultCode") == "100") {
                        Log.farm("领取奖励🎖️[$title]#${awardCount}g肥料")
                    } else {
                        Log.record(TAG, jo3.toString())
                    }
                }
            } else {
                Log.record(TAG, jo.getString("resultDesc"))
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "triggerTbTask err:", t)
        }
    }

    private fun receiveOrchardVisitAward() {
        try {
            val awardSources = listOf(
                Pair("tmall", "upgrade_tmall_exchange_task"),
                Pair("antfarm", "ANTFARM_ORCHARD_PLUS"),
                Pair("widget", "widget_shoufei")
            )
            var hasAwardReceived = false

            for ((diversionSource, source) in awardSources) {
                val response = AntOrchardRpcCall.receiveOrchardVisitAward(diversionSource, source)
                val jo = JSONObject(response)

                if (!ResChecker.checkRes(TAG, response)) {
                    continue
                }

                val awardList = jo.optJSONArray("orchardVisitAwardList")
                if (awardList == null || awardList.length() == 0) {
                    continue
                }

                for (i in 0 until awardList.length()) {
                    val awardObj = awardList.optJSONObject(i) ?: continue
                    val awardCount = awardObj.optInt("awardCount", 0)
                    val awardDesc = awardObj.optString("awardDesc", "")
                    Log.farm("回访奖励[$awardDesc] $awardCount g肥料")
                    hasAwardReceived = true
                }
            }

            if (hasAwardReceived) {
                Status.setFlagToday(StatusFlags.FLAG_ANTORCHARD_WIDGET_DAILY_AWARD)
                Log.record(TAG, "回访奖励领取完成")
            } else {
                Log.record(TAG, "回访奖励已全部领取或无可领取奖励")
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "receiveOrchardVisitAward err:", t)
        }
    }

    private fun limitedTimeChallenge() {
        try {
            val wua = SecurityBodyHelper.getSecurityBodyData(4).toString()
            val response = AntOrchardRpcCall.orchardSyncIndex(wua)
            val root = JSONObject(response)
            if (!ResChecker.checkRes(TAG, root)) return

            val challenge = root.optJSONObject("limitedTimeChallenge") ?: return
            val currentRound = challenge.optInt("currentRound", 0)
            if (currentRound <= 0) return

            val taskArray = challenge.optJSONArray("limitedTimeChallengeTasks") ?: return
            val targetIdx = currentRound - 1
            if (targetIdx !in 0 until taskArray.length()) return

            val roundTask = taskArray.optJSONObject(targetIdx) ?: return
            val ongoing = roundTask.optBoolean("ongoing", false)
            val MtaskStatus = roundTask.optString("taskStatus")
            val MtaskId = roundTask.optString("taskId")
            val MawardCount = roundTask.optInt("awardCount", 0)

            if (MtaskStatus == "FINISHED" && ongoing) {
                val awardResp = AntOrchardRpcCall.receiveTaskAward("ORCHARD_LIMITED_TIME_CHALLENGE", MtaskId)
                val joo = JSONObject(awardResp)
                if (ResChecker.checkRes(TAG, joo)) {
                    Log.farm("第 $currentRound 轮 限时任务🎁[肥料 * $MawardCount]")
                }
                return
            }

            if (roundTask.optString("taskStatus") != "TODO") return
            val childTasks = roundTask.optJSONArray("childTaskList") ?: return

            for (i in 0 until childTasks.length()) {
                val child = childTasks.optJSONObject(i) ?: continue
                val childTaskId = child.optString("taskId", "未知ID")
                val actionType = child.optString("actionType")
                val groupId = child.optString("groupId")
                val taskStatus = child.optString("taskStatus")
                val sceneCode = child.optString("sceneCode")
                val taskRequire = child.optInt("taskRequire", 0)
                val taskProgress = child.optInt("taskProgress", 0)

                if (taskStatus != "TODO") continue
                if (groupId == "GROUP_1_STEP_3_GAME_WZZT_30s") continue
                if (groupId == "GROUP_1_STEP_2_GAME_WZZT_30s") continue

                when (actionType) {
                    "SPREAD_MANURE" -> {
                        val need = taskRequire - taskProgress
                        if (need > 0) {
                            repeat(need) {
                                val w = SecurityBodyHelper.getSecurityBodyData(4).toString()
                                val r = AntOrchardRpcCall.orchardSpreadManure(w, "ch_appcenter__chsub_9patch")
                                if (JSONObject(r).optString("resultCode") != "100") return
                            }
                        }
                    }
                    "GAME_CENTER" -> {
                        val r = AntOrchardRpcCall.noticeGame("2021004165643274")
                        if (ResChecker.checkRes(TAG, JSONObject(r))) {
                            Log.record(TAG, "游戏任务触发成功")
                        }
                    }
                    "VISIT" -> {
                        val displayCfg = child.optJSONObject("taskDisplayConfig") ?: continue
                        val targetUrl = displayCfg.optString("targetUrl", "")
                        if (targetUrl.isEmpty()) continue

                        val finalUrl = UrlUtil.getFullNestedUrl(targetUrl, "url") ?: ""
                        val spaceCodeFeeds = if (finalUrl.isNotEmpty()) UrlUtil.extractParamFromUrl(finalUrl, "spaceCodeFeeds") else null
                        val finalSpaceCode = spaceCodeFeeds ?: UrlUtil.getParamValue(targetUrl, "spaceCodeFeeds") ?: ""
                        if (finalSpaceCode.isEmpty()) continue

                        val pageFrom = "ch_url-https://render.alipay.com/p/yuyan/180020010001263018/game.html"
                        val session = "u_41ba1_2f33e"
                        val r = XLightRpcCall.xlightPlugin(finalUrl, pageFrom, session, finalSpaceCode)
                        val jr = JSONObject(r)

                        val playingResult = jr.optJSONObject("resData")?.optJSONObject("playingResult") ?: jr.optJSONObject("playingResult")
                        if (playingResult == null) continue

                        val playingBizId = playingResult.optString("playingBizId", "")
                        val eventRewardDetail = playingResult.optJSONObject("eventRewardDetail")
                        val infoListArray = eventRewardDetail?.optJSONArray("eventRewardInfoList")
                        if (infoListArray == null || infoListArray.length() == 0) continue

                        val playEventInfo = infoListArray.getJSONObject(0)
                        val finishResult = XLightRpcCall.finishTask(playingBizId, playEventInfo, sceneCode, groupId)
                        if (ResChecker.checkRes(TAG, JSONObject(finishResult))) {
                            Log.record(TAG, "浏览广告任务完成")
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "limitedTimeChallenge err:", t)
        }
    }

    private suspend fun querySubplotsActivity(taskRequire: Int) {
        try {
            val response = AntOrchardRpcCall.querySubplotsActivity(treeLevel!!)
            val jo = JSONObject(response)

            if (jo.getString("resultCode") == "100") {
                val subplotsActivityList = jo.getJSONArray("subplotsActivityList")
                for (i in 0 until subplotsActivityList.length()) {
                    val jo2 = subplotsActivityList.getJSONObject(i)
                    if (jo2.getString("activityType") != "WISH") continue

                    val activityId = jo2.getString("activityId")
                    when (jo2.getString("status")) {
                        "NOT_STARTED" -> {
                            val extend = jo2.getString("extend")
                            val jo3 = JSONObject(extend)
                            val wishActivityOptionList = jo3.getJSONArray("wishActivityOptionList")
                            var optionKey: String? = null

                            for (j in 0 until wishActivityOptionList.length()) {
                                val jo4 = wishActivityOptionList.getJSONObject(j)
                                if (taskRequire == jo4.getInt("taskRequire")) {
                                    optionKey = jo4.getString("optionKey")
                                    break
                                }
                            }

                            if (optionKey != null) {
                                val jo5 = JSONObject(AntOrchardRpcCall.triggerSubplotsActivity(activityId, "WISH", optionKey))
                                if (jo5.getString("resultCode") == "100") {
                                    Log.farm("农场许愿✨[每日施肥$taskRequire 次]")
                                } else {
                                    Log.record(TAG, jo5.getString("resultDesc"))
                                }
                            }
                        }
                        "FINISHED" -> {
                            val jo3 = JSONObject(AntOrchardRpcCall.receiveOrchardRights(activityId, "WISH"))
                            if (jo3.getString("resultCode") == "100") {
                                Log.farm("许愿奖励✨[肥料${jo3.getInt("amount")}g]")
                                querySubplotsActivity(taskRequire)
                                return
                            } else {
                                Log.record(TAG, jo3.getString("resultDesc"))
                            }
                        }
                    }
                }
            } else {
                Log.record(TAG, jo.getString("resultDesc"))
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "querySubplotsActivity err:", t)
        }
    }

    private suspend fun orchardAssistFriend() {
        try {
            if (!Status.canAntOrchardAssistFriendToday()) {
                Log.record(TAG, "今日已助力，跳过农场助力")
                return
            }

            val friendSet = assistFriendList.value
            for (uid in friendSet) {
                val shareId = Base64.encodeToString(
                    ("$uid-${RandomUtil.getRandomInt(5)}ANTFARM_ORCHARD_SHARE_P2P").toByteArray(),
                    Base64.NO_WRAP
                )
                val str = AntOrchardRpcCall.achieveBeShareP2P(shareId)
                val jsonObject = JSONObject(str)
                CoroutineUtils.sleepCompat(800)
                val name = UserMap.getMaskName(uid)

                if (!ResChecker.checkRes(TAG, str)) {
                    val code = jsonObject.getString("code")
                    if (code == "600000027") {
                        Log.record(TAG, "农场助力💪今日助力他人次数上限")
                        Status.antOrchardAssistFriendToday()
                        return
                    }
                    Log.error(TAG, "农场助力😔失败[$name]${jsonObject.optString("desc")}")
                    continue
                }
                Log.farm("农场助力💪[助力:$name]")
            }
            Status.antOrchardAssistFriendToday()
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "orchardAssistFriend err:", t)
        }
    }
}