package fansirsqi.xposed.sesame.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import fansirsqi.xposed.sesame.hook.ApplicationHook
import fansirsqi.xposed.sesame.util.maps.UserMap
import org.json.JSONObject
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone

/**
 * Author:Khaos116
 * Date:2026/1/20
 * Time:15:41
 */
object MyUtils {
  //为了方便快速找到处理本地修改的代码
  const val CHANGE_KT1 = "1"
  const val CHANGE_KT2 = "2"
  const val CHANGE_KT3 = "3"
  const val CHANGE_KT4 = "4"
  const val CHANGE_KT5 = "5"
  const val CHANGE_KT6 = "6"
  const val CHANGE_KT7 = "7"
  const val CHANGE_KT8 = "8"
  const val CHANGE_KT9 = "9"
  const val CHANGE_KT10 = "10"
  const val CHANGE_KT11 = "11"
  const val CHANGE_KT12 = "12"
  const val CHANGE_KT13 = "13"
  const val CHANGE_KT14 = "14"
  const val CHANGE_KT15 = "15"
  const val CHANGE_KT16 = "16"
  const val CHANGE_KT17 = "17"
  const val CHANGE_KT18 = "18"
  const val CHANGE_KT19 = "19"

  //还未使用的
  const val CHANGE_KT20 = "20"
  const val CHANGE_KT21 = "21"
  const val CHANGE_KT22 = "22"
  const val CHANGE_KT23 = "23"
  const val CHANGE_KT24 = "24"
  const val CHANGE_KT25 = "25"
  const val CHANGE_KT26 = "26"
  const val CHANGE_KT27 = "27"
  const val CHANGE_KT28 = "28"
  const val CHANGE_KT29 = "29"

  const val _访问被拒绝1 = "alipay.mrchservbase.mrchbusiness.sign.transcode.check_1"
  const val _系统出错1 = "alipay.antforest.forest.h5.queryPropList_1"
  const val NO_SLEEP: String = "canSleepXXX"

  @JvmStatic
  var _关闭必弹验证1: Boolean = System.currentTimeMillis() > 0

  @JvmStatic
  var _关闭必弹验证2: Boolean = System.currentTimeMillis() > 0

  @JvmStatic
  var _关闭不支持RPC1: Boolean = System.currentTimeMillis() > 0

  @JvmStatic
  var _关闭不支持RPC2: Boolean = System.currentTimeMillis() > 0

  @JvmStatic
  var _关闭作弊广告流量: Boolean = System.currentTimeMillis() > 0

  @JvmStatic
  fun getInstance(): Calendar {
    return Calendar.getInstance(TimeZone.getTimeZone("GMT+8"))
  }

  var mSP: SharedPreferences? = null

  fun getSp功能异常(key: String): Boolean {
    val sp: SharedPreferences = getMySp() ?: return true
    return sp.getBoolean(key, false)
  }

  fun setSp功能异常(key: String, jo: JSONObject?) {
    if (jo == null) return
    //{"error":1009,"errorMessage":"访问被拒绝","errorNo":3,"errorTip":"1009"}
    //{"error":3000,"errorMessage":"系统出错，正在排查","errorNo":3,"errorTip":"3000"}
    val errorMessage = jo.optString("errorMessage", "")
    var isError = false
    if (errorMessage.contains("访问被拒绝")) {
      isError = true
    } else if (errorMessage.contains("系统出错")) {
      isError = true
    }
    if (isError) {
      getMySp()?.edit {
        putBoolean(key, true)
      }
    }
  }

  fun getSp当天是否执行(key: String): Boolean {
    val sp: SharedPreferences = getMySp() ?: return true
    val today = ZonedDateTime.now(ZoneId.of("GMT+8")).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    return sp.getBoolean(key + "_" + today + "_" + (UserMap.currentUid ?: ""), false)
  }

  fun setSp当天是否执行(key: String, jo: JSONObject?) {
    if (jo == null) return
    //{"error":1009,"errorMessage":"为了保障您的操作安全，请进行验证后继续。","errorNo":3,"errorTip":"1009"}
    val errorMessage = jo.optString("errorMessage", "")
    var isError = false
    if (errorMessage.contains("验证后继续")) {
      isError = true
    } else if (errorMessage.contains("已经签到")) {
      isError = true
    } else if (errorMessage.contains("操作存在异常")) {
      isError = true
    } else if (errorMessage.contains("系统出错")) {
      isError = true
    }
    if (isError) {
      val today = ZonedDateTime.now(ZoneId.of("GMT+8")).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
      getMySp()?.edit {
        putBoolean(key + "_" + today + "_" + (UserMap.currentUid ?: ""), true)
      }
    }
  }

  private fun getMySp(): SharedPreferences? {
    val context: Context = ApplicationHook.appContext ?: return null
    if (mSP == null) mSP = context.getSharedPreferences("XQE_UID", Context.MODE_PRIVATE)
    return mSP
  }
}