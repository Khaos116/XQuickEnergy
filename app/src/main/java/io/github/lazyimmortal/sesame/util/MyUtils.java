package io.github.lazyimmortal.sesame.util;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import io.github.lazyimmortal.sesame.entity.UserEntity;
import io.github.lazyimmortal.sesame.util.idMap.UserIdMap;

/**
 * Author:XX
 * Date:2024/12/3
 * Time:9:09
 */
public class MyUtils {
  //打印用户切换
  public static void recordUserName(@Nullable String uid) {
    if (TextUtils.isEmpty(uid)) return;
    UserEntity user = UserIdMap.get(uid);
    if (user != null) {
      Log.record("加载用户:" + user.getNickName());
    }
  }
}
