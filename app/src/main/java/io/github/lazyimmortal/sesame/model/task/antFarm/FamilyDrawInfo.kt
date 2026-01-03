package io.github.lazyimmortal.sesame.model.task.antFarm

import io.github.lazyimmortal.sesame.data.modelFieldExt.SelectModelField
import io.github.lazyimmortal.sesame.data.modelFieldExt.StringModelField
import io.github.lazyimmortal.sesame.util.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Objects

/**
 * Author:khaos116
 * Date:2026/1/4
 * Time:5:06
 */
object FamilyDrawInfo {
  private val TAG = this::class.java.simpleName

  /**
   * 家庭扭蛋任务
   * @param friendUserIds 好友用户ID列表
   * @param familyDrawInfo 家庭扭蛋信息
   */
  @JvmStatic
  fun familyDrawTask(familyOptions: SelectModelField?, giftFamilyDrawFragment: StringModelField?, friendUserIds: MutableList<String?>, familyDrawInfo: JSONObject) {
    try {
      val listFarmTask = familyDrawListFarmTask() ?: return
      for (i in 0..<listFarmTask.length()) {
        val jo = listFarmTask.getJSONObject(i)
        val taskStatus = TaskStatus.valueOf(jo.getString("taskStatus"))
        val taskId = jo.optString("taskId")
        val title = jo.optString("title")
        if (taskStatus == TaskStatus.RECEIVED) {
          continue
        }
        if (taskStatus == TaskStatus.TODO && taskId == "FAMILY_DRAW_VISIT_TASK"
          && familyOptions?.value?.contains("batchInviteP2P") == true
        ) {
          //分享
          familyBatchInviteP2PTask(friendUserIds, familyDrawInfo)
          continue
        }
        if (taskStatus == TaskStatus.FINISHED && taskId == "FAMILY_DRAW_FREE_TASK") {
          //签到
          familyDrawSignReceiveFarmTaskAward(taskId, title)
          continue
        }
        GlobalThreadPools.sleepCompat(1000)
      }
      val jo = JSONObject(AntFarmRpcCall.queryFamilyDrawActivity())
      if (ResChecker.checkRes(TAG, jo)) {
        GlobalThreadPools.sleepCompat(1000)
        val drawTimes = jo.optInt("familyDrawTimes")
        //碎片个数
        val giftNum = jo.optInt("mengliFragmentCount")
        if (giftNum >= 20 && !(giftFamilyDrawFragment?.value).isNullOrEmpty()) {
          giftFamilyDrawFragment(giftFamilyDrawFragment.value, giftNum)
        }
        for (i in 0..<drawTimes) {
          if (!familyDraw()) {
            return
          }
          GlobalThreadPools.sleepCompat(1500)
        }
      }
    } catch (e: Exception) {
      // 协程取消异常必须重新抛出，不能吞掉
      Log.debug("familyDrawTask异常:${e.message}")
      throw e
    } catch (t: Throwable) {
      Log.printStackTrace(TAG, "familyDrawTask err:", t)
    }
  }

