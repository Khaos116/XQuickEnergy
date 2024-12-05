package fansirsqi.xposed.sesame.entity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fansirsqi.xposed.sesame.util.Files;
import fansirsqi.xposed.sesame.util.Log;

/**
 * 区域代码类，继承自IdAndName。
 * 该类用于管理城市代码和城市名称。
 */
public class AreaCode extends IdAndName {
    private static final String TAG = AreaCode.class.getSimpleName();
    private static List<AreaCode> list;

    /**
     * 构造函数，初始化区域代码对象。
     *
     * @param i 区域代码
     * @param n 区域名称
     */
    public AreaCode(String i, String n) {
        id = i;
        name = n;
    }

    /**
     * 获取区域代码列表。
     * 如果列表尚未初始化，则从文件中读取城市代码。
     * 如果读取失败，则使用默认城市代码。
     *
     * @return 区域代码列表
     */
    public static List<AreaCode> getList() {
        if (list == null) {
            String cityCode = Files.readFromFile(Files.getCityCodeFile());
            JSONArray ja = parseCityCode(cityCode);
            list = new ArrayList<>();

            // 将JSONArray中的数据转换为AreaCode对象并添加到列表中
            for (int i = 0; i < ja.length(); i++) {
                try {
                    JSONObject jo = ja.getJSONObject(i);
                    list.add(new AreaCode(jo.getString("cityCode"), jo.getString("cityName")));
                } catch (JSONException e) {
                    Log.printStackTrace(TAG, e);
                }
            }
        }
        return list;
    }

    /**
     * 解析城市代码字符串为JSONArray。
     * 如果解析失败，则返回默认的城市代码JSONArray。
     *
     * @param cityCode 城市代码字符串
     * @return 解析后的JSONArray
     */
    private static JSONArray parseCityCode(String cityCode) {
        try {
            return new JSONArray(cityCode);
        } catch (JSONException e) {
            // 解析失败，使用默认城市代码
            String defaultCityCode = "[" +
                    "{\"cityCode\":\"320100\",\"cityName\":\"南京市\"}," +
                    "{\"cityCode\":\"330100\",\"cityName\":\"杭州市\"}," +
                    "{\"cityCode\":\"350100\",\"cityName\":\"福州市\"}," +
                    "{\"cityCode\":\"370100\",\"cityName\":\"济南市\"}," +
                    "{\"cityCode\":\"430100\",\"cityName\":\"长沙市\"}," +
                    "{\"cityCode\":\"440100\",\"cityName\":\"广州市\"}" +
                    "]";
            try {
                return new JSONArray(defaultCityCode);
            } catch (JSONException ex) {
                return new JSONArray(); // 返回空的JSONArray
            }
        }
    }
}