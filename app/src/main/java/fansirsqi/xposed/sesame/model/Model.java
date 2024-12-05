package fansirsqi.xposed.sesame.model;

import android.os.Build;

import fansirsqi.xposed.sesame.util.Log;
import lombok.Getter;
import fansirsqi.xposed.sesame.model.modelFieldExt.BooleanModelField;
import fansirsqi.xposed.sesame.task.ModelTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Model {

    // 存储所有模型配置的Map
    private static final Map<String, ModelConfig> modelConfigMap = new LinkedHashMap<>();
    private static final Map<String, ModelConfig> readOnlyModelConfigMap = Collections.unmodifiableMap(modelConfigMap);

    // 按ModelGroup分组的模型配置Map
    private static final Map<ModelGroup, Map<String, ModelConfig>> groupModelConfigMap = new LinkedHashMap<>();

    // 存储所有Model类及其实例的Map
    private static final Map<Class<? extends Model>, Model> modelMap = new ConcurrentHashMap<>();

    // 存储所有Model类的列表，按ModelOrder顺序
    private static final List<Class<Model>> modelClazzList = ModelOrder.getClazzList();

    // 存储所有Model实例的数组
    @Getter
    private static final Model[] modelArray = new Model[modelClazzList.size()];

    // 存储所有Model实例的列表
    private static final List<Model> modelList = new LinkedList<>(Arrays.asList(modelArray));

    // 只读Model实例列表
    private static final List<Model> readOnlyModelList = Collections.unmodifiableList(modelList);

    // 每个模型的启用字段
    private final BooleanModelField enableField;

    /**
     * 获取模型的启用字段
     *
     * @return 启用字段
     */
    public final BooleanModelField getEnableField() {
        return enableField;
    }

    /**
     * 默认构造函数，初始化启用字段
     */
    public Model() {
        this.enableField = new BooleanModelField("enable", getEnableFieldName(), false);
    }

    /**
     * 获取启用字段的名称
     *
     * @return 启用字段的名称
     */
    public String getEnableFieldName() {
        return "开启" + getName();
    }

    /**
     * 判断模型是否启用
     *
     * @return 如果启用则返回true，否则返回false
     */
    public final Boolean isEnable() {
        return enableField.getValue();
    }

    /**
     * 获取模型类型，默认返回NORMAL类型
     *
     * @return 模型类型
     */
    public ModelType getType() {
        return ModelType.NORMAL;
    }

    /**
     * 获取模型的名称，子类需要实现
     *
     * @return 模型名称
     */
    public abstract String getName();

    /**
     * 获取模型的组，子类需要实现
     *
     * @return 模型所属组
     */
    public abstract ModelGroup getGroup();

    /**
     * 获取模型的字段，子类需要实现
     *
     * @return 模型字段
     */
    public abstract ModelFields getFields();

    /**
     * 准备工作，可以被子类覆盖
     */
    public void prepare() {}

    /**
     * 启动模型，可以被子类覆盖
     *
     * @param classLoader 类加载器
     */
    public void boot(ClassLoader classLoader) {}

    /**
     * 销毁模型，可以被子类覆盖
     */
    public void destroy() {}

    /**
     * 获取所有模型配置的只读Map
     *
     * @return 模型配置Map
     */
    public static Map<String, ModelConfig> getModelConfigMap() {
        return readOnlyModelConfigMap;
    }

    /**
     * 获取所有模型分组的Set
     *
     * @return 模型组的Set
     */
    public static Set<ModelGroup> getGroupModelConfigGroupSet() {
        return groupModelConfigMap.keySet();
    }

    /**
     * 获取所有模型分组配置的Map列表
     *
     * @return 模型分组配置的Map列表
     */
    public static List<Map<String, ModelConfig>> getGroupModelConfigMapList() {
        List<Map<String, ModelConfig>> list = new ArrayList<>();
        for (Map<String, ModelConfig> modelConfigMap : groupModelConfigMap.values()) {
            list.add(Collections.unmodifiableMap(modelConfigMap));
        }
        return list;
    }

    /**
     * 获取特定模型组的配置Map
     *
     * @param modelGroup 模型组
     * @return 给定模型组的配置Map
     */
    public static Map<String, ModelConfig> getGroupModelConfig(ModelGroup modelGroup) {
        Map<String, ModelConfig> map = groupModelConfigMap.get(modelGroup);
        if (map == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * 检查是否有指定的模型
     *
     * @param modelClazz 模型类
     * @return 如果模型存在，返回true；否则返回false
     */
    public static Boolean hasModel(Class<? extends Model> modelClazz) {
        return modelMap.containsKey(modelClazz);
    }

    /**
     * 获取指定模型的实例
     *
     * @param modelClazz 模型类
     * @param <T>        模型类型
     * @return 返回指定模型类的实例
     */
    @SuppressWarnings("unchecked")
    public static <T extends Model> T getModel(Class<T> modelClazz) {
        return (T) modelMap.get(modelClazz);
    }

    /**
     * 获取所有模型的只读列表
     *
     * @return 所有模型的只读列表
     */
    public static List<Model> getModelList() {
        return readOnlyModelList;
    }

    /**
     * 初始化所有模型，销毁之前的模型实例，避免重复初始化
     */
    public static synchronized void initAllModel() {
        destroyAllModel();
        for (int i = 0, len = modelClazzList.size(); i < len; i++) {
            Class<Model> modelClazz = modelClazzList.get(i);
            try {
                Model model = modelClazz.newInstance();
                ModelConfig modelConfig = new ModelConfig(model);
                modelArray[i] = model;
                modelMap.put(modelClazz, model);
                String modelCode = modelConfig.getCode();
                modelConfigMap.put(modelCode, modelConfig);

                ModelGroup group = modelConfig.getGroup();
                // 兼容低版本SDK，使用传统方法
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    groupModelConfigMap.computeIfAbsent(group, k -> new LinkedHashMap<>())
                            .put(modelCode, modelConfig);
                } else {
                    // 如果低于API 24，使用传统方法
                    if (!groupModelConfigMap.containsKey(group)) {
                        groupModelConfigMap.put(group, new LinkedHashMap<>());
                    }
                    Objects.requireNonNull(groupModelConfigMap.get(group)).put(modelCode, modelConfig);
                }
            } catch (IllegalAccessException | InstantiationException e) {
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 启动所有模型
     *
     * @param classLoader 类加载器
     */
    public static synchronized void bootAllModel(ClassLoader classLoader) {
        for (Model model : modelArray) {
            try {
                model.prepare();
                if (model.getEnableField().getValue()) {
                    model.boot(classLoader);
                }
            } catch (Exception e) {
                Log.printStackTrace(e);
            }
        }
    }

    /**
     * 销毁所有模型
     */
    public static synchronized void destroyAllModel() {
        for (int i = 0, len = modelArray.length; i < len; i++) {
            Model model = modelArray[i];
            if (model != null) {
                try {
                    if (ModelType.TASK == model.getType()) {
                        ((ModelTask) model).stopTask();
                    }
                    model.destroy();
                } catch (Exception e) {
                    Log.printStackTrace(e);
                }
                modelArray[i] = null;
            }
        }
        modelMap.clear();
        modelConfigMap.clear();
    }
}
