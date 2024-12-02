package io.github.lazyimmortal.sesame.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import io.github.lazyimmortal.sesame.R;
import io.github.lazyimmortal.sesame.util.LibraryUtil;

/**
 * Date:2024/12/2
 * Time:15:50
 */
public class MainActivity extends AppCompatActivity {
  static {
    System.loadLibrary("sesame");
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_main);
    TextView tvFarm = findViewById(R.id.tvFarm);
    TextView tvCCL = findViewById(R.id.tvCCL);
    new Thread() {
      @SuppressLint("SetTextI18n")
      @Override
      public void run() {
        try {
          boolean sucFarm = LibraryUtil.doFarmTask(new JSONObject(taskFarm));
          boolean sucCCL = LibraryUtil.doFarmDrawTimesTask(new JSONObject(taskCCL));
          runOnUiThread(() -> {
            tvFarm.setText("农场任务结果:" + sucFarm);
            tvCCL.setText("抽抽乐任务结果:" + sucCCL);
          });
        } catch (Exception e) {
          Log.e("MainActivity", "JSON数据异常");
        }
      }
    }.start();
  }

  private final String taskFarm = "{\"awardCount\":90,\"awardType\":\"ALLPURPOSE\",\"backgroundColor\":\"#FF763E\",\"bizKey\":\"alty_leyoujigyg\",\"btnText\":\"去完成\",\"canReceiveAwardCount\":90,\"deliveryControlItem\":{\"appName\":\"antiep\",\"iepTaskTracer\":\"traceId:2183004717325885021655088eaae0~sceneCode:ANTFARM_FOOD_TASK~groupId:DUANWAI~taskType:alty_leyoujigyg~taskStatus:RECEIVED~decideRightsId:-~finishOnceAwardCnt:90\",\"itemCode\":\"alty_leyoujigyg\",\"sceneCode\":\"ANTFARM_FOOD_TASK\"},\"desc\":\"去逛一逛阿里体育-乐游记，完成即可获得90g饲料哦\",\"icon\":\"https://gw.alicdn.com/imgextra/i3/O1CN01SVf2ey1ImHJLqSBS3_!!6000000000935-2-tps-108-108.png\",\"receivePart\":false,\"rightsTimes\":1,\"rightsTimesLimit\":1,\"targetUrl\":\"alipays://platformapi/startapp?appId=2018073060792690&page=pages%2Fwakuang%2Findex%3FvisitSource%3DOUTER&query=chn%3Dalipay.mayizhuangyuan.liugou%26spm%3Da21bke.alipay_mayizhuangyuan_liugou.0.0\",\"taskId\":\"alty_leyoujigyg\",\"taskMode\":\"VIEW\",\"taskStatus\":\"RECEIVED\",\"title\":\"去阿里体育逛一逛\"}";
  private final String taskCCL = "{\"awardCount\":1,\"awardType\":\"DRAW_TIMES\",\"backgroundColor\":\"#00A446\",\"bizKey\":\"SHANGYEHUA_DRAW_TIMES\",\"btnText\":\"去完成\",\"canReceiveAwardCount\":1,\"deliveryControlItem\":{\"iepTaskTracer\":\"traceId:21ba1e2617323261239731974e1f72~sceneCode:ANTFARM_DRAW_TIMES_TASK~groupId:DRAW_TIMES_ACTIVITY_TASK~taskType:SHANGYEHUA_DRAW_TIMES~taskStatus:RECEIVED~decideRightsId:-~finishOnceAwardCnt:1\"},\"desc\":\"浏览杂货铺15s，可得1次机会\",\"icon\":\"https://mdn.alipayobjects.com/huamei_zxdryz/afts/img/A*jIhSSa7K-csAAAAAAAAAAAAADveNAQ/original\",\"receivePart\":false,\"rightsTimes\":1,\"rightsTimesLimit\":3,\"targetUrl\":\"alipays://platformapi/startapp?appId=2060090000304921&url=https%3A%2F%2Frender.alipay.com%2Fp%2Fyuyan%2F180020010001256918%2Fantfarm-landing.html%3FcaprMode%3Dsync&canPullDown=NO&showOptionMenu=NO&spaceCode=ANT_FARM_RAFFLE&renderConfigKey=mediaScene%2327%23%23adPosId%232023121322700074282%23%23spaceCode%23ANT_FARM_RAFFLE&iepTaskType=SHANGYEHUA_DRAW_TIMES&iepTaskSceneCode=ANTFARM_DRAW_TIMES_TASK&canDoTask=false&awardCount=1\",\"taskId\":\"SHANGYEHUA_DRAW_TIMES\",\"taskMode\":\"TRIGGER\",\"taskStatus\":\"RECEIVED\",\"title\":\"去杂货铺逛一逛\"}";
}
