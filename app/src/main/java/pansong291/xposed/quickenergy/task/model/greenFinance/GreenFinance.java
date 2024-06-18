package pansong291.xposed.quickenergy.task.model.greenFinance;

import org.json.JSONArray;
import org.json.JSONObject;

import pansong291.xposed.quickenergy.data.ModelFields;
import pansong291.xposed.quickenergy.task.common.ModelTask;
import pansong291.xposed.quickenergy.task.common.TaskCommon;
import pansong291.xposed.quickenergy.util.Config;
import pansong291.xposed.quickenergy.util.Log;

/**
 * @author Constanline
 * @since 2023/09/08
 */
public class GreenFinance extends ModelTask {
    private static final String TAG = GreenFinance.class.getSimpleName();

    @Override
    public String setName() {
        return "绿色经营";
    }

    @Override
    public ModelFields setFields() {
        return null;
    }

    public Boolean check() {
        return Config.INSTANCE.isGreenFinance() && !TaskCommon.IS_MORNING;
    }

    public Runnable init() {
        return GreenFinance::index;
    }

    private static void batchSelfCollect(JSONArray bsnIds) {
        String s = GreenFinanceRpcCall.batchSelfCollect(bsnIds);
        try {
            JSONObject joSelfCollect = new JSONObject(s);
            if (joSelfCollect.getBoolean("success")) {
                int totalCollectPoint = joSelfCollect.getJSONObject("result").getInt("totalCollectPoint");
                Log.other("绿色经营📊收集获得" + totalCollectPoint);
            } else {
                Log.i(TAG + ".batchSelfCollect", s);
            }
        } catch (Throwable th) {
            Log.i(TAG, "batchSelfCollect err:");
            Log.printStackTrace(TAG, th);
        } finally {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    private static void index() {
        String s = GreenFinanceRpcCall.greenFinanceIndex();
        try {
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject result = jo.getJSONObject("result");
                if (!result.getBoolean("greenFinanceSigned")) {
                    Log.other("绿色经营📊未开通");
                    return;
                }
                JSONObject mcaGreenLeafResult = result.getJSONObject("mcaGreenLeafResult");
                JSONArray greenLeafList = mcaGreenLeafResult.getJSONArray("greenLeafList");
                String currentCode = "";
                JSONArray bsnIds = new JSONArray();
                for (int i = 0; i < greenLeafList.length(); i++) {
                    JSONObject greenLeaf = greenLeafList.getJSONObject(i);
                    String code = greenLeaf.getString("code");
                    if (currentCode.equals(code) || bsnIds.length() == 0) {
                        bsnIds.put(greenLeaf.getString("bsnId"));
                    } else {
                        batchSelfCollect(bsnIds);
                        bsnIds = new JSONArray();
                    }
                }
                if (bsnIds.length() > 0) {
                    batchSelfCollect(bsnIds);
                }
            } else {
                Log.i(TAG, jo.getString("resultDesc"));
            }
        } catch (Throwable th) {
            Log.i(TAG, "index err:");
            Log.printStackTrace(TAG, th);
        }

        signIn("PLAY102632271");
        signIn("PLAY102932217");
        signIn("PLAY102232206");

        String appletId = "AP13159535";
        doTask(appletId);
    }

    private static void signIn(String sceneId) {
        try {
            String s = GreenFinanceRpcCall.signInQuery(sceneId);
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject result = jo.getJSONObject("result");
                if (!result.getBoolean("isTodaySignin")) {
                    s = GreenFinanceRpcCall.signInTrigger(sceneId);
                    jo = new JSONObject(s);
                    if (jo.getBoolean("success")) {
                        Log.other("绿色经营📊签到成功");
                    } else {
                        Log.i(TAG + ".signIn", s);
                    }
                }
            } else {
                Log.i(TAG + ".signIn", s);
            }
        } catch (Throwable th) {
            Log.i(TAG, "signIn err:");
            Log.printStackTrace(TAG, th);
        } finally {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.printStackTrace(e);
            }
        }
    }

    private static void doTask(String appletId) {
        String s = GreenFinanceRpcCall.taskQuery(appletId);
        try {
            JSONObject jo = new JSONObject(s);
            if (jo.getBoolean("success")) {
                JSONObject result = jo.getJSONObject("result");
                JSONArray taskDetailList = result.getJSONArray("taskDetailList");
                for (int i = 0; i < taskDetailList.length(); i++) {
                    JSONObject taskDetail = taskDetailList.getJSONObject(i);
                    if ("USER_TRIGGER".equals(taskDetail.getString("sendCampTriggerType"))) {
                        if ("NONE_SIGNUP".equals(taskDetail.getString("taskProcessStatus"))) {
                            s = GreenFinanceRpcCall.taskTrigger(taskDetail.getString("taskId"), "signup", appletId);
                            jo = new JSONObject(s);
                            if (jo.getBoolean("success")) {
                                s = GreenFinanceRpcCall.taskTrigger(taskDetail.getString("taskId"), "send", appletId);
                                jo = new JSONObject(s);
                                if (jo.getBoolean("success")) {
                                    Log.other("绿色经营📊任务完成");
                                }
                            }
                        }
                    }
                }
            } else {
                Log.i(TAG + ".doTask", s);
            }
        } catch (Throwable th) {
            Log.i(TAG, "signIn err:");
            Log.printStackTrace(TAG, th);
        }
    }
}