  /**
   * 家庭批量邀请P2P任务
   * @param friendUserIds 好友用户ID列表
   * @param familyDrawInfo 家庭扭蛋信息
   */
  private fun familyBatchInviteP2PTask(
    friendUserIds: MutableList<String?>,
    familyDrawInfo: JSONObject
  ) {
    try {
      if (Status.hasFlagToday("antFarm::familyBatchInviteP2P")) {
        return
      }
      if (Objects.isNull(friendUserIds) || friendUserIds.isEmpty()) {
        return
      }
      val activityId = familyDrawInfo.optString("activityId")
      val sceneCode = "ANTFARM_FD_VISIT_$activityId"
      var jo = JSONObject(AntFarmRpcCall.familyShareP2PPanelInfo(sceneCode))
      if (ResChecker.checkRes(TAG, jo)) {
        val p2PFriendVOList = jo.getJSONArray("p2PFriendVOList")
        if (Objects.isNull(p2PFriendVOList) || p2PFriendVOList.length() <= 0) {
          return
        }
        val inviteP2PVOList = JSONArray()
        for (i in 0..<p2PFriendVOList.length()) {
          if (inviteP2PVOList.length() < 6) {
            val `object` = JSONObject()
            `object`.put(
              "beInvitedUserId",
              p2PFriendVOList.getJSONObject(i).getString("userId")
            )
            `object`.put("bizTraceId", "")
            inviteP2PVOList.put(`object`)
          }
          if (inviteP2PVOList.length() >= 6) {
            break
          }
        }
        jo = JSONObject(AntFarmRpcCall.familyBatchInviteP2P(inviteP2PVOList, sceneCode))
        if (ResChecker.checkRes(TAG, jo)) {
          Log.farm("亲密家庭🏠提交任务[好友串门送扭蛋]")
          Status.setFlagToday("antFarm::familyBatchInviteP2P")
          GlobalThreadPools.sleepCompat(500)
        }
      }
    } catch (e: Exception) {
      // 协程取消异常必须重新抛出，不能吞掉
      Log.debug("familyBatchInviteP2PTask异常:${e.message}")
      throw e
    } catch (t: Throwable) {
      Log.printStackTrace(TAG, "familyBatchInviteP2PTask err:", t)
    }
  }

  private fun familyDrawSignReceiveFarmTaskAward(taskId: String?, title: String?) {
    try {
      val jo = JSONObject(AntFarmRpcCall.familyDrawSignReceiveFarmTaskAward(taskId))
      if (ResChecker.checkRes(TAG, jo)) {
        Log.farm("亲密家庭🏠扭蛋任务#$title#奖励领取成功")
      }
    } catch (e: Exception) {
      // 协程取消异常必须重新抛出，不能吞掉
      Log.debug("familyDrawSignReceiveFarmTaskAward异常:${e.message}")
      throw e
    } catch (t: Throwable) {
      Log.printStackTrace(TAG, "familyDrawSignReceiveFarmTaskAward err:", t)
    }
  }

  private fun giftFamilyDrawFragment(giftUserId: String?, giftNum: Int) {
    try {
      val jo = JSONObject(AntFarmRpcCall.giftFamilyDrawFragment(giftUserId, giftNum))
      if (ResChecker.checkRes(TAG, jo)) {
        Log.farm("亲密家庭🏠赠送扭蛋碎片#" + giftNum + "个#" + giftUserId)
      }
    } catch (t: Throwable) {
      Log.printStackTrace(TAG, "giftFamilyDrawFragment err:", t)
    }
  }

  private fun familyDrawListFarmTask(): JSONArray? {
    try {
      val jo = JSONObject(AntFarmRpcCall.familyDrawListFarmTask())
      if (ResChecker.checkRes(TAG, jo)) {
        return jo.getJSONArray("farmTaskList")
      }
    } catch (t: Throwable) {
      Log.printStackTrace(TAG, "familyDrawListFarmTask err:", t)
    }
    return null
  }

  /**
   * 家庭扭蛋抽奖
   * @return 是否还有剩余抽奖次数
   */
  private fun familyDraw(): Boolean {
    try {
      val jo = JSONObject(AntFarmRpcCall.familyDraw())
      if (ResChecker.checkRes(TAG, jo)) {
        val familyDrawPrize = jo.getJSONObject("familyDrawPrize")
        val title = familyDrawPrize.optString("title")
        val awardCount = familyDrawPrize.getString("awardCount")
        val familyDrawTimes = jo.optInt("familyDrawTimes")
        Log.farm("开扭蛋🎟️抽中[$title]#[$awardCount]")
        return familyDrawTimes != 0
      }
    } catch (e: Exception) {
      // 协程取消异常必须重新抛出，不能吞掉
      Log.debug("familyDraw异常:${e.message}")
      throw e
    } catch (t: Throwable) {
      Log.printStackTrace(TAG, "familyDraw err:", t)
    }
    return false
  }
}