package io.github.lazyimmortal.sesame.util;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.lazyimmortal.sesame.entity.UserEntity;
import io.github.lazyimmortal.sesame.util.idMap.UserIdMap;

/**
 * Date:2024/12/3
 * Time:9:09
 */
public class MyUtils {
  public static final String NO_SLEEP = "canSleepXXX";

  //是否显示首页弹窗
  public static boolean showHomeDialog() {
    return false;
  }

  //打印用户切换
  public static void recordUserName(@Nullable String uid) {
    if (TextUtils.isEmpty(uid)) return;
    //UserEntity user = UserIdMap.get(uid);
    //if (user != null) {
    //  Log.record("加载用户:" + user.getNickName());
    //}
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
  public static String antFarmGroupIdMaybeEmpty(JSONObject jo) {
    return jo.optString("groupId");
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
}
