package fansirsqi.xposed.sesame.task.antForest;

import fansirsqi.xposed.sesame.util.*;

import org.json.JSONObject;

public class GreenLife {
    public static final String TAG = GreenLife.class.getSimpleName();
    /** 森林集市 */
    public static void ForestMarket(String sourceType) {
        try {
            JSONObject jo = MyUtils.myJSONObject(AntForestRpcCall.consultForSendEnergyByAction(sourceType));
            if (ResChecker.checkRes(TAG,jo)) {
                JSONObject data = jo.getJSONObject("data");
                if (data.optBoolean("canSendEnergy", false)) {
                    CoroutineUtils.sleepCompat(1000);
                    jo = MyUtils.myJSONObject(AntForestRpcCall.sendEnergyByAction(sourceType));
                    if (ResChecker.checkRes(TAG,jo)) {
                        data = jo.getJSONObject("data");
                        if (data.optBoolean("canSendEnergy", false)) {
                            int receivedEnergyAmount = data.optInt("receivedEnergyAmount");
                            Log.forest("集市逛街🛍[获得:能量" + receivedEnergyAmount + "g]");
                        }
                    }
                }
            } else {
                Log.record(TAG, jo.getJSONObject("data").optString("resultCode"));
                CoroutineUtils.sleepCompat(300);
            }
        } catch (Throwable t) {
            Log.record(TAG, "sendEnergyByAction err:");
            Log.printStackTrace(TAG, t);
        }
    }
}