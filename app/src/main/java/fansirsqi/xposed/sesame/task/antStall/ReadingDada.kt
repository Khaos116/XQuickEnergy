package fansirsqi.xposed.sesame.task.antStall

import fansirsqi.xposed.sesame.model.ModelGroup
import fansirsqi.xposed.sesame.task.AnswerAI.AnswerAI
import fansirsqi.xposed.sesame.util.JsonUtil
import fansirsqi.xposed.sesame.util.Log
import fansirsqi.xposed.sesame.util.MyUtils
import org.json.JSONObject

/**
 * @file ReadingDada.kt
 * @brief 阅读答题功能模块
 * @author
 * @since 2023/08/22
 */
object ReadingDada {
    private const val TAG = "ReadingDada"

    val group: ModelGroup = ModelGroup.STALL

    /**
     * @brief 回答问题
     * @param bizInfo 业务信息JSON对象
     * @return 是否回答成功
     */
    fun answerQuestion(bizInfo: JSONObject): Boolean {
        try {
            // 获取任务跳转URL
            val taskJumpUrl = bizInfo.optString("taskJumpUrl").takeIf { it.isNotEmpty() }
                ?: bizInfo.optString("targetUrl")

            // 解析活动ID
            val activityId = taskJumpUrl.split("activityId%3D")[1].split("%26")[0]

            // 解析外部业务ID
            val outBizId = if (taskJumpUrl.contains("outBizId%3D")) {
                taskJumpUrl.split("outBizId%3D")[1].split("%26")[0]
            } else {
                ""
            }

            // 获取问题
            val questionResponse = ReadingDadaRpcCall.getQuestion(activityId)
            val questionJson = MyUtils.myJSONObject(questionResponse)

            if (questionJson.optString("resultCode") == "200") {
                val options = questionJson.getJSONArray("options")
                val question = questionJson.optString("title")

                // 使用AI获取答案
                var answer = AnswerAI.getAnswer(
                    question,
                    JsonUtil.jsonArrayToList(options),
                    "other"
                )

                // 如果AI未返回答案,使用第一个选项
                if (answer.isNullOrEmpty()) {
                    answer = options.optString(0)
                }

                // 提交答案
                val submitResponse = ReadingDadaRpcCall.submitAnswer(
                    activityId,
                    outBizId,
                    questionJson.optString("questionId"),
                    answer
                )

                val submitJson = MyUtils.myJSONObject(submitResponse)
                return if (submitJson.optString("resultCode") == "200") {
                    Log.record(TAG, "答题完成")
                    true
                } else {
                    Log.error(TAG, "答题失败")
                    false
                }
            } else {
                Log.error(TAG, "获取问题失败")
            }
        } catch (e: Throwable) {
            Log.printStackTrace(TAG, "answerQuestion err:", e)
        }
        return false
    }
}
