package io.github.lazyimmortal.sesame.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Date:2024/12/3
 * Time:9:09
 */
public class MyUtils {
  //用户id、昵称对应表
  public static final HashMap<String, String> mUidMap = new HashMap<>();
  private static SharedPreferences mSP = null;
  //修改参数，不让小鸡自动睡觉
  public static final String NO_SLEEP = "canSleepXXX";
  //关闭H5的新设置页面
  public static final boolean closeNewSetting = true;

  //是否显示首页弹窗
  public static boolean showHomeDialog() {
    return false;
  }

  //打印用户切换
  public static void recordUserName(@Nullable Context context, @Nullable String uid) {
    if (context == null) return;
    if (TextUtils.isEmpty(uid)) return;
    String name = mUidMap.get(uid);
    if (mSP == null) mSP = context.getSharedPreferences("XQE_UID", Context.MODE_PRIVATE);
    if (!TextUtils.isEmpty(name)) {
      mSP.edit().putString(uid, name).apply();//保存以便下次访问
      Log.record("加载用户:" + name);
    } else {
      String spName = mSP.getString(uid, "");
      if (TextUtils.isEmpty(spName)) {
        Log.record("加载用户ID:" + uid);
      } else {
        mUidMap.put(uid, spName);
        Log.record("加载用户:" + spName);
      }
    }
  }

  //首页显示全部记录，菜单显示其他记录
  public static boolean showHomeAllLog() {
    return true;
  }

  //处理可能为空
  public static @Nullable JSONArray antSportTaskListMaybeNull(JSONObject jo) {
    return jo.optJSONArray("taskList");
  }

  //处理可能为空
  public static @Nullable JSONArray antSportUserExchangeRecordsMaybeNull(JSONObject jo) {
    return jo.optJSONArray("userExchangeRecords");
  }

  //处理可能为空
  public static String antFarmGroupIdMaybeEmpty(JSONObject jo) {
    return jo.optString("groupId");
  }

  //处理可能为空
  public static @Nullable JSONObject antFarmSleepNotifyInfoMaybeNull(JSONObject jo) {
    return jo.optJSONObject("sleepNotifyInfo");
  }

  //处理可能为空
  public static long antForestV2NowMaybeNull(JSONObject jo) {
    long time = jo.optLong("now");
    if (time == 0) {
      return System.currentTimeMillis();
    } else {
      return time;
    }
  }

  //发现有不支持的BizSubType
  public static boolean antMemberUnsSupportBizSubType(String subType) {
    if (TextUtils.isEmpty(subType)) return false;
    return !TextUtils.equals(subType, "ngfe_tag__ptr3o4eriu");
  }
}
