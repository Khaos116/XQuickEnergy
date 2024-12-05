package fansirsqi.xposed.sesame.rpc.intervallimit;

import android.os.Build;

import fansirsqi.xposed.sesame.util.Log;
import fansirsqi.xposed.sesame.util.ThreadUtil;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RpcIntervalLimit类用于管理不同方法的调用间隔限制，确保调用间隔不小于设定值。
 * 提供添加、更新、进入间隔限制以及清除限制的功能。
 */
public class RpcIntervalLimit {

    // 默认的间隔限制设置为50毫秒
    private static final IntervalLimit DEFAULT_INTERVAL_LIMIT = new DefaultIntervalLimit(50);

    // 存储各方法的间隔限制，使用线程安全的ConcurrentHashMap
    private static final Map<String, IntervalLimit> intervalLimitMap = new ConcurrentHashMap<>();

    /**
     * 为指定方法添加间隔限制。
     *
     * @param method  方法名称
     * @param interval 间隔时间（毫秒）
     */
    public static void addIntervalLimit(String method, Integer interval) {
        addIntervalLimit(method, new DefaultIntervalLimit(interval));
    }

    /**
     * 为指定方法添加自定义间隔限制对象。
     *
     * @param method 方法名称
     * @param intervalLimit 自定义的间隔限制对象
     */
    public static void addIntervalLimit(String method, IntervalLimit intervalLimit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android N及以上版本可以使用compute方法
            intervalLimitMap.compute(method, (key, existingLimit) -> {
                if (existingLimit != null) {
                    Log.runtime("方法：" + method + " 间隔限制已存在");
                    throw new IllegalArgumentException("方法：" + method + " 间隔限制已存在");
                }
                return intervalLimit;
            });
        } else {
            // 低于Android N的版本手动实现compute的功能
            synchronized (intervalLimitMap) {
                if (intervalLimitMap.containsKey(method)) {
                    Log.runtime("方法：" + method + " 间隔限制已存在");
                    throw new IllegalArgumentException("方法：" + method + " 间隔限制已存在");
                }
                intervalLimitMap.put(method, intervalLimit);
            }
        }
    }

    /**
     * 更新指定方法的间隔限制。
     *
     * @param method 方法名称
     * @param interval 新的间隔时间（毫秒）
     */
    public static void updateIntervalLimit(String method, Integer interval) {
        updateIntervalLimit(method, new DefaultIntervalLimit(interval));
    }

    /**
     * 更新指定方法的间隔限制对象。
     *
     * @param method 方法名称
     * @param intervalLimit 新的自定义间隔限制对象
     */
    public static void updateIntervalLimit(String method, IntervalLimit intervalLimit) {
        intervalLimitMap.put(method, intervalLimit);
    }

    /**
     * 进入指定方法的间隔限制，确保调用间隔时间不小于设定值。
     *
     * @param method 方法名称
     */
    public static void enterIntervalLimit(String method) {
        IntervalLimit intervalLimit;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android N及以上使用getOrDefault方法
            intervalLimit = intervalLimitMap.getOrDefault(method, DEFAULT_INTERVAL_LIMIT);
        } else {
            // 低于Android N手动实现getOrDefault的功能
            intervalLimit = intervalLimitMap.containsKey(method) ? intervalLimitMap.get(method) : DEFAULT_INTERVAL_LIMIT;
        }

        // 确保intervalLimit非空
        synchronized (Objects.requireNonNull(intervalLimit, "间隔限制对象不能为空")) {
            long sleep = intervalLimit.getInterval() - (System.currentTimeMillis() - intervalLimit.getTime());
            if (sleep > 0) {
                ThreadUtil.sleep(sleep);
            }
            intervalLimit.setTime(System.currentTimeMillis());
        }
    }

    /**
     * 清除所有方法的间隔限制。
     */
    public static void clearIntervalLimit() {
        intervalLimitMap.clear();
    }
}
