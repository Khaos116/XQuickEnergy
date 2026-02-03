package fansirsqi.xposed.sesame.task.AnswerAI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import fansirsqi.xposed.sesame.util.JsonUtil;
import fansirsqi.xposed.sesame.util.Log;
import okhttp3.*;

/**
 * GenAI帮助类
 *
 * @author Xiong
 */
public class GeminiAI implements AnswerAIInterface {
  @Override
  public void setModelName(String modelName) {
    Log.other("不接受更改Gemini模型:" + modelName);
  }

  @Override
  public String getModelName() {
    return "gemini-2.5-flash";
  }

  @Override
  public String getAnswerStr(String text, String model) {
    setModelName(model);
    return getAnswerStr(text);
  }

  private final String TAG = GeminiAI.class.getSimpleName();

  private final String apiUrl = "https://api.genai.gd.edu.kg/google";

  private final String token;

  // 私有构造函数，防止外部实例化
  public GeminiAI(String token) {
    if (token != null && !token.isEmpty()) {
      this.token = token;
    } else {
      this.token = "";
    }
  }

  /**
   * 获取AI回答结果
   *
   * @param text 问题内容
   * @return AI回答结果
   */
  @Override
  public String getAnswerStr(String text) {
    Response response = null;
    try {
      JSONObject jsonReq = new JSONObject();
      // 针对选择题优化的 Prompt
      String fullPrompt = "直接给出答案文字，严禁解释，不要标点符号。题目：" + text;

      JSONArray contents = new JSONArray();
      contents.put(new JSONObject().put("parts", new JSONArray().put(new JSONObject().put("text", fullPrompt))));
      jsonReq.put("contents", contents);

      // 必须开启 google_search，否则无法回答最新的常识题（如蚂蚁庄园）
      jsonReq.put("tools", new JSONArray().put(new JSONObject().put("google_search", new JSONObject())));

      OkHttpClient client = new OkHttpClient();
      RequestBody body = RequestBody.create(jsonReq.toString(), MediaType.parse("application/json"));

      String modelName = "gemini-2.5-flash"; // 确保使用你刚才测通的模型
      String finalUrl = apiUrl + "/v1beta/models/" + modelName + ":generateContent?key=" + token;

      Request request = new Request.Builder().url(finalUrl).post(body).build();
      response = client.newCall(request).execute();

      if (response.body() != null) {
        String jsonStr = response.body().string();
        // 调试建议：Log.i("Gemini Raw: " + jsonStr);
        JSONObject resObj = new JSONObject(jsonStr);
        String answer = JsonUtil.getValueByPath(resObj, "candidates.[0].content.parts.[0].text");

        if (answer != null) {
          // 清理所有可能干扰匹配的杂质
          return answer.trim().replaceAll("[。，.！!？? \"'“”]", "");
        }
      }
    } catch (Exception e) {
      Log.printStackTrace(TAG, e);
      Log.error("Gemini答题出错: " + e.getMessage());
      Log.other("Gemini答题出错: " + e.getMessage());
    } finally {
      if (response != null) response.close();
    }
    return "";
  }

  /**
   * 获取答案
   *
   * @param title      问题
   * @param answerList 答案集合
   * @return 空没有获取到
   */
  @Override
  public Integer getAnswer(String title, List<String> answerList) {
    StringBuilder answerStr = new StringBuilder();
    for (String answer : answerList) {
      answerStr.append("[").append(answer).append("]");
    }
    String answerResult = getAnswerStr(title + "\n" + answerStr);
    if (answerResult != null && !answerResult.isEmpty()) {
      for (int i = 0, size = answerList.size(); i < size; i++) {
        if (answerResult.contains(answerList.get(i))) {
          return i;
        }
      }
    }
    return -1;
  }
}

