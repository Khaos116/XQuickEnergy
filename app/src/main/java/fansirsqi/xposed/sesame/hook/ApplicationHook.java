package fansirsqi.xposed.sesame.hook;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import fansirsqi.xposed.sesame.BuildConfig;
import fansirsqi.xposed.sesame.data.Config;
import fansirsqi.xposed.sesame.data.RunType;
import fansirsqi.xposed.sesame.data.ViewAppInfo;
import fansirsqi.xposed.sesame.entity.AlipayVersion;
import fansirsqi.xposed.sesame.entity.FriendWatch;
import fansirsqi.xposed.sesame.entity.RpcEntity;
import fansirsqi.xposed.sesame.model.BaseModel;
import fansirsqi.xposed.sesame.model.Model;
import fansirsqi.xposed.sesame.rpc.bridge.NewRpcBridge;
import fansirsqi.xposed.sesame.rpc.bridge.OldRpcBridge;
import fansirsqi.xposed.sesame.rpc.bridge.RpcBridge;
import fansirsqi.xposed.sesame.rpc.bridge.RpcVersion;
import fansirsqi.xposed.sesame.rpc.debug.DebugRpc;
import fansirsqi.xposed.sesame.rpc.intervallimit.RpcIntervalLimit;
import fansirsqi.xposed.sesame.task.BaseTask;
import fansirsqi.xposed.sesame.task.ModelTask;
import fansirsqi.xposed.sesame.task.TaskCommon;
import fansirsqi.xposed.sesame.task.antMember.AntMemberRpcCall;
import fansirsqi.xposed.sesame.util.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

public class ApplicationHook implements IXposedHookLoadPackage {

  static final String TAG = ApplicationHook.class.getSimpleName();

  @Getter private static final String modelVersion = BuildConfig.VERSION_NAME;

  static final Map<Object, Object[]> rpcHookMap = new ConcurrentHashMap<>();

  private static final Map<String, PendingIntent> wakenAtTimeAlarmMap = new ConcurrentHashMap<>();

  @Getter private static ClassLoader classLoader = null;

  @Getter private static Object microApplicationContextObject = null;

  @Getter
  @SuppressLint("StaticFieldLeak")
  static Context context = null;

  @Getter static AlipayVersion alipayVersion = new AlipayVersion("");

  @Getter private static volatile boolean hooked = false;

  static volatile boolean init = false;

  static volatile Calendar dayCalendar;

  @Getter static volatile boolean offline = false;

  @Getter static final AtomicInteger reLoginCount = new AtomicInteger(0);

  @SuppressLint("StaticFieldLeak")
  static Service service;

  @Getter static Handler mainHandler;

  static BaseTask mainTask;

  private static RpcBridge rpcBridge;

  @Getter private static RpcVersion rpcVersion;

  private static PowerManager.WakeLock wakeLock;

  private static PendingIntent alarm0Pi;

  private static XC_MethodHook.Unhook rpcRequestUnhook;

  private static XC_MethodHook.Unhook rpcResponseUnhook;

  public static void setOffline(boolean offline) {
    ApplicationHook.offline = offline;
  }

