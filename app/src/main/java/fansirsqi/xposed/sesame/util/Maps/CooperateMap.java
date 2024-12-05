package fansirsqi.xposed.sesame.util.Maps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fansirsqi.xposed.sesame.util.Files;
import fansirsqi.xposed.sesame.util.Log;

/**
 * 合作ID映射工具类。
 * 提供了一个线程安全的ID映射，支持添加、删除、加载和保存ID映射。
 * 用于存储和检索与合作相关的ID映射信息。
 */
public class CooperateMap {

    /**
     * 存储ID映射的并发HashMap。
     */
    private static final Map<String, String> idMap = new ConcurrentHashMap<>();

    /**
     * 只读的ID映射。
     */
    private static final Map<String, String> readOnlyIdMap = Collections.unmodifiableMap(idMap);

    /**
     * 获取只读的ID映射。
     * @return 只读的ID映射。
     */
    public static Map<String, String> getMap() {
        return readOnlyIdMap;
    }

    /**
     * 添加或更新ID映射。
     * @param key 键。
     * @param value 值。
     */
    public static synchronized void add(String key, String value) {
        idMap.put(key, value);
    }

    /**
     * 从ID映射中删除键值对。
     * @param key 键。
     */
    public static synchronized void remove(String key) {
        idMap.remove(key);
    }

    /**
     * 从文件加载ID映射。
     * @param userId 用户ID，用于确定文件名。
     */
    public static synchronized void load(String userId) {
        idMap.clear();
        try {
            String body = Files.readFromFile(Files.getCooperationIdMapFile(userId));
            if (!body.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> newMap = objectMapper.readValue(body, new TypeReference<Map<String, String>>() {});
                idMap.putAll(newMap);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    /**
     * 将ID映射保存到文件。
     * @param userId 用户ID，用于确定文件名。
     * @return 如果保存成功返回true，否则返回false。
     */
    public static synchronized boolean save(String userId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(idMap);
            return Files.write2File(json, Files.getCooperationIdMapFile(userId));
        } catch (Exception e) {
            Log.printStackTrace(e);
            return false;
        }
    }

}