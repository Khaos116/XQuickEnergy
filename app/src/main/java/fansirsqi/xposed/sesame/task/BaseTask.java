package fansirsqi.xposed.sesame.task;

import android.os.Build;

import fansirsqi.xposed.sesame.util.Log;
import lombok.Getter;
import fansirsqi.xposed.sesame.util.ThreadUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * BaseTask 类用于表示任务的基础类，支持管理子任务、启动和停止任务等操作。
 * 子类可以通过继承此类来实现自定义任务逻辑。
 */
public abstract class BaseTask {

    @Getter
    private volatile Thread thread;

    // 存储子任务的映射表，使用线程安全的 ConcurrentHashMap
    private final Map<String, BaseTask> childTaskMap = new ConcurrentHashMap<>();

    // 构造方法，初始化线程为 null
    public BaseTask() {
        this.thread = null;
    }

    /**
     * 获取任务的唯一标识符，默认为对象的字符串表示
     *
     * @return 任务的唯一标识符
     */
    public String getId() {
        return toString();
    }

    /**
     * 检查任务是否可以执行
     *
     * @return 如果任务可以执行返回 true，否则返回 false
     */
    public abstract Boolean check();

    /**
     * 执行任务的具体操作
     */
    public abstract void run();

    /**
     * 判断是否存在指定 ID 的子任务
     *
     * @param childId 子任务的 ID
     * @return 如果存在该子任务则返回 true，否则返回 false
     */
    public synchronized Boolean hasChildTask(String childId) {
        return childTaskMap.containsKey(childId);
    }

    /**
     * 获取指定 ID 的子任务
     *
     * @param childId 子任务的 ID
     * @return 子任务对象，如果不存在返回 null
     */
    public synchronized BaseTask getChildTask(String childId) {
        return childTaskMap.get(childId);
    }

    /**
     * 向当前任务添加一个子任务
     *
     * @param childTask 子任务对象
     */
    public synchronized void addChildTask(BaseTask childTask) {
        String childId = childTask.getId();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 使用 compute 方法保证线程安全，处理子任务的添加和替换
            childTaskMap.compute(childId, (key, value) -> {
                if (value != null) {
                    value.stopTask();  // 停止已有子任务
                }
                childTask.startTask();  // 启动新子任务
                return childTask;
            });
        } else {
            // 在低版本中直接操作，避免使用 compute
            BaseTask oldTask = childTaskMap.get(childId);
            if (oldTask != null) {
                oldTask.stopTask();
            }
            childTask.startTask();
            childTaskMap.put(childId, childTask);
        }
    }

    /**
     * 移除指定 ID 的子任务
     *
     * @param childId 子任务的 ID
     */
    public synchronized void removeChildTask(String childId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 使用 compute 方法保证线程安全，停止并移除子任务
            childTaskMap.compute(childId, (key, value) -> {
                if (value != null) {
                    ThreadUtil.shutdownAndWait(value.getThread(), -1, TimeUnit.SECONDS);
                }
                return null;
            });
        } else {
            BaseTask oldTask = childTaskMap.get(childId);
            if (oldTask != null) {
                ThreadUtil.shutdownAndWait(oldTask.getThread(), -1, TimeUnit.SECONDS);
            }
            childTaskMap.remove(childId);
        }
    }

    /**
     * 获取当前子任务的数量
     *
     * @return 子任务的数量
     */
    public synchronized Integer countChildTask() {
        return childTaskMap.size();
    }

    /**
     * 启动当前任务
     */
    public void startTask() {
        startTask(false);
    }

    /**
     * 启动当前任务，并支持强制启动
     *
     * @param force 是否强制启动，如果线程已存在且未停止，则强制停止后重新启动
     */
    public synchronized void startTask(Boolean force) {
        // 检查线程状态，如果线程存在且处于活动状态，则不再启动
        if (thread != null && thread.isAlive()) {
            // 如果不强制启动，则返回
            if (!force) {
                return;
            }
            // 停止当前任务
            stopTask();
        }
        // 创建并启动新线程
        thread = new Thread(this::run);
        try {
            // 检查任务是否可以执行
            if (check()) {
                thread.start();  // 启动当前任务线程
                // 启动所有子任务
                for (BaseTask childTask : childTaskMap.values()) {
                    if (childTask != null) {
                        childTask.startTask();  // 启动每个子任务
                    }
                }
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    /**
     * 停止当前任务及其所有子任务
     */
    public synchronized void stopTask() {
        if (thread != null && thread.isAlive()) {
            ThreadUtil.shutdownAndWait(thread, 5, TimeUnit.SECONDS);  // 停止当前任务线程
        }
        // 停止所有子任务
        for (BaseTask childTask : childTaskMap.values()) {
            if (childTask != null) {
                ThreadUtil.shutdownAndWait(childTask.getThread(), -1, TimeUnit.SECONDS);
            }
        }
        // 清理线程和子任务映射
        thread = null;
        childTaskMap.clear();
    }

    /**
     * 创建一个新的 BaseTask 实例
     *
     * @return 一个新的 BaseTask 实例
     */
    public static BaseTask newInstance() {
        return new BaseTask() {
            @Override
            public void run() {
                // 默认空实现
            }

            @Override
            public Boolean check() {
                return true;  // 默认返回 true，表示任务可执行
            }
        };
    }

    /**
     * 创建一个新的 BaseTask 实例，并指定任务 ID
     *
     * @param id 任务 ID
     * @return 一个新的 BaseTask 实例
     */
    public static BaseTask newInstance(String id) {
        return new BaseTask() {
            @Override
            public String getId() {
                return id;  // 返回指定的任务 ID
            }

            @Override
            public void run() {
                // 默认空实现
            }

            @Override
            public Boolean check() {
                return true;  // 默认返回 true，表示任务可执行
            }
        };
    }

    /**
     * 创建一个新的 BaseTask 实例，并指定任务 ID 和执行的 Runnable
     *
     * @param id 任务 ID
     * @param runnable 任务执行逻辑
     * @return 一个新的 BaseTask 实例
     */
    public static BaseTask newInstance(String id, Runnable runnable) {
        return new BaseTask() {
            @Override
            public String getId() {
                return id;  // 返回指定的任务 ID
            }

            @Override
            public void run() {
                runnable.run();  // 执行传入的 Runnable
            }

            @Override
            public Boolean check() {
                return true;  // 默认返回 true，表示任务可执行
            }
        };
    }
}