  @Override
  public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
    if ("fansirsqi.xposed.sesame".equals(lpparam.packageName)) {
      try {
        XposedHelpers.callStaticMethod(lpparam.classLoader.loadClass(ViewAppInfo.class.getName()), "setRunTypeByCode", RunType.MODEL.getCode());
      } catch (ClassNotFoundException e) {
        LogUtil.printStackTrace(e);
      }
    }
    else if (ClassUtil.PACKAGE_NAME.equals(lpparam.packageName) && ClassUtil.PACKAGE_NAME.equals(lpparam.processName)) {
      if (hooked) return;
      classLoader = lpparam.classLoader;
      XposedHelpers.findAndHookMethod(
          Application.class,
          "attach",
          Context.class,
          new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
              context = (Context) param.args[0];
              try {
                alipayVersion = new AlipayVersion(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
              } catch (Exception e) {
                LogUtil.printStackTrace(e);
              }
              super.afterHookedMethod(param);
            }
          });
      try {
        XposedHelpers.findAndHookMethod(
            "com.alipay.mobile.nebulaappproxy.api.rpc.H5AppRpcUpdate",
            classLoader,
            "matchVersion",
            classLoader.loadClass(ClassUtil.H5PAGE_NAME),
            Map.class,
            String.class,
            XC_MethodReplacement.returnConstant(false));
        LogUtil.runtime(TAG, "hook matchVersion successfully");
      } catch (Throwable t) {
        LogUtil.runtime(TAG, "hook matchVersion err:");
        LogUtil.printStackTrace(TAG, t);
      }
      try {
        XposedHelpers.findAndHookMethod(
            "com.alipay.mobile.quinox.LauncherActivity",
            classLoader,
            "onResume",
            new XC_MethodHook() {
              @Override
              protected void afterHookedMethod(MethodHookParam param) {
                LogUtil.runtime(TAG, "Activity onResume");
                String targetUid = getUserId();
                if (targetUid == null) {
                  LogUtil.record("用户未登录");
                  Toast.show("用户未登录");
                  return;
                }
                if (!init) {
                  if (initHandler(true)) {
                    init = true;
                  }
                  return;
                }
                String currentUid = UserIdMapUtil.getCurrentUid();
                if (!targetUid.equals(currentUid)) {
                  if (currentUid != null) {
                    initHandler(true);
                    LogUtil.record("用户已切换");
                    Toast.show("用户已切换");
                    return;
                  }
                  UserIdMapUtil.initUser(targetUid);
                }
                if (offline) {
                  offline = false;
                  execHandler();
                  ((Activity) param.thisObject).finish();
                  LogUtil.runtime(TAG, "Activity reLogin");
                }
              }
            });
        LogUtil.runtime(TAG, "hook login successfully");
      } catch (Throwable t) {
        LogUtil.runtime(TAG, "hook login err:");
        LogUtil.printStackTrace(TAG, t);
      }
      try {
        XposedHelpers.findAndHookMethod(
            "android.app.Service",
            classLoader,
            "onCreate",
            new XC_MethodHook() {
              @SuppressLint("WakelockTimeout")
              @Override
              protected void afterHookedMethod(MethodHookParam param) {
                Service appService = (Service) param.thisObject;
                if (!ClassUtil.CURRENT_USING_SERVICE.equals(appService.getClass().getCanonicalName())) {
                  return;
                }
                LogUtil.runtime(TAG, "Service onCreate");
                context = appService.getApplicationContext();
                service = appService;
                mainHandler = new Handler();
                mainTask =
                    BaseTask.newInstance(
                        "MAIN_TASK",
                        new Runnable() {
                          private volatile long lastExecTime = 0;
                          @Override
                          public void run() {
                            if (!init) {
                              return;
                            }
                            LogUtil.record("应用版本：" + alipayVersion.getVersionString());
                            LogUtil.record("模块版本：" + modelVersion);
                            LogUtil.record("开始执行");
                            try {
                              int checkInterval = BaseModel.getCheckInterval().getValue();
                              if (lastExecTime + 2000 > System.currentTimeMillis()) {
                                LogUtil.record("执行间隔较短，跳过执行");
                                execDelayedHandler(checkInterval);
                                return;
                              }
                              updateDay();
                              String targetUid = getUserId();
                              String currentUid = UserIdMapUtil.getCurrentUid();
                              if (targetUid == null || currentUid == null) {
                                LogUtil.record("用户为空，放弃执行");
                                reLogin();
                                return;
                              }
                              if (!targetUid.equals(currentUid)) {
                                LogUtil.record("开始切换用户");
                                Toast.show("开始切换用户");
                                reLogin();
                                return;
                              }
                              lastExecTime = System.currentTimeMillis();
                              try {
                                FutureTask<Boolean> checkTask = new FutureTask<>(AntMemberRpcCall::check);
                                Thread checkThread = new Thread(checkTask);
                                checkThread.start();
                                if (!checkTask.get(10, TimeUnit.SECONDS)) {
                                  long waitTime = 10000 - System.currentTimeMillis() + lastExecTime;
                                  if (waitTime > 0) {
                                    Thread.sleep(waitTime);
                                  }
                                  LogUtil.record("执行失败：检查超时");
                                  reLogin();
                                  return;
                                }
                                reLoginCount.set(0);
                              } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                LogUtil.record("执行失败：检查中断");
                                reLogin();
                                return;
                              } catch (Exception e) {
                                LogUtil.record("执行失败：检查异常");
                                reLogin();
                                LogUtil.printStackTrace(TAG, e);
                                return;
                              }
                              TaskCommon.update();
                              ModelTask.startAllTask(false);
                              lastExecTime = System.currentTimeMillis();
                              try {
                                // 定时执行的时间列表
                                List<String> execAtTimeList = BaseModel.getExecAtTimeList().getValue();
                                if (execAtTimeList != null) {
                                  Calendar lastExecTimeCalendar = TimeUtil.getCalendarByTimeMillis(lastExecTime);
                                  Calendar nextExecTimeCalendar = TimeUtil.getCalendarByTimeMillis(lastExecTime + checkInterval);
                                  for (String execAtTime : execAtTimeList) {
                                    Calendar execAtTimeCalendar = TimeUtil.getTodayCalendarByTimeStr(execAtTime);
                                    if (execAtTimeCalendar != null
                                        && lastExecTimeCalendar.compareTo(execAtTimeCalendar) < 0
                                        && nextExecTimeCalendar.compareTo(execAtTimeCalendar) > 0) {
                                      LogUtil.record("设置定时执行:" + execAtTime);
                                      execDelayedHandler(execAtTimeCalendar.getTimeInMillis() - lastExecTime);
                                      FileUtil.clearLog();
                                      return;
                                    }
                                  }
                                }
                              } catch (Exception e) {
                                LogUtil.runtime(TAG, "execAtTime err:");
                                LogUtil.printStackTrace(TAG, e);
                              }

                              execDelayedHandler(checkInterval);
                              FileUtil.clearLog();
                            } catch (Exception e) {
                              LogUtil.record("执行异常:");
                              LogUtil.printStackTrace(e);
                            }
                          }
                        });
                registerBroadcastReceiver(appService);
                dayCalendar = Calendar.getInstance();
                StatisticsUtil.load();
                FriendWatch.load();
                if (initHandler(true)) {
                  init = true;
                }
              }
            });
        LogUtil.runtime(TAG, "hook service onCreate successfully");
      } catch (Throwable t) {
        LogUtil.runtime(TAG, "hook service onCreate err:");
        LogUtil.printStackTrace(TAG, t);
      }
      try {
        XposedHelpers.findAndHookMethod(
            "android.app.Service",
            classLoader,
            "onDestroy",
            new XC_MethodHook() {
              @Override
              protected void afterHookedMethod(MethodHookParam param) {
                Service service = (Service) param.thisObject;
                if (!ClassUtil.CURRENT_USING_SERVICE.equals(service.getClass().getCanonicalName())) {
                  return;
                }
                LogUtil.record("支付宝前台服务被销毁");
                NotificationUtil.updateStatusText("支付宝前台服务被销毁");
                destroyHandler(true);
                FriendWatch.unload();
                StatisticsUtil.unload();
                restartByBroadcast();
              }
            });
      } catch (Throwable t) {
        LogUtil.runtime(TAG, "hook service onDestroy err:");
        LogUtil.printStackTrace(TAG, t);
      }
      try {
        XposedHelpers.findAndHookMethod("com.alipay.mobile.common.fgbg.FgBgMonitorImpl", classLoader, "isInBackground", XC_MethodReplacement.returnConstant(false));
      } catch (Throwable t) {
        LogUtil.runtime(TAG, "hook FgBgMonitorImpl method 1 err:");
        LogUtil.printStackTrace(TAG, t);
      }
      try {
        XposedHelpers.findAndHookMethod("com.alipay.mobile.common.fgbg.FgBgMonitorImpl", classLoader, "isInBackground", boolean.class, XC_MethodReplacement.returnConstant(false));
      } catch (Throwable t) {
        LogUtil.runtime(TAG, "hook FgBgMonitorImpl method 2 err:");
        LogUtil.printStackTrace(TAG, t);
      }
      try {
        XposedHelpers.findAndHookMethod("com.alipay.mobile.common.fgbg.FgBgMonitorImpl", classLoader, "isInBackgroundV2", XC_MethodReplacement.returnConstant(false));
      } catch (Throwable t) {
        LogUtil.runtime(TAG, "hook FgBgMonitorImpl method 3 err:");
        LogUtil.printStackTrace(TAG, t);
      }
      try {
        XposedHelpers.findAndHookMethod(
            "com.alipay.mobile.common.transport.utils.MiscUtils",
            classLoader,
            "isAtFrontDesk",
            classLoader.loadClass("android.content.Context"),
            XC_MethodReplacement.returnConstant(true));
        LogUtil.runtime(TAG, "hook MiscUtils successfully");
      } catch (Throwable t) {
        LogUtil.runtime(TAG, "hook MiscUtils err:");
        LogUtil.printStackTrace(TAG, t);
      }
      hooked = true;
      LogUtil.runtime(TAG, "load success: " + lpparam.packageName);
    }
  }

  private static void setWakenAtTimeAlarm() {
    try {
      unsetWakenAtTimeAlarm();
      try {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.eg.android.AlipayGphone.sesame.execute"), getPendingIntentFlag());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (setAlarmTask(calendar.getTimeInMillis(), pendingIntent)) {
          alarm0Pi = pendingIntent;
          LogUtil.record("设置定时唤醒:0|000000");
        }
      } catch (Exception e) {
        LogUtil.runtime(TAG, "setWakenAt0 err:");
        LogUtil.printStackTrace(TAG, e);
      }
      List<String> wakenAtTimeList = BaseModel.getWakenAtTimeList().getValue();
      if (wakenAtTimeList != null && !wakenAtTimeList.isEmpty()) {
        Calendar nowCalendar = Calendar.getInstance();
        for (int i = 1, len = wakenAtTimeList.size(); i < len; i++) {
          try {
            String wakenAtTime = wakenAtTimeList.get(i);
            Calendar wakenAtTimeCalendar = TimeUtil.getTodayCalendarByTimeStr(wakenAtTime);
            if (wakenAtTimeCalendar != null) {
              if (wakenAtTimeCalendar.compareTo(nowCalendar) > 0) {
                PendingIntent wakenAtTimePendingIntent = PendingIntent.getBroadcast(context, i, new Intent("com.eg.android.AlipayGphone.sesame.execute"), getPendingIntentFlag());
                if (setAlarmTask(wakenAtTimeCalendar.getTimeInMillis(), wakenAtTimePendingIntent)) {
                  String wakenAtTimeKey = i + "|" + wakenAtTime;
                  wakenAtTimeAlarmMap.put(wakenAtTimeKey, wakenAtTimePendingIntent);
                  LogUtil.record("设置定时唤醒:" + wakenAtTimeKey);
                }
              }
            }
          } catch (Exception e) {
            LogUtil.runtime(TAG, "setWakenAtTime err:");
            LogUtil.printStackTrace(TAG, e);
          }
        }
      }
    } catch (Exception e) {
      LogUtil.runtime(TAG, "setWakenAtTimeAlarm err:");
      LogUtil.printStackTrace(TAG, e);
    }
  }

  private static void unsetWakenAtTimeAlarm() {
    try {
      for (Map.Entry<String, PendingIntent> entry : wakenAtTimeAlarmMap.entrySet()) {
        try {
          String wakenAtTimeKey = entry.getKey();
          PendingIntent wakenAtTimePendingIntent = entry.getValue();
          if (unsetAlarmTask(wakenAtTimePendingIntent)) {
            wakenAtTimeAlarmMap.remove(wakenAtTimeKey);
            LogUtil.record("取消定时唤醒:" + wakenAtTimeKey);
          }
        } catch (Exception e) {
          LogUtil.runtime(TAG, "unsetWakenAtTime err:");
          LogUtil.printStackTrace(TAG, e);
        }
      }
      try {
        if (unsetAlarmTask(alarm0Pi)) {
          alarm0Pi = null;
          LogUtil.record("取消定时唤醒:0|000000");
        }
      } catch (Exception e) {
        LogUtil.runtime(TAG, "unsetWakenAt0 err:");
        LogUtil.printStackTrace(TAG, e);
      }
    } catch (Exception e) {
      LogUtil.runtime(TAG, "unsetWakenAtTimeAlarm err:");
      LogUtil.printStackTrace(TAG, e);
    }
  }

  @SuppressLint("WakelockTimeout")
  synchronized Boolean initHandler(Boolean force) {
    if (service == null) {
      return false;
    }
    destroyHandler(force);
    try {
      if (force) {
        String userId = getUserId();
        if (userId == null) {
          LogUtil.record("用户未登录");
          Toast.show("用户未登录");
          return false;
        }
        if (!PermissionUtil.checkAlarmPermissions()) {
          LogUtil.record("支付宝无闹钟权限");
          mainHandler.postDelayed(
              () -> {
                if (!PermissionUtil.checkOrRequestAlarmPermissions(context)) {
                  ToastUtil.makeText(context, "请授予支付宝使用闹钟权限", android.widget.Toast.LENGTH_SHORT).show();
                }
              },
              2000);
          return false;
        }
        // 检查并请求后台运行权限
        if (BaseModel.getBatteryPerm().getValue() && !init && !PermissionUtil.checkBatteryPermissions()) {
          LogUtil.record("支付宝无始终在后台运行权限");
          mainHandler.postDelayed(
              () -> {
                if (!PermissionUtil.checkOrRequestBatteryPermissions(context)) {
                  ToastUtil.makeText(context, "请授予支付宝始终在后台运行权限", android.widget.Toast.LENGTH_SHORT).show();
                }
              },
              2000);
        }
        UserIdMapUtil.initUser(userId);
        Model.initAllModel();
        LogUtil.record("模块版本：" + modelVersion);
        LogUtil.record("开始加载");
        Config.load(userId);
        if (!Model.getModel(BaseModel.class).getEnableField().getValue()) {
          LogUtil.record("芝麻粒已禁用");
          Toast.show("芝麻粒已禁用");
          return false;
        }
        if (BaseModel.getNewRpc().getValue()) {
          rpcBridge = new NewRpcBridge();
        } else {
          rpcBridge = new OldRpcBridge();
        }
        rpcBridge.load();
        rpcVersion = rpcBridge.getVersion();
        // 保持唤醒锁，防止设备休眠
        if (BaseModel.getStayAwake().getValue()) {
          try {
            PowerManager pm = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, service.getClass().getName());
            wakeLock.acquire(); // 确保唤醒锁在前台服务启动前
          } catch (Throwable t) {
            LogUtil.printStackTrace(t);
          }
        }
        // 设置闹钟
        setWakenAtTimeAlarm();
        // Hook RPC 请求和响应
        if (BaseModel.getNewRpc().getValue() && BaseModel.getDebugMode().getValue()) {
          try {
            rpcRequestUnhook =
                XposedHelpers.findAndHookMethod(
                    "com.alibaba.ariver.commonability.network.rpc.RpcBridgeExtension",
                    classLoader,
                    "rpc",
                    String.class,
                    boolean.class,
                    boolean.class,
                    String.class,
                    classLoader.loadClass(ClassUtil.JSON_OBJECT_NAME),
                    String.class,
                    classLoader.loadClass(ClassUtil.JSON_OBJECT_NAME),
                    boolean.class,
                    boolean.class,
                    int.class,
                    boolean.class,
                    String.class,
                    classLoader.loadClass("com.alibaba.ariver.app.api.App"),
                    classLoader.loadClass("com.alibaba.ariver.app.api.Page"),
                    classLoader.loadClass("com.alibaba.ariver.engine.api.bridge.model.ApiContext"),
                    classLoader.loadClass("com.alibaba.ariver.engine.api.bridge.extension.BridgeCallback"),
                    new XC_MethodHook() {
                      @SuppressLint("WakelockTimeout")
                      @Override
                      protected void beforeHookedMethod(MethodHookParam param) {
                        Object[] args = param.args;
                        Object object = args[15];
                        Object[] recordArray = new Object[4];
                        recordArray[0] = System.currentTimeMillis();
                        recordArray[1] = args[0];
                        recordArray[2] = args[4];
                        if (object != null) {
                          rpcHookMap.put(object, recordArray);
                          LogUtil.capture("记录Hook ID: " + object.hashCode() + "\n方法: " + args[0] + "\n参数: " + args[4] + "\n");
                        }
                      }

                      @SuppressLint("WakelockTimeout")
                      @Override
                      protected void afterHookedMethod(MethodHookParam param) {
                        Object object = param.args[15];
                        Object[] recordArray = rpcHookMap.remove(object);
                        if (recordArray != null) {
                          LogUtil.capture("记录\n时间: " + recordArray[0] + "\n方法: " + recordArray[1] + "\n参数: " + recordArray[2] + "\n数据: " + recordArray[3] + "\n");
                        } else {
                          LogUtil.capture("删除记录ID: " + object.hashCode());
                        }
                      }
                    });
            LogUtil.runtime(TAG, "hook record request successfully");
          } catch (Throwable t) {
            LogUtil.runtime(TAG, "hook record request err:");
            LogUtil.printStackTrace(TAG, t);
          }
          try {
            rpcResponseUnhook =
                XposedHelpers.findAndHookMethod(
                    "com.alibaba.ariver.engine.common.bridge.internal.DefaultBridgeCallback",
                    classLoader,
                    "sendJSONResponse",
                    classLoader.loadClass(ClassUtil.JSON_OBJECT_NAME),
                    new XC_MethodHook() {
                      @SuppressLint("WakelockTimeout")
                      @Override
                      protected void beforeHookedMethod(MethodHookParam param) {
                        Object object = param.thisObject;
                        Object[] recordArray = rpcHookMap.get(object);
                        if (recordArray != null) {
                          recordArray[3] = String.valueOf(param.args[0]);
                        }
                      }
                    });
            LogUtil.runtime(TAG, "hook record response successfully");
          } catch (Throwable t) {
            LogUtil.runtime(TAG, "hook record response err:");
            LogUtil.printStackTrace(TAG, t);
          }
        }
        // 启动前台服务
        NotificationUtil.start(service);
        // 启动模型
        Model.bootAllModel(classLoader);
        StatusUtil.load();
        updateDay();
        BaseModel.initData();
        LogUtil.record("加载完成");
        Toast.show("芝麻粒加载成功");
      }
      offline = false;
      execHandler();
      return true;
    } catch (Throwable th) {
      LogUtil.runtime(TAG, "startHandler err:");
      LogUtil.printStackTrace(TAG, th);
      Toast.show("芝麻粒加载失败");
      return false;
    }
  }

  static synchronized void destroyHandler(Boolean force) {
    try {
      if (force) {
        if (service != null) {
          stopHandler();
          BaseModel.destroyData();
          StatusUtil.unload();
          NotificationUtil.stop();
          RpcIntervalLimit.clearIntervalLimit();
          Config.unload();
          Model.destroyAllModel();
          UserIdMapUtil.unload();
        }
        if (rpcResponseUnhook != null) {
          try {
            rpcResponseUnhook.unhook();
          } catch (Exception e) {
            LogUtil.printStackTrace(e);
          }
        }
        if (rpcRequestUnhook != null) {
          try {
            rpcRequestUnhook.unhook();
          } catch (Exception e) {
            LogUtil.printStackTrace(e);
          }
        }
        if (wakeLock != null) {
          wakeLock.release();
          wakeLock = null;
        }
        if (rpcBridge != null) {
          rpcVersion = null;
          rpcBridge.unload();
          rpcBridge = null;
        }
      } else {
        ModelTask.stopAllTask();
      }
    } catch (Throwable th) {
      LogUtil.runtime(TAG, "stopHandler err:");
      LogUtil.printStackTrace(TAG, th);
    }
  }

  static void execHandler() {
    mainTask.startTask(false);
  }

  /**
   * 安排主任务在指定的延迟时间后执行，并更新通知中的下次执行时间。
   *
   * @param delayMillis 延迟执行的毫秒数
   */
  static void execDelayedHandler(long delayMillis) {
    // 使用主线程的Handler在指定延迟后执行一个Runnable任务，该任务启动主任务
    mainHandler.postDelayed(() -> mainTask.startTask(false), delayMillis);

    try {
      // 更新通知中的下次执行时间文本，显示为当前时间加上延迟时间
      NotificationUtil.updateNextExecText(System.currentTimeMillis() + delayMillis);
    } catch (Exception e) {
      // 如果更新通知文本时发生异常，捕获异常并打印堆栈跟踪
      LogUtil.printStackTrace(e);
    }
  }


  private static void stopHandler() {
    mainTask.stopTask();
    ModelTask.stopAllTask();
  }

  public static void updateDay() {
    Calendar nowCalendar = Calendar.getInstance();
    try {
      int nowYear = nowCalendar.get(Calendar.YEAR);
      int nowMonth = nowCalendar.get(Calendar.MONTH);
      int nowDay = nowCalendar.get(Calendar.DAY_OF_MONTH);
      if (dayCalendar.get(Calendar.YEAR) != nowYear || dayCalendar.get(Calendar.MONTH) != nowMonth || dayCalendar.get(Calendar.DAY_OF_MONTH) != nowDay) {
        dayCalendar = (Calendar) nowCalendar.clone();
        dayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        dayCalendar.set(Calendar.MINUTE, 0);
        dayCalendar.set(Calendar.SECOND, 0);
        LogUtil.record("日期更新为：" + nowYear + "-" + (nowMonth + 1) + "-" + nowDay);
        setWakenAtTimeAlarm();
      }
    } catch (Exception e) {
      LogUtil.printStackTrace(e);
    }
    try {
      StatisticsUtil.save(nowCalendar);
    } catch (Exception e) {
      LogUtil.printStackTrace(e);
    }
    try {
      StatusUtil.save(nowCalendar);
    } catch (Exception e) {
      LogUtil.printStackTrace(e);
    }
    try {
      FriendWatch.updateDay();
    } catch (Exception e) {
      LogUtil.printStackTrace(e);
    }
  }

  @SuppressLint({"ScheduleExactAlarm", "ObsoleteSdkInt"})
  private static Boolean setAlarmTask(long triggerAtMillis, PendingIntent operation) {
    try {
      AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation);
      }
      LogUtil.runtime("setAlarmTask triggerAtMillis:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(triggerAtMillis) + " operation:" + operation);
      return true;
    } catch (Throwable th) {
      LogUtil.runtime(TAG, "setAlarmTask err:");
      LogUtil.printStackTrace(TAG, th);
    }
    return false;
  }

  private static Boolean unsetAlarmTask(PendingIntent operation) {
    try {
      if (operation != null) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(operation);
      }
      return true;
    } catch (Throwable th) {
      LogUtil.runtime(TAG, "unsetAlarmTask err:");
      LogUtil.printStackTrace(TAG, th);
    }
    return false;
  }

  public static String requestString(RpcEntity rpcEntity) {
    return rpcBridge.requestString(rpcEntity, 3, -1);
  }

  public static String requestString(RpcEntity rpcEntity, int tryCount, int retryInterval) {
    return rpcBridge.requestString(rpcEntity, tryCount, retryInterval);
  }

  public static String requestString(String method, String data) {
    return rpcBridge.requestString(method, data);
  }

  public static String requestString(String method, String data, String relation) {
    return rpcBridge.requestString(method, data, relation);
  }

  public static String requestString(String method, String data, int tryCount, int retryInterval) {
    return rpcBridge.requestString(method, data, tryCount, retryInterval);
  }

  public static String requestString(String method, String data, String relation, int tryCount, int retryInterval) {
    return rpcBridge.requestString(method, data, relation, tryCount, retryInterval);
  }

  public static RpcEntity requestObject(RpcEntity rpcEntity) {
    return rpcBridge.requestObject(rpcEntity, 3, -1);
  }

  public static void requestObject(RpcEntity rpcEntity, int tryCount, int retryInterval) {
    rpcBridge.requestObject(rpcEntity, tryCount, retryInterval);
  }

  public static RpcEntity requestObject(String method, String data) {
    return rpcBridge.requestObject(method, data);
  }

  public static RpcEntity requestObject(String method, String data, String relation) {
    return rpcBridge.requestObject(method, data, relation);
  }

  public static RpcEntity requestObject(String method, String data, int tryCount, int retryInterval) {
    return rpcBridge.requestObject(method, data, tryCount, retryInterval);
  }

  public static RpcEntity requestObject(String method, String data, String relation, int tryCount, int retryInterval) {
    return rpcBridge.requestObject(method, data, relation, tryCount, retryInterval);
  }

  public static void reLoginByBroadcast() {
    try {
      context.sendBroadcast(new Intent("com.eg.android.AlipayGphone.sesame.reLogin"));
    } catch (Throwable th) {
      LogUtil.runtime(TAG, "sesame sendBroadcast reLogin err:");
      LogUtil.printStackTrace(TAG, th);
    }
  }

  public static void restartByBroadcast() {
    try {
      context.sendBroadcast(new Intent("com.eg.android.AlipayGphone.sesame.restart"));
    } catch (Throwable th) {
      LogUtil.runtime(TAG, "sesame sendBroadcast restart err:");
      LogUtil.printStackTrace(TAG, th);
    }
  }

  @SuppressLint("ObsoleteSdkInt")
  private static int getPendingIntentFlag() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
    } else {
      return PendingIntent.FLAG_UPDATE_CURRENT;
    }
  }

  public static Object getMicroApplicationContext() {
    if (microApplicationContextObject == null) {
      return microApplicationContextObject =
          XposedHelpers.callMethod(
              XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mobile.framework.AlipayApplication", classLoader), "getInstance"), "getMicroApplicationContext");
    }
    return microApplicationContextObject;
  }

  public static Object getServiceObject(String service) {
    try {
      return XposedHelpers.callMethod(getMicroApplicationContext(), "findServiceByInterface", service);
    } catch (Throwable th) {
      LogUtil.runtime(TAG, "getUserObject err");
      LogUtil.printStackTrace(TAG, th);
    }
    return null;
  }

  public static Object getUserObject() {
    try {
      return XposedHelpers.callMethod(
          getServiceObject(XposedHelpers.findClass("com.alipay.mobile.personalbase.service.SocialSdkContactService", classLoader).getName()), "getMyAccountInfoModelByLocal");
    } catch (Throwable th) {
      LogUtil.runtime(TAG, "getUserObject err");
      LogUtil.printStackTrace(TAG, th);
    }
    return null;
  }

  public static String getUserId() {
    try {
      Object userObject = getUserObject();
      if (userObject != null) {
        return (String) XposedHelpers.getObjectField(userObject, "userId");
      }
    } catch (Throwable th) {
      LogUtil.runtime(TAG, "getUserId err");
      LogUtil.printStackTrace(TAG, th);
    }
    return null;
  }

  public static void reLogin() {
    mainHandler.post(
        () -> {
          if (reLoginCount.get() < 5) {
            execDelayedHandler(reLoginCount.getAndIncrement() * 5000L);
          } else {
            execDelayedHandler(Math.max(BaseModel.getCheckInterval().getValue(), 180_000));
          }
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setClassName(ClassUtil.PACKAGE_NAME, ClassUtil.CURRENT_USING_ACTIVITY);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          offline = true;
          context.startActivity(intent);
        });
  }

  class AlipayBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      LogUtil.runtime("sesame 查看广播:" + action + " intent:" + intent);
      if (action != null) {
        switch (action) {
          case "com.eg.android.AlipayGphone.sesame.restart":
            String userId = intent.getStringExtra("userId");
            if (StringUtil.isEmpty(userId) || Objects.equals(UserIdMapUtil.getCurrentUid(), userId)) {
              initHandler(true);
            }
            break;
          case "com.eg.android.AlipayGphone.sesame.execute":
            initHandler(false);
            break;
          case "com.eg.android.AlipayGphone.sesame.reLogin":
            reLogin();
            break;
          case "com.eg.android.AlipayGphone.sesame.status":
            try {
              context.sendBroadcast(new Intent("fansirsqi.xposed.sesame.status"));
            } catch (Throwable th) {
              LogUtil.runtime(TAG, "sesame sendBroadcast status err:");
              LogUtil.printStackTrace(TAG, th);
            }
            break;
          case "com.eg.android.AlipayGphone.sesame.rpctest":
            try {
              String method = intent.getStringExtra("method");
              String data = intent.getStringExtra("data");
              String type = intent.getStringExtra("type");
              DebugRpc rpcInstance = new DebugRpc(); // 创建实例
              rpcInstance.start(method, data, type); // 通过实例调用非静态方法
            } catch (Throwable th) {
              LogUtil.runtime(TAG, "sesame 测试RPC请求失败:");
              LogUtil.printStackTrace(TAG, th);
            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + action);
        }
      }
    }
  }

  /**
   * 注册广播接收器以监听支付宝相关动作。
   *
   * @param context 应用程序上下文
   */
  @SuppressLint("UnspecifiedRegisterReceiverFlag") // 忽略Lint关于注册广播接收器时未指定导出属性的警告
  void registerBroadcastReceiver(Context context) {
    //       创建一个IntentFilter实例，用于过滤出我们需要捕获的广播
    try {
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction("com.eg.android.AlipayGphone.sesame.restart"); // 重启支付宝服务的动作
      intentFilter.addAction("com.eg.android.AlipayGphone.sesame.execute"); // 执行特定命令的动作
      intentFilter.addAction("com.eg.android.AlipayGphone.sesame.reLogin"); // 重新登录支付宝的动作
      intentFilter.addAction("com.eg.android.AlipayGphone.sesame.status"); // 查询支付宝状态的动作
      intentFilter.addAction("com.eg.android.AlipayGphone.sesame.rpctest"); // 调试RPC的动作
      // 根据Android SDK版本注册广播接收器
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // 在Android 13及以上版本，注册广播接收器并指定其可以被其他应用发送的广播触发
        context.registerReceiver(new AlipayBroadcastReceiver(), intentFilter, Context.RECEIVER_EXPORTED);
      } else {
        // 在Android 13以下版本，注册广播接收器
        context.registerReceiver(new AlipayBroadcastReceiver(), intentFilter);
      }
      // 记录成功注册广播接收器的日志
      LogUtil.runtime(TAG, "hook registerBroadcastReceiver successfully");
    } catch (Throwable th) {
      // 记录注册广播接收器失败的日志
      LogUtil.runtime(TAG, "hook registerBroadcastReceiver err:");
      // 打印异常堆栈信息
      LogUtil.printStackTrace(TAG, th);
    }
  }

}
