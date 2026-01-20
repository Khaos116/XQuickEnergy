package fansirsqi.xposed.sesame.util

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
  const val CHANGE_KT20 = "20"

  //还未使用的
  const val CHANGE_KT21 = "21"
  const val CHANGE_KT22 = "22"
  const val CHANGE_KT23 = "23"
  const val CHANGE_KT24 = "24"
  const val CHANGE_KT25 = "25"
  const val CHANGE_KT26 = "26"
  const val CHANGE_KT27 = "27"
  const val CHANGE_KT28 = "28"
  const val CHANGE_KT29 = "29"

  @JvmStatic
  fun getInstance(): Calendar {
    return Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
  }
}