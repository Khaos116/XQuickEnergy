package fansirsqi.xposed.sesame.entity;

import org.json.JSONException;
import org.json.JSONObject;
import fansirsqi.xposed.sesame.util.*;
import fansirsqi.xposed.sesame.util.Maps.UserMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * 表示好友能量监视器的实体类，提供能量收集的统计和管理功能。
 * 该类是线程安全的，适用于多线程环境。
 *
 * @author Constanline
 * @since 2023/08/08
 */
public class FriendWatch extends IdAndName {

    // 日志标签
    private static final String TAG = FriendWatch.class.getSimpleName();

    // 用于存储好友能量数据的 JSON 对象
    private static JSONObject joFriendWatch;

    private String startTime; // 开始统计时间
    private int allGet; // 总收集能量
    private int weekGet; // 本周收集能量

    /**
     * 构造方法，初始化好友监视器对象。
     *
     * @param id 好友 ID
     * @param name 好友名称
     */
    public FriendWatch(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 比较两个好友的周收能量，用于排序。
     *
     * @param o 要比较的另一个对象
     * @return 比较结果
     */
    @Override
    public int compareTo(IdAndName o) {
        FriendWatch another = (FriendWatch) o;
        if (this.weekGet > another.weekGet) {
            return -1;
        } else if (this.weekGet < another.weekGet) {
            return 1;
        }
        return super.compareTo(o);
    }

    /**
     * 更新好友的能量收集数据。
     *
     * @param id 好友 ID
     * @param collectedEnergy 本次收集的能量
     */
    public static void friendWatch(String id, int collectedEnergy) {
        try {
            JSONObject joSingle = joFriendWatch.optJSONObject(id);
            if (joSingle == null) {
                joSingle = new JSONObject();
                joSingle.put("name", UserMap.getMaskName(id));
                joSingle.put("allGet", 0);
                joSingle.put("startTime", TimeUtil.getFormatDate());
                joFriendWatch.put(id, joSingle);
            }
            joSingle.put("weekGet", joSingle.optInt("weekGet", 0) + collectedEnergy);
        } catch (Throwable th) {
            Log.runtime(TAG, "friendWatch err:");
            Log.printStackTrace(TAG, th);
        }
    }

    /**
     * 保存好友能量数据到文件。
     */
    public static synchronized void save() {
        try {
            Files.write2File(joFriendWatch.toString(), Files.getFriendWatchFile());
        } catch (Exception e) {
            Log.runtime(TAG, "friendWatch save err:");
            Log.printStackTrace(TAG, e);
        }
    }

    /**
     * 更新每日统计数据，如果需要更新周数据则进行重置。
     */
    public static void updateDay() {
        if (!needUpdateAll(Files.getFriendWatchFile().lastModified())) {
            return;
        }
        try {
            String dateStr = TimeUtil.getFormatDate();
            Iterator<String> ids = joFriendWatch.keys();
            while (ids.hasNext()) {
                String id = ids.next();
                JSONObject joSingle = joFriendWatch.getJSONObject(id);
                joSingle.put("allGet", joSingle.optInt("allGet", 0) + joSingle.optInt("weekGet", 0));
                joSingle.put("weekGet", 0);
                if (!joSingle.has("startTime")) {
                    joSingle.put("startTime", dateStr);
                }
            }
            save();
        } catch (Throwable th) {
            Log.runtime(TAG, "friendWatch updateDay err:");
            Log.printStackTrace(TAG, th);
        }
    }

    /**
     * 加载好友能量数据。
     *
     * @return 加载是否成功
     */
    public static synchronized Boolean load() {
        try {
            String strFriendWatch = Files.readFromFile(Files.getFriendWatchFile());
            joFriendWatch = strFriendWatch.isEmpty() ? new JSONObject() : new JSONObject(strFriendWatch);
            return true;
        } catch (JSONException e) {
            Log.printStackTrace(e);
            joFriendWatch = new JSONObject();
        }
        return false;
    }

    /**
     * 卸载好友能量数据，清空内存中的缓存。
     */
    public static synchronized void unload() {
        joFriendWatch = new JSONObject();
    }

    /**
     * 判断是否需要更新所有数据（周一重置）。
     *
     * @param last 上次更新时间的时间戳
     * @return 是否需要更新
     */
    public static boolean needUpdateAll(long last) {
        if (last == 0L) {
            return true;
        }
        Calendar cLast = Calendar.getInstance();
        cLast.setTimeInMillis(last);
        Calendar cNow = Calendar.getInstance();
        return cLast.get(Calendar.DAY_OF_YEAR) != cNow.get(Calendar.DAY_OF_YEAR) &&
                cNow.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    /**
     * 获取好友能量列表，用于展示。
     *
     * @return 包含所有好友能量数据的列表
     */
    public static List<FriendWatch> getList() {
        ArrayList<FriendWatch> list = new ArrayList<>();
        try {
            String strFriendWatch = Files.readFromFile(Files.getFriendWatchFile());
            JSONObject joFriendWatch = strFriendWatch.isEmpty() ? new JSONObject() : new JSONObject(strFriendWatch);

            Iterator<String> ids = joFriendWatch.keys();
            while (ids.hasNext()) {
                String id = ids.next();
                JSONObject friend = joFriendWatch.optJSONObject(id);
                if (friend == null) {
                    continue;
                }
                FriendWatch friendWatch = new FriendWatch(id, friend.optString("name"));
                friendWatch.startTime = friend.optString("startTime", "无");
                friendWatch.weekGet = friend.optInt("weekGet", 0);
                friendWatch.allGet = friend.optInt("allGet", 0) + friendWatch.weekGet;

                friendWatch.name = friendWatch.name + "(开始统计时间:" + friendWatch.startTime + ")\n周收:" +
                        friendWatch.weekGet + " 总收:" + friendWatch.allGet;
                list.add(friendWatch);
            }
        } catch (Throwable t) {
            Log.runtime(TAG, "FriendWatch getList err:");
            Log.printStackTrace(TAG, t);
        }
        return list;
    }
}
