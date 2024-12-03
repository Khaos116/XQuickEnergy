package io.github.lazyimmortal.sesame.util;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;


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
    //io.github.lazyimmortal.sesame.entity.UserEntity user = io.github.lazyimmortal.sesame.util.idMap.UserIdMap.get(uid);
    //if (user != null) {
    //  Log.record("加载用户:" + user.getNickName());
    //} else {
    //  String userName = io.github.lazyimmortal.sesame.hook.ApplicationHook.getUserName();
    //  if (TextUtils.isEmpty(userName)) {
    //    Log.record("加载用户昵称:" + userName);
    //  } else {
    //    Log.record("加载用户ID:" + uid);
    //  }
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

  //放到ApplicationHook
  //private static Object queryAllCombinedAccount() {
  //  try {
  //    return XposedHelpers.callMethod(getServiceObject(XposedHelpers.findClass("com.alipay.mobile.personalbase.service.SocialSdkContactService", classLoader).getName()), "queryAllCombinedAccount", 1, false);
  //  } catch (Throwable th) {
  //    Log.i(TAG, "getUserObject err");
  //    Log.printStackTrace(TAG, th);
  //  }
  //  return null;
  //}
  //
  //public static String getUserName() {
  //  Object object = queryAllCombinedAccount();
  //  if (object instanceof List<?>) {
  //    List<?> list = ((List<?>) object);
  //    if (!list.isEmpty()) {
  //      Object first = list.get(0);//com.alipay.mobile.personalbase.model.MobileRecordAccount
  //      try {
  //        if (first != null) {
  //          return (String) XposedHelpers.getObjectField(first, "nickName");
  //        }
  //      } catch (Throwable th) {
  //        Log.i(TAG, "getUserName err");
  //        Log.printStackTrace(TAG, th);
  //      }
  //    }
  //  }
  //  return null;
  //}
}
