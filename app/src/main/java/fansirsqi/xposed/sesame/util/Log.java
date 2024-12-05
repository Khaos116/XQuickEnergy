package fansirsqi.xposed.sesame.util;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.clean.NeverCleanStrategy;
import com.elvishew.xlog.printer.file.naming.FileNameGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import fansirsqi.xposed.sesame.model.BaseModel;

/** 日志工具类，负责初始化和管理各种类型的日志记录器，并提供日志输出方法。 */
public class Log {

  // 日志初始化，设置日志等级
  static {
    XLog.init(LogLevel.ALL);
  }

  /**
   * 创建一个 ThreadLocal<SimpleDateFormat> 实例，用于日期格式化。
   *
   * @param pattern 日期格式化模式
   * @return 一个线程安全的 SimpleDateFormat 实例
   */
  private static ThreadLocal<SimpleDateFormat> createThreadLocal(final String pattern) {
    return ThreadLocal.withInitial(() -> new SimpleDateFormat(pattern, Locale.getDefault()));
  }

  /**
   * 通用的日志记录器创建方法，减少重复代码。
   *
   * @param tag 日志标签
   * @param pattern 日志输出的模式
   * @return Loger 实例
   */
  private static Logger createLogger(String tag, String pattern) {
    return XLog.tag(tag)
            .printers(
                    new FilePrinter.Builder(Files.LOG_DIRECTORY.getPath())
                            .fileNameGenerator(new CustomDateFileNameGenerator(tag))
                            .backupStrategy(new NeverBackupStrategy())
                            .cleanStrategy(new NeverCleanStrategy())
                            .flattener(new PatternFlattener(pattern))
                            .build())
            .build();
  }

  // 使用 ThreadLocal 避免多线程环境下 SimpleDateFormat 的线程安全问题
  public static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_THREAD_LOCAL = createThreadLocal("yyyy-MM-dd");
  public static final ThreadLocal<SimpleDateFormat> DATE_TIME_FORMAT_THREAD_LOCAL = createThreadLocal("yyyy-MM-dd HH:mm:ss");
  public static final ThreadLocal<SimpleDateFormat> OTHER_DATE_TIME_FORMAT_THREAD_LOCAL = createThreadLocal("yyyy.MM.dd HH:mm:ss");

  // 日志记录器
  private static final Logger runtimeLogger = createLogger("runtime", "{d HH:mm:ss.SSS} {t}: {m}");
  private static final Logger recordLogger = createLogger("record", "{d HH:mm:ss.SSS} {m}");
  private static final Logger systemLogger = createLogger("system", "{d HH:mm:ss.SSS} {t}: {m}");
  private static final Logger captureLogger = createLogger("capture", "{d HH:mm:ss.SSS} {t}: {m}");
  private static final Logger forestLogger = createLogger("forest", "{d HH:mm:ss.SSS} {m}");
  private static final Logger farmLogger = createLogger("farm", "{d HH:mm:ss.SSS} {m}");
  private static final Logger otherLogger = createLogger("other", "{d HH:mm:ss.SSS} {m}");
  private static final Logger errorLogger = createLogger("error", "{d HH:mm:ss.SSS} {t}: {m}");
  private static final Logger debugLogger = createLogger("debug", "{d HH:mm:ss.SSS} {m}");


  /**
   * 输出信息级别的日志。
   *
   * @param s 日志内容
   */
  public static void runtime(String s) {
    runtimeLogger.i(s);
  }

  /**
   * 输出带有标签的信息级别的日志。
   *
   * @param tag 标签
   * @param s 日志内容
   */
  public static void runtime(String tag, String s) {
    runtime(tag + ", " + s);
  }

  /**
   * 记录日志并输出到 record 日志文件中。
   *
   * @param str 日志内容
   */
  public static void record(String str) {
    runtimeLogger.i(str);
    if (BaseModel.getRecordLog().getValue()) {
      recordLogger.i(str);
    }
  }

  /**
   * 输出系统级别的日志。
   *
   * @param tag 标签
   * @param s 日志内容
   */
  public static void system(String tag, String s) {
    systemLogger.i(tag + ", " + s);
  }

  /**
   * 输出网络抓包相关的日志。
   *
   * @param s 日志内容
   */
  public static void capture(String s) {
    captureLogger.d(s);
  }

  /**
   * 记录森林相关的日志。
   *
   * @param s 日志内容
   */
  public static void forest(String s) {
    record(s);
    forestLogger.i(s);
  }

  /**
   * 记录农场相关的日志。
   *
   * @param s 日志内容
   */
  public static void farm(String s) {
    record(s);
    farmLogger.i(s);
  }

  /**
   * 记录其他类型的日志。
   *
   * @param s 日志内容
   */
  public static void other(String s) {
    record(s);
    otherLogger.i(s);
  }

  public static void debug(String s) {
    debugLogger.i(s);
  }

  /**
   * 记录错误日志。
   *
   * @param s 日志内容
   */
  public static void error(String s) {
    errorLogger.i(s);
    runtime(s);
  }

  /**
   * 打印异常堆栈跟踪信息。
   *
   * @param t 异常对象
   */
  public static void printStackTrace(Throwable t) {
    String str = android.util.Log.getStackTraceString(t);
    errorLogger.i(str);
    runtime(str);
  }

  /**
   * 打印带有标签的异常堆栈跟踪信息。
   *
   * @param tag 标签
   * @param t 异常对象
   */
  public static void printStackTrace(String tag, Throwable t) {
    String str = tag + ", " + android.util.Log.getStackTraceString(t);
    errorLogger.i(str);
    runtime(str);
  }

  /**
   * 根据日志名称生成带有日期的日志文件名。
   *
   * @param logName 日志名称
   * @return 带日期的日志文件名
   */
  public static String getLogFileName(String logName) {
    // 增加非空检查
    String sdf = TimeUtil.getFormatDate();
    if (sdf != null) {
      return logName + "." + sdf + ".log";
    } else {
      throw new IllegalStateException("Date format not initialized properly");
    }
  }

  /**
   * 获取当前格式化的日期字符串。
   *
   * @return 格式化后的日期字符串
   */
  public static String getFormatDate() {
    return TimeUtil.getFormatDateTime().split(" ")[0];
  }

  /**
   * 获取当前格式化的时间字符串。
   *
   * @return 格式化后的时间字符串
   */
  public static String getFormatTime() {
    return TimeUtil.getFormatDateTime().split(" ")[1];
  }



  /** 自定义日志文件名生成器。 */
  public static class CustomDateFileNameGenerator implements FileNameGenerator {

    private final ThreadLocal<SimpleDateFormat> mLocalDateFormat = createThreadLocal("yyyy-MM-dd");
    private final String name;

    public CustomDateFileNameGenerator(String name) {
      this.name = name;
    }

    @Override
    public boolean isFileNameChangeable() {
      return true;
    }

    /** 生成包含日期的文件名。 */
    @Override
    public String generateFileName(int logLevel, long timestamp) {
      SimpleDateFormat sdf = mLocalDateFormat.get();
      // 增加非空检查
      if (sdf != null) {
        return name + "." + sdf.format(new Date(timestamp)) + ".log";
      } else {
        throw new IllegalStateException("Date format not initialized properly");
      }
    }
  }
}
