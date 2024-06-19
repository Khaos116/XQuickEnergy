package pansong291.xposed.quickenergy.data;

import android.os.Build;

import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;
import lombok.Getter;
import pansong291.xposed.quickenergy.task.common.ModelTask;
import pansong291.xposed.quickenergy.util.FileUtil;
import pansong291.xposed.quickenergy.util.JsonUtil;
import pansong291.xposed.quickenergy.util.ListUtil;
import pansong291.xposed.quickenergy.util.Log;
import pansong291.xposed.quickenergy.util.UserIdMap;

@Data
public class ConfigV2 {

    private static final String TAG = ConfigV2.class.getSimpleName();

    public static final ConfigV2 INSTANCE = new ConfigV2();

    @Getter
    private static volatile boolean init;

    private boolean immediateEffect = true;//立即生效
    private boolean recordLog = true;//记录日志
    private boolean showToast = true;//气泡提示
    private int toastOffsetY = -200;//气泡纵向偏移
    private int checkInterval = 1800_000;
    private boolean stayAwake = true;//保持唤醒
    private List<String> execAtTimeList = ListUtil.newArrayList("00,065555");
    private boolean timeoutRestart = true;//超时重启
    private boolean startAt0 = true;//0点整执行
    private boolean startAt7 = true;//7点前启动
    private boolean enableOnGoing = true;//开启状态栏禁删
    private boolean batteryPerm = true;//为支付宝申请后台运行权限
    private boolean newRpc = true;//使用新接口
    private boolean debugMode = false;//开启抓包
    private boolean languageSimplifiedChinese = true;//界面始终使用中文
    private int waitWhenException = 60 * 60 * 1000;

    private final Map<String, ModelFields> modelFieldsMap = new ConcurrentHashMap<>();

    public void setModelFieldsMap(Map<String, ModelFields> newModels) {
        modelFieldsMap.clear();
        Map<String, ModelConfig> modelConfigMap = ModelTask.getModelConfigMap();
        if (newModels == null) {
            newModels = new HashMap<>();
        }
        for (ModelConfig modelConfig : modelConfigMap.values()) {
            String modelCode = modelConfig.getCode();
            ModelFields newModelFields = new ModelFields();
            ModelFields configModelFields = modelConfig.getFields();
            ModelFields modelFields = newModels.get(modelCode);
            if (modelFields != null) {
                for (ModelField configModelField : configModelFields.values()) {
                    ModelField modelField = modelFields.get(configModelField.getCode());
                    try {
                        if (modelField != null) {
                            Object value = modelField.getValue();
                            if (value != null) {
                                configModelField.setValue(value);
                            }
                        }
                    } catch (Exception e) {
                        Log.printStackTrace(e);
                    }
                    newModelFields.addField(configModelField);
                }
            } else {
                for (ModelField configModelField : configModelFields.values()) {
                    newModelFields.addField(configModelField);
                }
            }
            modelFieldsMap.put(modelCode, newModelFields);
        }
        for (Map.Entry<String, ModelFields> modelFieldsEntry : newModels.entrySet()) {
            ModelFields newModelFields = modelFieldsEntry.getValue();
            if (newModelFields != null) {
                String modelCode = modelFieldsEntry.getKey();
                ModelConfig modelConfig = modelConfigMap.get(modelCode);
                if (modelConfig != null) {
                    ModelFields configModelFields = modelConfig.getFields();
                    for (Map.Entry<String, ModelField> modelFieldEntry : newModelFields.entrySet()) {
                        ModelField modelField = modelFieldEntry.getValue();
                        if (modelField != null) {
                            ModelField configModelField = configModelFields.get(modelFieldEntry.getKey());
                            if (configModelField != null) {
                                try {
                                    configModelField.setValue(modelField.getValue());
                                } catch (Exception e) {
                                    Log.printStackTrace(e);
                                }
                            }
                        }
                    }
                    modelFieldsMap.put(modelCode, configModelFields);
                }
            }
        }
    }

