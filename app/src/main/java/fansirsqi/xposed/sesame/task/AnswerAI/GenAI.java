package fansirsqi.xposed.sesame.task.AnswerAI;

import fansirsqi.xposed.sesame.util.Log;
import okhttp3.*;
import org.json.JSONObject;

import java.util.List;

import static fansirsqi.xposed.sesame.util.JsonUtil.getValueByPath;

/**
 * GenAI帮助类，用于与GenAI接口交互以获取AI回答
 * 支持单条文本问题及带有候选答案列表的问题请求
 */
public class GenAI implements AnswerAIInterface {
    private final String TAG = GenAI.class.getSimpleName();

    // GenAI服务接口URL
    private final String url = "https://api.genai.gd.edu.kg/google";

    // 认证Token，用于访问GenAI接口
    private final String token;

    /**
     * 私有构造函数，防止外部实例化
     *
     * @param token API访问令牌
     */
    public GenAI(String token) {
        this.token = (token != null && !token.isEmpty()) ? token : "";
    }

    /**
     * 向AI接口发送请求获取回答
     *
     * @param text 问题内容
     * @return AI回答结果，空字符串表示请求失败或无结果
     */
    @Override
    public String getAnswer(String text) {
        String result = "";
        try {
            // 构造请求体内容
            String content = "{\n" +
                    "    \"contents\": [\n" +
                    "        {\n" +
                    "            \"parts\": [\n" +
                    "                {\n" +
                    "                    \"text\": \"只回答答案 " + text + "\"\n" +
                    "                }\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";

            // 配置OkHttp客户端和请求体
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(content, mediaType);
            String requestUrl = url + "/v1beta/models/gemini-1.5-flash:generateContent?key=" + token;

            // 构建HTTP请求
            Request request = new Request.Builder()
                    .url(requestUrl)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 执行请求并获取响应
            Response response = client.newCall(request).execute();
            if (response.body() == null) {
                return result;
            }

            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                Log.other("Gemini请求失败");
                Log.runtime("Gemini接口异常：" + jsonResponse);
                return result; // 可能的API Key错误
            }

            // 解析JSON响应，获取回答内容
            JSONObject jsonObject = new JSONObject(jsonResponse);
            result = getValueByPath(jsonObject, "candidates.[0].content.parts.[0].text");
        } catch (Throwable t) {
            Log.printStackTrace(TAG, t);
        }
        return result;
    }

    /**
     * 向AI接口发送请求，结合候选答案判断最终的回答
     *
     * @param title     问题标题
     * @param answerList 答案集合
     * @return 匹配的答案，空字符串表示无匹配或请求失败
     */
    @Override
    public String getAnswer(String title, List<String> answerList) {
        // 构建候选答案的字符串表示
        StringBuilder answerStr = new StringBuilder();
        for (String answer : answerList) {
            answerStr.append("[").append(answer).append("]");
        }

        // 发送请求并获取AI回答结果
        String answerResult = getAnswer(title + "\n" + answerStr);
        if (answerResult != null && !answerResult.isEmpty()) {
            Log.record("AI🧠回答：" + answerResult);

            // 查找并返回与候选答案匹配的项
            for (String answer : answerList) {
                if (answerResult.contains(answer)) {
                    return answer;
                }
            }
        }
        return "";
    }
}
