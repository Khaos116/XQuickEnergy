package fansirsqi.xposed.sesame.task.antOrchard

import android.util.Base64
import fansirsqi.xposed.sesame.data.Status
import fansirsqi.xposed.sesame.data.StatusFlags
import fansirsqi.xposed.sesame.entity.AlipayUser
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
import fansirsqi.xposed.sesame.util.MyUtils
import fansirsqi.xposed.sesame.util.Notify
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
    private var wuaList: Array<String>? = null
    private var executeIntervalInt: Int = 0

    private lateinit var executeInterval: IntegerModelField
    // 新增七日礼包开关
    private lateinit var receiveSevenDayGift: BooleanModelField
    private lateinit var receiveOrchardTaskAward: BooleanModelField
    private lateinit var orchardSpreadManureCount: IntegerModelField
    private lateinit var assistFriendList: SelectModelField

    override fun getName(): String = "农场"

    override fun getGroup(): ModelGroup = ModelGroup.ORCHARD

    override fun getIcon(): String = "AntOrchard.png"

    override fun getFields(): ModelFields {
        val modelFields = ModelFields()
        modelFields.addField(
            IntegerModelField("executeInterval", "执行间隔(毫秒)", 500).also { executeInterval = it }
        )
        // 新增七日礼包开关
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

            val taobaoData = JSONObject(indexJson.getString("taobaoData"))
            treeLevel = taobaoData.getJSONObject("gameInfo")
                .getJSONObject("plantInfo")
                .getJSONObject("seedStage")
                .getInt("stageLevel")
                .toString()

            if (userId == null) {
                userId = UserMap.currentUid
            }

            // 修改：七日礼包逻辑完善
            if (receiveSevenDayGift.value) {
                if (indexJson.has("lotteryPlusInfo")) {
                    drawLotteryPlus(indexJson.getJSONObject("lotteryPlusInfo"))
                } else {
                    // 首页没有，去子活动里找
                    checkLotteryPlus()
                }
            }

            // 每日肥料
            extraInfoGet()

            MyUtils.CHANGE_KT2.trim()
            //如果有🥚 则进行砸🥚
            val goldenEggInfo = indexJson.optJSONObject("goldenEggInfo")
            val unsmashedGoldenEggs = goldenEggInfo?.optInt("unsmashedGoldenEggs") ?: 0
            if (unsmashedGoldenEggs > 0) {
                smashedGoldenEgg(unsmashedGoldenEggs)
            }

            // 农场任务
            if (receiveOrchardTaskAward.value) {
                doOrchardDailyTask(userId!!)
                triggerTbTask()
            }

            //回访奖励
            if (!Status.hasFlagToday(StatusFlags.FLAG_ANTORCHARD_WIDGET_DAILY_AWARD)) {
                receiveOrchardVisitAward()
            }

            limitedTimeChallenge()

            // 施肥
            val orchardSpreadManureCountValue = orchardSpreadManureCount.value
            val watered = Status.getIntFlagToday(StatusFlags.FLAG_ANTORCHARD_SpreadManure_Count) ?: 0
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

    private fun canSpreadManureContinue(stageBefore: Int, stageAfter: Int): Boolean {
        return if (stageAfter - stageBefore > 1) {
            true
        } else {
            Log.record(TAG, "施肥只加0.01%进度今日停止施肥！")
            false
        }
    }

    private suspend fun orchardSpreadManure() {
        try {
            val sourceList = listOf(
                "DNHZ_NC_zhimajingnangSF",
                "widget_shoufei",
                "ch_appcenter__chsub_9patch"
            )
            var loopCount = 0 // 循环次数计数器

            // 获取今日已施肥次数
            var totalWatered = Status.getIntFlagToday(StatusFlags.FLAG_ANTORCHARD_SpreadManure_Count) ?: 0

            // 检查是否已达到目标
            if (totalWatered >= orchardSpreadManureCount.value) {
                Log.record(TAG, "今日已完成施肥目标：$totalWatered/${orchardSpreadManureCount.value}")
                return
            }

            Log.record(TAG, "开始施肥任务，当前进度：$totalWatered/${orchardSpreadManureCount.value}")

            do {
                try {
                    loopCount++
                    if (loopCount > 20) {
                        Log.record(TAG, "循环次数达到上限 $loopCount，避免任务时间过长")
                        return
                    }

                    // 获取果园数据
                    val orchardIndexData = JSONObject(AntOrchardRpcCall.orchardIndex())
                    if (orchardIndexData.getString("resultCode") != "100") {
                        Log.error(TAG, orchardIndexData.getString("resultDesc"))
                        return
                    }

                    val orchardTaobaoData = JSONObject(orchardIndexData.getString("taobaoData"))
                    val gameInfo = orchardTaobaoData.getJSONObject("gameInfo")
                    val plantInfo = gameInfo.getJSONObject("plantInfo")

                    // 检查是否可以兑换
                    if (plantInfo.getBoolean("canExchange")) {
                        Log.farm("🎉 农场果树可兑换！")
                        Notify.sendNewNotification("芝麻粒TK提醒您：", "🎉 农场果树可兑换！")
                        return
                    }

                    val seedStage = plantInfo.getJSONObject("seedStage")
                    treeLevel = seedStage.getInt("stageLevel").toString()

                    val accountInfo = gameInfo.getJSONObject("accountInfo")
                    val happyPoint = accountInfo.getInt("happyPoint")
                    val wateringCost = accountInfo.getInt("wateringCost")
                    val wateringLeftTimes = accountInfo.getInt("wateringLeftTimes")

                    if (happyPoint < wateringCost) {
                        Log.record(TAG, "肥料不足: 当前 $happyPoint < 消耗 $wateringCost")
                        return
                    }

                    if (wateringLeftTimes <= 0) {
                        Log.record(TAG, "今日剩余施肥次数为 0")
                        return
                    }

                    val remainingTarget = orchardSpreadManureCount.value - totalWatered
                    if (remainingTarget <= 0) {
                        Log.record(TAG, "已达今日施肥目标：$totalWatered/${orchardSpreadManureCount.value}")
                        return
                    }

                    val maxCanWater = minOf(remainingTarget, wateringLeftTimes)

                    //无需更改，一键施肥5次 只加100明日奖励，        施肥1次也加100，为了增加农场奖励，暂时不使用快速模式
                    val useQuickWater = false                //maxCanWater >= 5
                    val actualWaterTimes = 1         // if (useQuickWater) minOf(5, maxCanWater) else 1

                    val wua = SecurityBodyHelper.getSecurityBodyData(4).toString()
                    val randomSource = sourceList.random()

                    val spreadManureData = JSONObject(
                        AntOrchardRpcCall.orchardSpreadManure(wua, randomSource, useQuickWater)
                    )

                    if (spreadManureData.getString("resultCode") != "100") {
                        Log.error(TAG, "农场施肥失败: ${spreadManureData.getString("resultDesc")}")
                        return
                    }

                    val spreadTaobaoData = JSONObject(spreadManureData.getString("taobaoData"))
                    val currentStage = spreadTaobaoData.getJSONObject("currentStage")
                    val stageLevel = currentStage.getDouble("stageLevel") // 当前等级
                    val stageMaxLevel = currentStage.getDouble("stageMaxLevel") // 最大等级
                    val currentLevelProgressPercentage = currentStage.getDouble("currentLevelProgressPercentage") // 进度
                    val stageText = currentStage.getString("stageText")
                    val dailyAppWateringCount = spreadTaobaoData.getJSONObject("statistics").getInt("dailyAppWateringCount")

                    // 累加施肥次数
                    totalWatered += actualWaterTimes
                    if (dailyAppWateringCount > 0) totalWatered = dailyAppWateringCount               //没用的判断增加了！
                    //原来用的totalWatered，其实想通过index获取今日次数，但是单人好像获取不到？ 为了防止浇水上限，所以直接同步 dailyAppWateringCount
                    Status.setIntFlagToday(StatusFlags.FLAG_ANTORCHARD_SpreadManure_Count, dailyAppWateringCount)

                    val waterMethod = if (useQuickWater) "x$actualWaterTimes" else "x1"
                    Log.farm("农场施肥💩[$waterMethod] $stageText|累计:$totalWatered 今日:$dailyAppWateringCount")///${orchardSpreadManureCount.value}


                    // 检查果树成长上限
                    if (stageLevel >= stageMaxLevel && currentLevelProgressPercentage >= 100.0) {
                        Log.record(TAG, "果树已达成长上限，停止施肥")
                        return
                    }

                } finally {
                    CoroutineUtils.sleepCompat(executeIntervalInt.toLong())
                }
            } while (totalWatered < orchardSpreadManureCount.value)

            Log.record(TAG, "施肥任务完成，总计施肥: $totalWatered/${orchardSpreadManureCount.value}")

        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "orchardSpreadManure err:", t)
        }
    }

    private suspend fun extraInfoGet() {
        try {
            val response = AntOrchardRpcCall.extraInfoGet()
            val jo = JSONObject(response)

            if (jo.getString("resultCode") == "100") {
                val fertilizerPacket = jo.getJSONObject("data")
                    .getJSONObject("extraData")
                    .getJSONObject("fertilizerPacket")

                if (fertilizerPacket.getString("status") != "todayFertilizerWaitTake") return

                val todayFertilizerNum = fertilizerPacket.getInt("todayFertilizerNum")
                val setResponse = JSONObject(AntOrchardRpcCall.extraInfoSet())

                if (setResponse.getString("resultCode") == "100") {
                    Log.farm("每日肥料💩[${todayFertilizerNum}g]")
                } else {
                    Log.error(TAG, setResponse.toString())
                }
            } else {
                Log.error(TAG, jo.toString())
            }
        } catch (t: Throwable) {
            Log.printStackTrace(TAG, "extraInfoGet err:", t)
        }
    }

    // 新增：从子活动中检查七日礼包
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
                        // extend 是字符串，需要二次解析
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

            // team 模式：inTeam = true 表示已经开启合种/帮帮种
            val inTeam = responseJson.optBoolean("inTeam", false)
            Log.record(TAG, if (inTeam) "当前为农场 team 模式（合种/帮帮种已开启）" else "当前为普通单人农场模式")

            // 签到任务
            if (responseJson.has("signTaskInfo")) {
                val signTaskInfo = responseJson.getJSONObject("signTaskInfo")
                orchardSign(signTaskInfo)
            }

            val taskList = responseJson.getJSONArray("taskList")
            for (i in 0 until taskList.length()) {
                val task = taskList.getJSONObject(i)

                // 只处理 TODO 状态的任务
                if (task.optString("taskStatus") != "TODO") continue

                val actionType = task.optString("actionType")
                val sceneCode = task.optString("sceneCode")
                val taskId = task.optString("taskId")
                val groupId = task.optString("groupId")

                // 任务标题
                val title = if (task.has("taskDisplayConfig")) {
                    task.getJSONObject("taskDisplayConfig").optString("title", "未知任务")
                } else {
                    "未知任务"
                }

                if ("逛一逛一淘" == title && MyUtils._关闭不支持RPC1) {
                  continue
                }

                // 黑名单任务：后端不支持 finishTask 或需要端内实际跳转
                if (TaskBlacklist.isTaskInBlacklist(groupId)) {
                    Log.record(TAG, "跳过黑名单任务[$title] groupId=$groupId")
                    continue
                }

                // 广告类任务：VISIT / XLIGHT
                if (actionType == "VISIT" || actionType == "XLIGHT") {
                    val rightsTimes = task.optInt("rightsTimes", 0)
                    var rightsTimesLimit = task.optInt("rightsTimesLimit", 0)

                    // 有些任务把次数放在 extend.rightsTimesLimit（字符串）里
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

                    // 控制执行次数
                    val timesToDo = if (rightsTimesLimit > 0) {
                        val remaining = rightsTimesLimit - rightsTimes
                        if (remaining <= 0) continue else remaining
                    } else {
                        1
                    }

                    for (cnt in 0 until timesToDo) {
                        //[{"outBizNo":"mokuai_senlin_hydrw_0.2913546844220295","requestType":"RPC","sceneCode":"ANTOCEAN_TASK","source":"ANTFOCEAN","taskType":"mokuai_senlin_hydrw","uniqueId":"1767460371318492722387988273949"}]
                        if (MyUtils._关闭不支持RPC2 && "ANTOCEAN_TASK" == sceneCode && "mokuai_senlin_hydrw" == taskId) {
                          continue
                        }
                        val finishResponse = JSONObject(AntOrchardRpcCall.finishTask(userId, sceneCode, taskId))
                        if (ResChecker.checkRes(TAG, finishResponse)) {
                            Log.farm("农场广告任务📺[$title] 第${rightsTimes + cnt + 1}次")
                        } else {
                            //  Log.error(TAG, "失败：农场广告任务📺[$titlge] 第${rightsTimes + cnt + 1}次${finishResponse.optString("desc")}")
                            // 自动添加到黑名单
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

                // 非广告类的普通任务
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
                // 解析 batchSmashedList
                val batchSmashedList = jo.getJSONArray("batchSmashedList")
                for (i in 0 until batchSmashedList.length()) {
                    val smashedItem = batchSmashedList.getJSONObject(i)
                    val manureCount = smashedItem.optInt("manureCount", 0)
                    val jackpot = smashedItem.optBoolean("jackpot", false)

                    // 输出信息
                    Log.farm("砸出肥料 🎖️: $manureCount g" + if (jackpot) "（触发大奖）" else "")
                }

                /*
                 // 可选：输出 goldenEggInfoVO 状态
                 val goldenEggInfo = jo.optJSONObject("goldenEggInfoVO")
                 if (goldenEggInfo != null) {
                     val smashedGoldenEggs = goldenEggInfo.optInt("smashedGoldenEggs", 0)
                     val unsmashedGoldenEggs = goldenEggInfo.optInt("unsmashedGoldenEggs", 0)
                     Log.farm("已砸蛋: $smashedGoldenEggs, 剩余可砸蛋: $unsmashedGoldenEggs")
                 }
                 */

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
                    val actionType = jo2.getString("actionType")//如果是 XLIGHT要走单独的浏览广告完成,注意，这里只看 actionType，taskPlantType可能是XLight但是不走这里
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
            // 定义两种奖励来源
            val awardSources = listOf(
                Pair("tmall", "upgrade_tmall_exchange_task"),
                Pair("antfarm", "ANTFARM_ORCHARD_PLUS"),
                Pair("widget", "widget_shoufei")
            )

            // 记录是否有成功领取到奖励
            var hasAwardReceived = false

            // 遍历所有奖励来源
            for ((diversionSource, source) in awardSources) {
                // Log.debug(TAG, "尝试领取回访奖励: diversionSource=$diversionSource, source=$source")

                val response = AntOrchardRpcCall.receiveOrchardVisitAward(diversionSource, source)
                val jo = JSONObject(response)

                // 检查响应是否成功
                if (!ResChecker.checkRes(TAG, response)) {
                    Log.error(TAG, "领取回访奖励失败 (source=$source): $response")
                    continue
                }

                val awardList = jo.optJSONArray("orchardVisitAwardList")
                if (awardList == null || awardList.length() == 0) {
                    Log.record(TAG, "领取回访奖励完成 (source=$source): 无奖励，可能已领取过")
                    continue
                }

                // 遍历所有奖励内容
                for (i in 0 until awardList.length()) {
                    val awardObj = awardList.optJSONObject(i) ?: continue

                    val awardCount = awardObj.optInt("awardCount", 0)
                    val awardDesc = awardObj.optString("awardDesc", "")
                    Log.farm("回访奖励[$awardDesc] $awardCount g肥料")
                    hasAwardReceived = true
                }
            }

            // 如果至少有一个来源领取到了奖励，则设置标记
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

    //限时奖励
    private fun limitedTimeChallenge() {
        try {
            // 1. 请求同步数据
            val wua = SecurityBodyHelper.getSecurityBodyData(4).toString()

            val response = AntOrchardRpcCall.orchardSyncIndex(wua)
            val root = JSONObject(response)

            if (!ResChecker.checkRes(TAG, root)) {
                Log.error(TAG, "orchardSyncIndex 查询失败: $response")
                return
            }

            // 2. 获取 limitedTimeChallenge
            val challenge = root.optJSONObject("limitedTimeChallenge") ?: run {
                Log.error(TAG, "错误：limitedTimeChallenge 字段不存在或为 null")
                return
            }

            val currentRound = challenge.optInt("currentRound", 0)
            if (currentRound <= 0) {
                Log.error(TAG, "错误：currentRound 无效：$currentRound")
                return
            }

            // 3. 获取任务数组
            val taskArray = challenge.optJSONArray("limitedTimeChallengeTasks") ?: run {
                Log.error(TAG, "错误：limitedTimeChallengeTasks 字段不存在或不是数组")
                return
            }

            val targetIdx = currentRound - 1
            if (targetIdx !in 0 until taskArray.length()) {
                Log.error(TAG, "错误：当前轮数 $currentRound 对应下标 $targetIdx 超出数组长度: ${taskArray.length()}")
                return
            }

            // 4. 当前轮任务
            val roundTask = taskArray.optJSONObject(targetIdx) ?: run {
                Log.error(TAG, "错误：第 $currentRound 轮任务不存在")
                return
            }

            val ongoing = roundTask.optBoolean("ongoing", false)      // 该轮是否正在进行（若任务完成但 ongoing=true，说明待领取）
            val MtaskStatus = roundTask.optString("taskStatus")
            val MtaskId = roundTask.optString("taskId")
            val MawardCount = roundTask.optInt("awardCount", 0)

            // 🔥 条件：大任务已经完成，但仍未领取奖励（ongoing=true）
            if (MtaskStatus == "FINISHED" && ongoing) {
                Log.record(TAG, "第 $currentRound 轮 奖励未领取，尝试领取")

                val awardResp = AntOrchardRpcCall.receiveTaskAward(
                    "ORCHARD_LIMITED_TIME_CHALLENGE",
                    MtaskId
                )

                val joo = JSONObject(awardResp)

                if (ResChecker.checkRes(TAG, joo)) {
                    Log.farm("第 $currentRound 轮 限时任务🎁[肥料 * $MawardCount]")

                } else {
                    val desc = joo.optString("desc", "未知错误")
                    Log.error(TAG, "农场 限时任务 错误：$desc")

                }
                return
            }

            if (roundTask.optString("taskStatus") != "TODO") {
                Log.error(TAG, "警告：第 $currentRound 轮任务非 TODO，状态=${roundTask.optString("taskStatus")}")
                return
            }

            // 子任务
            val childTasks = roundTask.optJSONArray("childTaskList") ?: run {
                Log.error(TAG, "警告：第 $currentRound 轮无子任务列表")
                return
            }

            Log.record(TAG, "开始处理第 $currentRound 轮的 ${childTasks.length()} 个子任务")

            // 5. 遍历子任务
            for (i in 0 until childTasks.length()) {

                val child = childTasks.optJSONObject(i) ?: run {
                    Log.error(TAG, "警告：子任务索引 $i 非 JSONObject，跳过")
                    continue
                }

                val childTaskId = child.optString("taskId", "未知ID")
                val actionType = child.optString("actionType")
                val groupId = child.optString("groupId")  //GROUP_1_STEP_3_GAME_WZZT_30s
                val taskStatus = child.optString("taskStatus")
                val taskId = child.optString("taskId") //GROUP_1_STEP_3_GAME_WZZT_30s
                val sceneCode = child.optString("sceneCode")
                val taskRequire = child.optInt("taskRequire", 0)
                val taskProgress = child.optInt("taskProgress", 0)
                val awardCount = child.optInt("awardCount", 0)


                if (taskStatus != "TODO") continue
                if (groupId == "GROUP_1_STEP_3_GAME_WZZT_30s") continue//完成不了玩游戏30秒
                if (groupId == "GROUP_1_STEP_2_GAME_WZZT_30s") continue//完成不了玩游戏30秒
                Log.record(TAG, "------ 开始处理子任务 $i | ID=$childTaskId ------")

                // ============================
                //  子任务逻辑处理
                // ============================

                when (actionType) {

                    // 施肥任务
                    "SPREAD_MANURE" -> {
                        val need = taskRequire - taskProgress

                        if (need > 0) {
                            Log.record(TAG, "施肥任务需补充 $need 次")

                            repeat(need) { index ->
                                val wua = SecurityBodyHelper.getSecurityBodyData(4).toString()
                                val spreadResult = AntOrchardRpcCall.orchardSpreadManure(wua, "ch_appcenter__chsub_9patch")
                                val resultJson = JSONObject(spreadResult)
                                val resultCode = resultJson.optString("resultCode", "")
                                val resultDesc = resultJson.optString("resultDesc", "")
                                if (resultCode != "100") {
                                    Log.error(TAG, "农场 orchardSpreadManure 错误：$resultDesc")
                                    return   // ❗施肥失败直接退出整个 limitedTimeChallenge()
                                } else {
                                    Log.record(TAG, "施肥第 ${index + 1} 次结果：$resultDesc")
                                }
                            }

                            Log.record(TAG, "施肥任务成功完成 $need 次")
                        } else {
                            Log.record(TAG, "施肥任务无需操作（当前进度 >= 需求）")
                        }
                    }

                    // 打游戏任务（仅支持 GROUP_1_STEP_1_PLAY_GAME）
                    "GAME_CENTER" -> {

                        val r = AntOrchardRpcCall.noticeGame("2021004165643274")

                        val jr = JSONObject(r)
                        if (ResChecker.checkRes(TAG, jr)) {
                            Log.record(TAG, "游戏任务触发成功 → 子任务应当自动完成")
                        } else {
                            Log.error(TAG, "游戏任务触发失败，返回: $r")//
                        }

                    }

                    // 浏览广告任务
                    "VISIT" -> {
                        val displayCfg = child.optJSONObject("taskDisplayConfig")
                        if (displayCfg == null) {
                            Log.error(TAG, "任务没有 taskDisplayConfig，无法继续")
                            continue
                        }

                        val targetUrl = displayCfg.optString("targetUrl", "")
                        if (targetUrl.isEmpty()) {
                            Log.error(TAG, "taskDisplayConfig.targetUrl 为空")
                            continue
                        }

                        // ① 提取完整的落地页URL
                        val finalUrl = UrlUtil.getFullNestedUrl(targetUrl, "url") ?: ""
                        //Log.record(TAG, "解析到完整落地页 url = $finalUrl")

                        // ② 从完整URL中提取spaceCodeFeeds
                        val spaceCodeFeeds = if (finalUrl.isNotEmpty()) {
                            UrlUtil.extractParamFromUrl(finalUrl, "spaceCodeFeeds")
                        } else null
                        //Log.record(TAG, "解析d到 spaceCodeFeeds = ${spaceCodeFeeds ?: "null"}")

                        // 容错处理：如果spaceCodeFeeds还是null，尝试从原始targetUrl直接提取
                        val finalSpaceCode = spaceCodeFeeds ?: UrlUtil.getParamValue(targetUrl, "spaceCodeFeeds") ?: ""
                        if (finalSpaceCode.isEmpty()) {
                            //      Log.record(TAG, "spaceCodeFeeds 解析失败，跳过此任务")
                            continue
                        }

                        // -------------------------------------------------------------
                        // 🔥 触发游戏任务（广告浏览）
                        // -------------------------------------------------------------
                        val pageFrom = "ch_url-https://render.alipay.com/p/yuyan/180020010001263018/game.html"
                        val session = "u_41ba1_2f33e"

                        val r = XLightRpcCall.xlightPlugin(
                            pageUrl = finalUrl,
                            pageFrom = pageFrom,
                            session = session,
                            spaceCode = finalSpaceCode
                        )

                        val jr = JSONObject(r)
                        /*
                        if (!ResChecker.checkRes(TAG, jr)) {
                            Log.record(TAG, "广告任务触发失败，返回")//: $r
                            Log.record(TAG, "调试信息 → finalUrl: $finalUrl, spaceCode: $finalSpaceCode")
                            continue
                        }*/

                        Log.record(TAG, "广告任务触发成功 → 即将调用 finishTask() 完成任务")

                        // -------------------------------------------------------------
                        // 🔥 自动完成任务（兼容两种JSON结构）
                        // -------------------------------------------------------------

                        // 尝试获取playingResult（兼容有无resData的情况）
                        val playingResult = jr.optJSONObject("resData")?.optJSONObject("playingResult")
                            ?: jr.optJSONObject("playingResult")

                        if (playingResult == null) {
                            Log.error(TAG, "playingResult 为空，无法 finishTask")
                            continue
                        }

                        val playingBizId = playingResult.optString("playingBizId", "")
                        if (playingBizId.isEmpty()) {
                            Log.error(TAG, "playingBizId 为空，无法 finishTask")
                            continue
                        }

                        // 获取事件列表中的第一个 eventRewardInfo（作为 playEventInfo）
                        val eventRewardDetail = playingResult.optJSONObject("eventRewardDetail")
                        val infoListArray = eventRewardDetail?.optJSONArray("eventRewardInfoList")

                        if (infoListArray == null || infoListArray.length() == 0) {
                            Log.error(TAG, "eventRewardInfoList 为空，无法 finishTask")
                            continue
                        }

                        val playEventInfo = infoListArray.getJSONObject(0)

                        val iepTaskSceneCode = sceneCode
                        val iepTaskType = groupId

                        val finishResult = XLightRpcCall.finishTask(
                            playBizId = playingBizId,
                            playEventInfo = playEventInfo,
                            iepTaskSceneCode = iepTaskSceneCode,
                            iepTaskType = iepTaskType
                        )

                        val fr = JSONObject(finishResult)

                        if (ResChecker.checkRes(TAG, fr)) {
                            Log.record(TAG, "finishTask 完成成功 → 浏览广告任务完成")
                        } else {
                            Log.error(TAG, "finishTask 完成失败: $finishResult")
                        }
                    }

                    else -> {
                        Log.error(TAG, "无法处理的任务类型：$childTaskId | actionType=$actionType")
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