    public Boolean hasModelFields(String modelCode) {
        return modelFieldsMap.containsKey(modelCode);
    }

    public ModelFields getModelFields(String modelCode) {
        return getModelFields(modelCode, false);
    }

    public ModelFields getModelFields(String modelCode, Boolean isCreate) {
        if (isCreate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return modelFieldsMap.compute(modelCode, (key, value) -> {
                    if (value == null) {
                        ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
                        if (modelConfig != null) {
                            value = new ModelFields();
                            value.putAll(modelConfig.getFields());
                        }
                    }
                    return value;
                });
            } else {
                ModelFields modelFields = modelFieldsMap.get(modelCode);
                if (modelFields == null) {
                    ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
                    if (modelConfig != null) {
                        modelFields = new ModelFields();
                        modelFields.putAll(modelConfig.getFields());
                        modelFieldsMap.put(modelCode, modelFields);
                    }
                }
                return modelFields;
            }
        } else {
            return modelFieldsMap.get(modelCode);
        }
    }

    /*public void removeModelFields(String modelCode) {
        modelFieldsMap.remove(modelCode);
    }*/

    /*public void addModelFields(String modelCode, ModelFields modelFields) {
        modelFieldsMap.put(modelCode, modelFields);
    }*/

    public Boolean hasModelField(String modelCode, String fieldCode) {
        ModelFields modelFields = getModelFields(modelCode);
        if (modelFields == null) {
            return false;
        }
        return modelFields.containsKey(fieldCode);
    }

    public ModelField getModelField(String modelCode, String fieldCode) {
        return getModelField(modelCode, fieldCode, false);
    }

    public ModelField getModelField(String modelCode, String fieldCode, Boolean isCreate) {
        ModelFields modelFields = getModelFields(modelCode, isCreate);
        if (modelFields == null) {
            return null;
        }
        if (isCreate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return modelFields.compute(fieldCode, (key, value)-> {
                    if (value == null) {
                        ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
                        if (modelConfig != null) {
                            ModelField modelField = modelConfig.getFields().get(fieldCode);
                            if (modelField != null) {
                                value = modelField.clone();
                            }
                        }
                    }
                    return value;
                });
            } else {
                ModelField modelField = modelFields.get(fieldCode);
                if (modelField == null) {
                    ModelConfig modelConfig = ModelTask.getModelConfigMap().get(modelCode);
                    if (modelConfig != null) {
                        modelField = modelConfig.getFields().get(fieldCode);
                        if (modelField != null) {
                            modelField = modelField.clone();
                            modelFields.put(fieldCode, modelField);
                        }
                    }
                }
                return modelField;
            }
        } else {
            return modelFields.get(fieldCode);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ModelField> T getModelFieldExt(String modelCode, String fieldCode) {
        return getModelFieldExt(modelCode, fieldCode, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends ModelField> T getModelFieldExt(String modelCode, String fieldCode, Boolean isCreate) {
        return (T) getModelField(modelCode, fieldCode, isCreate);
    }

    /*public void removeModelField(String modelCode, String fieldCode) {
        ModelFields modelFields = getModelFields(modelCode);
        if (modelFields == null) {
            return;
        }
        modelFields.remove(fieldCode);
    }*/

    /*public Boolean addModelField(String modelCode, ModelField modelField) {
        ModelFields modelFields = getModelFields(modelCode);
        if (modelFields == null) {
            return false;
        }
        modelFields.put(modelCode, modelField);
        return true;
    }*/

    public static Boolean isModify() {
        String json = null;
        if (FileUtil.getConfigV2File(UserIdMap.getCurrentUid()).exists()) {
            json = FileUtil.readFromFile(FileUtil.getConfigV2File(UserIdMap.getCurrentUid()));
        }
        if (json != null) {
            String formatted = JsonUtil.toJsonString(INSTANCE);
            return formatted == null || !formatted.equals(json);
        }
        return true;
    }

    public static Boolean save(Boolean force) {
        if (!force) {
            if (!isModify()) {
                return true;
            }
        }
        String json = JsonUtil.toJsonString(INSTANCE);
        Log.system(TAG, "保存 config_v2.json: " + json);
        return FileUtil.write2File(json, FileUtil.getConfigV2File());
    }

    public static synchronized ConfigV2 load() {
        Log.i(TAG, "开始加载配置");
        ModelTask.initAllModel();
        String json = null;
        try {
            File configV2File = FileUtil.getConfigV2File(UserIdMap.getCurrentUid());
            if (configV2File.exists()) {
                json = FileUtil.readFromFile(configV2File);
            }
            JsonUtil.MAPPER.readerForUpdating(INSTANCE).readValue(json);
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.i(TAG, "配置文件格式有误，已重置配置文件");
            Log.system(TAG, "配置文件格式有误，已重置配置文件");
            try {
                JsonUtil.MAPPER.updateValue(INSTANCE, new ConfigV2());
            } catch (JsonMappingException e) {
                Log.printStackTrace(TAG, t);
            }
        }
        String formatted = JsonUtil.toJsonString(INSTANCE);
        if (formatted != null && !formatted.equals(json)) {
            Log.i(TAG, "重新格式化 config_v2.json");
            Log.system(TAG, "重新格式化 config_v2.json");
            FileUtil.write2File(formatted, FileUtil.getConfigV2File());
        }
        init = true;
        Log.i(TAG, "加载配置成功");
        return INSTANCE;
    }

    public static boolean isInit() {
        return init;
    }

    public static void setInit(boolean init) {
        ConfigV2.init = init;
    }

    public boolean isImmediateEffect() {
        return immediateEffect;
    }

    public void setImmediateEffect(boolean immediateEffect) {
        this.immediateEffect = immediateEffect;
    }

    public boolean isRecordLog() {
        return recordLog;
    }

    public void setRecordLog(boolean recordLog) {
        this.recordLog = recordLog;
    }

    public boolean isShowToast() {
        return showToast;
    }

    public void setShowToast(boolean showToast) {
        this.showToast = showToast;
    }

    public int getToastOffsetY() {
        return toastOffsetY;
    }

    public void setToastOffsetY(int toastOffsetY) {
        this.toastOffsetY = toastOffsetY;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    public boolean isStayAwake() {
        return stayAwake;
    }

    public void setStayAwake(boolean stayAwake) {
        this.stayAwake = stayAwake;
    }

    public boolean isTimeoutRestart() {
        return timeoutRestart;
    }

    public void setTimeoutRestart(boolean timeoutRestart) {
        this.timeoutRestart = timeoutRestart;
    }

    public boolean isStartAt0() {
        return startAt0;
    }

    public void setStartAt0(boolean startAt0) {
        this.startAt0 = startAt0;
    }

    public boolean isStartAt7() {
        return startAt7;
    }

    public void setStartAt7(boolean startAt7) {
        this.startAt7 = startAt7;
    }

    public boolean isEnableOnGoing() {
        return enableOnGoing;
    }

    public void setEnableOnGoing(boolean enableOnGoing) {
        this.enableOnGoing = enableOnGoing;
    }

    public boolean isBatteryPerm() {
        return batteryPerm;
    }

    public void setBatteryPerm(boolean batteryPerm) {
        this.batteryPerm = batteryPerm;
    }

    public boolean isNewRpc() {
        return newRpc;
    }

    public void setNewRpc(boolean newRpc) {
        this.newRpc = newRpc;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isLanguageSimplifiedChinese() {
        return languageSimplifiedChinese;
    }

    public void setLanguageSimplifiedChinese(boolean languageSimplifiedChinese) {
        this.languageSimplifiedChinese = languageSimplifiedChinese;
    }

    public int getWaitWhenException() {
        return waitWhenException;
    }

    public void setWaitWhenException(int waitWhenException) {
        this.waitWhenException = waitWhenException;
    }

    public Map<String, ModelFields> getModelFieldsMap() {
        return modelFieldsMap;
    }
}
