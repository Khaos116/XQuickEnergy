package fansirsqi.xposed.sesame.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonMappingException;

import fansirsqi.xposed.sesame.util.Log;
import lombok.Data;
import fansirsqi.xposed.sesame.util.Files;
import fansirsqi.xposed.sesame.util.JsonUtil;

/**
 * UI 配置类，用于管理应用的 UI 配置。
 * 包括加载、保存、卸载等操作。
 */
@Data
public class UIConfig {

    private static final String TAG = UIConfig.class.getSimpleName();

    // 单例模式：UIConfig 的唯一实例
    public static final UIConfig INSTANCE = new UIConfig();

    // 用于标记是否已初始化，防止重复初始化
    @JsonIgnore
    private boolean init;

    // 是否使用新 UI
    private Boolean newUI = true;

    /**
     * 保存当前 UI 配置到文件中
     *
     * @return 保存成功返回 true，失败返回 false
     */
    public static Boolean save() {
        Log.record("保存UI配置");
        return Files.setUIConfigFile(toSaveStr());
    }

    /**
     * 加载 UI 配置文件，如果文件不存在，则初始化为默认配置
     *
     * @return 加载后的 UIConfig 实例
     */
    public static synchronized UIConfig load() {
        java.io.File uiConfigFile = Files.getUIConfigFile();
        try {
            if (uiConfigFile.exists()) {
                Log.runtime("加载UI配置");
                String json = Files.readFromFile(uiConfigFile);
                // 使用 Jackson 将读取的 JSON 数据更新到 INSTANCE 中
                JsonUtil.copyMapper().readerForUpdating(INSTANCE).readValue(json);

                // 如果格式化后的内容与原始内容不同，则进行格式化并保存
                String formatted = toSaveStr();
                if (formatted != null && !formatted.equals(json)) {
                    Log.runtime(TAG, "格式化UI配置");
                    Log.system(TAG, "格式化UI配置");
                    Files.write2File(formatted, uiConfigFile);
                }
            } else {
                unload();  // 如果文件不存在，卸载当前配置
                Log.runtime(TAG, "初始UI配置");
                Log.system(TAG, "初始UI配置");
                // 保存默认配置到文件
                Files.write2File(toSaveStr(), uiConfigFile);
            }
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
            Log.runtime(TAG, "重置UI配置");
            Log.system(TAG, "重置UI配置");
            try {
                unload();  // 出现异常时卸载当前配置
                Files.write2File(toSaveStr(), uiConfigFile);  // 保存默认配置
            } catch (Exception e) {
                Log.printStackTrace(TAG, e);
            }
        }
        INSTANCE.setInit(true);  // 标记初始化完成
        return INSTANCE;
    }

    /**
     * 卸载当前 UI 配置，重置为默认配置
     */
    public static synchronized void unload() {
        try {
            // 使用 Jackson 将 INSTANCE 更新为新的默认 UIConfig 实例
            JsonUtil.copyMapper().updateValue(INSTANCE, new UIConfig());
        } catch (JsonMappingException e) {
            Log.printStackTrace(TAG, e);
        }
    }

    /**
     * 将当前 UI 配置实例转换为 JSON 字符串，供保存使用
     *
     * @return 格式化后的 JSON 字符串
     */
    public static String toSaveStr() {
        return JsonUtil.toFormatJsonString(INSTANCE);
    }

}
