package fansirsqi.xposed.sesame.util;

import org.json.JSONException;
import org.json.JSONObject;

public class ResUtil {
    private static final String TAG = ResUtil.class.getSimpleName();

    // 写一个将str转换为jsonobj的方法
    public static String strToJson(String str) {
        try {
            return new JSONObject(str).toString();
        } catch (JSONException e) {
            Log.printStackTrace(TAG, e);
            return null;
        }
    }

    /**
     * 检查JSON对象中的响应码（resultCode），并根据其类型和值返回处理结果。
     *
     * <p>
     * 该方法首先尝试从JSON对象中获取resultCode字段，然后根据其类型（整数或字符串）进行检查。
     * 如果resultCode的值符合预期（整数类型为200，字符串类型为"SUCCESS"或"100"），则返回true。
     * 如果resultCode不存在、类型不匹配或值不符合预期，将记录错误信息并返回false。
     * 如果在处理过程中发生异常，将捕获异常并记录错误信息，然后返回false。
     *
     * @param TAG 用来标识日志的标签
     * @param jo  包含resultCode的JSON对象
     * @return 如果resultCode符合预期，则返回true；否则返回false
     */
    public static Boolean checkResCode(String TAG, JSONObject jo) {
        try {
            // 尝试从JSON对象中获取resultCode字段
            Object resCode = jo.opt("resultCode");
            if (resCode == null) {
                // 如果resultCode不存在，记录日志并返回false
                Log.record(TAG + "checkResCode err: resultCode不存在");
                return false;
            }

            // 根据resultCode的类型进行处理
            if (resCode instanceof Integer) {
                // 如果resultCode是整数类型，检查是否等于200
                if ((Integer) resCode != 200) {
                    // 如果不等于200，记录错误信息并返回false
                    recordError(TAG, jo, "resultMsg", "checkResCode Integer err");
                    return false;
                }
                return true;
            } else if (resCode instanceof String) {
                // 如果resultCode是字符串类型，检查是否匹配"SUCCESS"或"100"（不区分大小写）
                if (!((String) resCode).matches("(?i)SUCCESS|100")) {
                    // 如果不匹配，记录错误信息并返回false
                    recordError(TAG, jo, "resultDesc", "checkResCode String err");
                    return false;
                }
                return true;
            }

            // 如果resultCode类型既不是整数也不是字符串，记录日志并返回false
            Log.record(TAG + "checkResCode Type fail: " + jo.toString());
            return false;
        } catch (Throwable t) {
            // 捕获并记录异常信息
            Log.runtime(TAG, "checkResCode error: " + t.getMessage());
        }
        // 如果发生异常，返回false
        return false;
    }

    private static void recordError(String TAG, JSONObject jo, String key, String prefix) throws JSONException {
        if (jo.has(key)) {
            Log.record(TAG + prefix + ": " + jo.getString(key));
        } else if (jo.has("resultView")) {
            Log.record(TAG + prefix + ": " + jo.getString("resultView"));
        } else {
            Log.record(TAG + prefix + ": " + jo.toString());
        }
    }
}
