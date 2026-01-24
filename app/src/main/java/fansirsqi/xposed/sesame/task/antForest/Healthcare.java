package fansirsqi.xposed.sesame.task.antForest;

import org.json.JSONArray;
import org.json.JSONObject;

import fansirsqi.xposed.sesame.util.*;

/**
 * @author Byseven
 * @date 2025/3/7
 * @apiNote
 */
public class Healthcare {

    public static final String TAG = Healthcare.class.getSimpleName();

    public static void queryForestEnergy(String scene) {
        try {
            JSONObject jo = MyUtils.myJSONObject(AntForestRpcCall.queryForestEnergy(scene));
            if (!ResChecker.checkRes(TAG, jo)) {
                return;
            }
            jo = jo.getJSONObject("data").getJSONObject("response");
            JSONArray ja = jo.getJSONArray("energyGeneratedList");
            if (ja.length() > 0) {
                harvestForestEnergy(scene, ja);
            }
            int remainBubble = jo.optInt("remainBubble");
            for (int i = 0; i < remainBubble; i++) {
                ja = produceForestEnergy(scene);
                if (ja.length() == 0 || !harvestForestEnergy(scene, ja)) {
                    return;
                }
                TimeUtil.sleepCompat(1000);
            }
        } catch (Throwable th) {
            Log.record(TAG, "queryForestEnergy err:");
            Log.printStackTrace(TAG, th);
        }
    }

    private static JSONArray produceForestEnergy(String scene) {
        JSONArray energyGeneratedList = new JSONArray();
        try {
            JSONObject jo = MyUtils.myJSONObject(AntForestRpcCall.produceForestEnergy(scene));
            if (ResChecker.checkRes(TAG, jo)) {
                jo = jo.getJSONObject("data").getJSONObject("response");
                energyGeneratedList = jo.getJSONArray("energyGeneratedList");
                if (energyGeneratedList.length() > 0) {
                    String title = scene.equals("FEEDS") ? "绿色医疗" : "电子小票";
                    int cumulativeEnergy = jo.optInt("cumulativeEnergy");
                    Log.forest("医疗健康🚑完成[" + title + "]#产生[" + cumulativeEnergy + "g能量]");
                }
            }
        } catch (Throwable th) {
            Log.record(TAG, "produceForestEnergy err:");
            Log.printStackTrace(TAG, th);
        }
        return energyGeneratedList;
    }

    private static Boolean harvestForestEnergy(String scene, JSONArray bubbles) {
        try {
            JSONObject jo = MyUtils.myJSONObject(AntForestRpcCall.harvestForestEnergy(scene, bubbles));
            if (!ResChecker.checkRes(TAG, jo)) {
                return false;
            }
            jo = jo.getJSONObject("data").getJSONObject("response");
            int collectedEnergy = jo.optInt("collectedEnergy");
            if (collectedEnergy > 0) {
                String title = scene.equals("FEEDS") ? "绿色医疗" : "电子小票";
                Log.forest("医疗健康🚑收取[" + title + "]#获得[" + collectedEnergy + "g能量]");
                return true;
            }
        } catch (Throwable th) {
            Log.record(TAG, "harvestForestEnergy err:");
            Log.printStackTrace(TAG, th);
        }
        return false;
    }
}