package fansirsqi.xposed.sesame.task.reserve;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import fansirsqi.xposed.sesame.entity.ReserveEntity;
import fansirsqi.xposed.sesame.model.BaseModel;
import fansirsqi.xposed.sesame.model.ModelFields;
import fansirsqi.xposed.sesame.model.ModelGroup;
import fansirsqi.xposed.sesame.model.modelFieldExt.SelectAndCountModelField;
import fansirsqi.xposed.sesame.task.ModelTask;
import fansirsqi.xposed.sesame.task.TaskCommon;
import fansirsqi.xposed.sesame.util.*;
import fansirsqi.xposed.sesame.util.maps.IdMapManager;
import fansirsqi.xposed.sesame.util.maps.ReserveaMap;
import fansirsqi.xposed.sesame.data.Status;

public class Reserve extends ModelTask {
    private static final String TAG = Reserve.class.getSimpleName();

    @Override
    public String getName() {
        return "保护地";
    }

    @Override
    public ModelGroup getGroup() {
        return ModelGroup.FOREST;
    }

    @Override
    public String getIcon() {
        return "Reserve.png";
    }

    private SelectAndCountModelField reserveList;

    @Override
    public ModelFields getFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(reserveList = new SelectAndCountModelField("reserveList", "保护地列表", new LinkedHashMap<>(), ReserveEntity::getList, "顾名思义"));
        return modelFields;
    }

    protected void runJava() {
        try {
            Log.record(TAG, "开始保护地任务");
            initReserve();
            animalReserve();
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "start.run err:",t);
        } finally {
            Log.record(TAG, "保护地任务");
        }
    }

    /**
     * 初始化保护地任务。通过 ReserveRpc 接口查询可兑换的树项目，将符合条件的保护地任务存入 ReserveIdMapUtil。 条件：项目类型为 "RESERVE" 且状态为 "AVAILABLE"。若调用失败则加载备份的 ReserveIdMapUtil。
     */
    public static void initReserve() {
        try {
            String response = ReserveRpcCall.queryTreeItemsForExchange();
            JSONObject jsonResponse = MyUtils.myJSONObject(response);
            if (ResChecker.checkRes(TAG, jsonResponse)) {
                JSONArray treeItems = jsonResponse.optJSONArray("treeItems");
                if (treeItems != null) {
                    for (int i = 0; i < treeItems.length(); i++) {
                        JSONObject item = treeItems.getJSONObject(i);
                        // 跳过未定义 projectType 字段的项目
                        if (!item.has("projectType")) {
                            continue;
                        }
                        // 过滤出 projectType 为 "RESERVE" 且 applyAction 为 "AVAILABLE" 的项目
                        if ("RESERVE".equals(item.optString("projectType")) && "AVAILABLE".equals(item.optString("applyAction"))) {
                            // 将符合条件的项目添加到 ReserveIdMapUtil
                            String itemId = item.optString("itemId");
                            String itemName = item.optString("itemName");
                            int energy = item.optInt("energy");
                            IdMapManager.getInstance(ReserveaMap.class).add(itemId, itemName + "(" + energy + "g)");
                        }
                    }
                    Log.record(TAG, "初始化保护地任务成功。");
                }
                // 将筛选结果保存到 ReserveIdMapUtil
                IdMapManager.getInstance(ReserveaMap.class).save();
            } else {
                // 若 resultCode 不为 SUCCESS，记录错误描述
                Log.error(jsonResponse.optString("resultDesc", "未知错误"));
            }
        } catch (JSONException e) {
            // 捕获 JSON 解析错误并记录日志
            Log.printStackTrace(TAG, "JSON 解析错误：" + e.getMessage(),e);
            IdMapManager.getInstance(ReserveaMap.class).load(); // 若出现异常则加载保存的 ReserveIdMapUtil 备份
        } catch (Exception e) {
            // 捕获所有其他异常并记录
            Log.printStackTrace(TAG, "初始化保护地任务时出错：" + e.getMessage(),e);
            IdMapManager.getInstance(ReserveaMap.class).load(); // 加载备份的 ReserveIdMapUtil
        }
    }

    private void animalReserve() {
        try {
            Log.record(TAG, "开始执行-" + getName());
            String s = ReserveRpcCall.queryTreeItemsForExchange();
            if (s == null) {
                GlobalThreadPools.sleepCompat(RandomUtil.delay());
                s = ReserveRpcCall.queryTreeItemsForExchange();
            }
            JSONObject jo = MyUtils.myJSONObject(s);
            if (ResChecker.checkRes(TAG, jo)) {
                JSONArray ja = jo.getJSONArray("treeItems");
                for (int i = 0; i < ja.length(); i++) {
                    jo = ja.getJSONObject(i);
                    if (!jo.has("projectType")) {
                        continue;
                    }
                    if (!"RESERVE".equals(jo.optString("projectType"))) {
                        continue;
                    }
                    if (!"AVAILABLE".equals(jo.optString("applyAction"))) {
                        continue;
                    }
                    String projectId = jo.optString("itemId");
                    String itemName = jo.optString("itemName");
                    Map<String, Integer> map = reserveList.getValue();
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        if (Objects.equals(entry.getKey(), projectId)) {
                            Integer count = entry.getValue();
                            if (count != null && count > 0 && Status.canReserveToday(projectId, count)) {
                                exchangeTree(projectId, itemName, count);
                            }
                            break;
                        }
                    }
                }
            } else {
                Log.record(TAG, jo.optString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "animalReserve err:",t);
        } finally {
            Log.record(TAG, "结束执行-" + getName());
        }
    }

    private boolean queryTreeForExchange(String projectId) {
        try {
            String s = ReserveRpcCall.queryTreeForExchange(projectId);
            JSONObject jo = MyUtils.myJSONObject(s);
            if (ResChecker.checkRes(TAG, jo)) {
                String applyAction = jo.optString("applyAction");
                int currentEnergy = jo.optInt("currentEnergy");
                jo = jo.getJSONObject("exchangeableTree");
                if ("AVAILABLE".equals(applyAction)) {
                    if (currentEnergy >= jo.optInt("energy")) {
                        return true;
                    } else {
                        Log.forest("领保护地🏕️[" + jo.optString("projectName") + "]#能量不足停止申请");
                        return false;
                    }
                } else {
                    Log.forest("领保护地🏕️[" + jo.optString("projectName") + "]#似乎没有了");
                    return false;
                }
            } else {
                Log.record(jo.optString("resultDesc"));
                Log.record(s);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "queryTreeForExchange err:",t);
        }
        return false;
    }

    private void exchangeTree(String projectId, String itemName, int count) {
        int appliedTimes = 0;
        try {
            String s;
            JSONObject jo;
            boolean canApply = queryTreeForExchange(projectId);
            if (!canApply)
                return;
            for (int applyCount = 1; applyCount <= count; applyCount++) {
                s = ReserveRpcCall.exchangeTree(projectId);
                jo = MyUtils.myJSONObject(s);
                if (ResChecker.checkRes(TAG, jo)) {
                    int vitalityAmount = jo.optInt("vitalityAmount", 0);
                    appliedTimes = Status.getReserveTimes(projectId) + 1;
                    String str = "领保护地🏕️[" + itemName + "]#第" + appliedTimes + "次"
                            + (vitalityAmount > 0 ? "-活力值+" + vitalityAmount : "");
                    Log.forest(str);
                    Status.reserveToday(projectId, 1);
                } else {
                    //Log.record(jo.optString("resultDesc"));
                    //Log.runtime(jo.toString());
                    Log.error("领保护地🏕️[" + itemName + "]#发生未知错误，停止申请");
                    // Statistics.reserveToday(projectId, count);
                    break;
                }
                GlobalThreadPools.sleepCompat(300);
                canApply = queryTreeForExchange(projectId);
                if (!canApply) {
                    // Statistics.reserveToday(projectId, count);
                    break;
                } else {
                    GlobalThreadPools.sleepCompat(300);
                }
                if (!Status.canReserveToday(projectId, count))
                    break;
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, "exchangeTree err:",t);
        }
    }
}