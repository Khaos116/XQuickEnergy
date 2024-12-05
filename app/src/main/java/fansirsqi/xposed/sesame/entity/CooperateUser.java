package fansirsqi.xposed.sesame.entity;

import fansirsqi.xposed.sesame.util.Maps.CooperateMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 表示合作用户的实体类，包含 ID 和名称。
 */
public class CooperateUser extends IdAndName {

    /**
     * 构造方法，根据给定的 ID 和名称初始化合作用户对象。
     * @param i 用户的 ID
     * @param n 用户的名称
     */
    public CooperateUser(String i, String n) {
        id = i;
        name = n;
    }

    /**
     * 获取所有合作用户的列表。
     * @return 包含所有合作用户的 CooperateUser 对象列表
     */
    public static List<CooperateUser> getList() {
        List<CooperateUser> list = new ArrayList<>();
        Set<Map.Entry<String, String>> idSet = CooperateMap.getMap().entrySet();

        // 遍历映射表的每一项，添加到用户列表中
        for (Map.Entry<String, String> entry : idSet) {
            list.add(new CooperateUser(entry.getKey(), entry.getValue()));
        }
        return list;
    }
}
