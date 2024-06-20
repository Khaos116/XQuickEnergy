package pansong291.xposed.quickenergy.task.model.antCooperate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;

import pansong291.xposed.quickenergy.data.ModelFields;
import pansong291.xposed.quickenergy.data.modelFieldExt.BooleanModelField;
import pansong291.xposed.quickenergy.data.modelFieldExt.IdAndNameSelectModelField;
import pansong291.xposed.quickenergy.entity.CooperateUser;
import pansong291.xposed.quickenergy.entity.KVNode;
import pansong291.xposed.quickenergy.task.common.ModelTask;
import pansong291.xposed.quickenergy.task.common.TaskCommon;
import pansong291.xposed.quickenergy.util.CooperationIdMap;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.RandomUtil;
import pansong291.xposed.quickenergy.util.Statistics;
import pansong291.xposed.quickenergy.util.UserIdMap;

public class AntCooperate extends ModelTask {
    private static final String TAG = AntCooperate.class.getSimpleName();

    @Override
    public String setName() {
        return "合种";
    }

    public static BooleanModelField cooperateWater;
    public static IdAndNameSelectModelField cooperateWaterList;

    @Override
    public ModelFields setFields() {
        ModelFields modelFields = new ModelFields();
        modelFields.addField(cooperateWater = new BooleanModelField("cooperateWater", "合种浇水", false));
        modelFields.addField(cooperateWaterList = new IdAndNameSelectModelField("cooperateWaterList", "合种浇水列表", new KVNode<>(new LinkedHashMap<>(), true), CooperateUser.getList()));
        return modelFields;
    }

    public Boolean check() {
        return cooperateWater.getValue() && !TaskCommon.IS_ENERGY_TIME;
    }

    public Runnable init() {
        return () -> {
            try {
                String s = AntCooperateRpcCall.queryUserCooperatePlantList();
                if (s == null) {
                    Thread.sleep(RandomUtil.delay());
                    s = AntCooperateRpcCall.queryUserCooperatePlantList();
                }
                JSONObject jo = new JSONObject(s);
                if ("SUCCESS".equals(jo.getString("resultCode"))) {
                    int userCurrentEnergy = jo.getInt("userCurrentEnergy");
                    JSONArray ja = jo.getJSONArray("cooperatePlants");
                    for (int i = 0; i < ja.length(); i++) {
                        jo = ja.getJSONObject(i);
                        String cooperationId = jo.getString("cooperationId");
                        if (!jo.has("name")) {
                            s = AntCooperateRpcCall.queryCooperatePlant(cooperationId);
                            jo = new JSONObject(s).getJSONObject("cooperatePlant");
                        }
                        String name = jo.getString("name");
                        int waterDayLimit = jo.getInt("waterDayLimit");
                        CooperationIdMap.putIdMap(cooperationId, name);
                        if (!Statistics.canCooperateWaterToday(UserIdMap.getCurrentUid(), cooperationId))
                            continue;
                        Integer num = cooperateWaterList.getValue().getKey().get(cooperationId);
                        if (num != null) {
                            if (num > waterDayLimit)
                                num = waterDayLimit;
                            if (num > userCurrentEnergy)
                                num = userCurrentEnergy;
                            if (num > 0)
                                cooperateWater(UserIdMap.getCurrentUid(), cooperationId, num, name);
                        }
                    }
                } else {
                    Log.i(TAG, jo.getString("resultDesc"));
                }
            } catch (Throwable t) {
                Log.i(TAG, "start.run err:");
                Log.printStackTrace(TAG, t);
            }
            CooperationIdMap.saveIdMap();
        };
    }

    private static void cooperateWater(String uid, String coopId, int count, String name) {
        try {
            String s = AntCooperateRpcCall.cooperateWater(uid, coopId, count);
            JSONObject jo = new JSONObject(s);
            if ("SUCCESS".equals(jo.getString("resultCode"))) {
                Log.forest("合种浇水🚿[" + name + "]" + jo.getString("barrageText"));
                Statistics.cooperateWaterToday(UserIdMap.getCurrentUid(), coopId);
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable t) {
            Log.i(TAG, "cooperateWater err:");
            Log.printStackTrace(TAG, t);
        }
    }

}
