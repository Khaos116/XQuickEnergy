package tkaxv7s.xposed.sesame.util;

import android.app.Notification;
import android.app.NotificationManager;

import java.util.*;

import tkaxv7s.xposed.sesame.data.ConfigV2;

/**
 * 在IDEA中添加@Getter注解后报红问题解决方案：需要在IDEA中安装lombok插件，并引入 lombok的依赖即可
 * Author:XX
 * Date:2024/6/20
 * Time:10:57
 */
public class MyChangeUtils {
  //取消通知栏折叠
  public static void innerSetContentText(
      Long nextExecTime,
      Notification.Builder builder,
      CharSequence contentText,
      NotificationManager mNotifyManager
  ) {
    String preContent = (nextExecTime > 0) ? "下次扫描" + TimeUtil.getTimeStr(nextExecTime) + "\n" : "";
    builder.setContentTitle("芝麻粒 " + preContent);
    builder.setContentText(contentText);
    if (mNotifyManager != null) {
      mNotifyManager.notify(NotificationUtil.NOTIFICATION_ID, builder.build());
    }
  }

  //设置时区为GMT+8
  public static Calendar getInstance() {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 设置时区为东八区（北京时间）
    return calendar;
  }

  //关闭一键捉鸡
  public static void batchHireAnimalRecommend() {

  }

  public static boolean fixCalendarHasNull(Calendar c1, Calendar c2, Calendar c3) {
    return c1 == null || c2 == null || c3 == null;
  }
}
