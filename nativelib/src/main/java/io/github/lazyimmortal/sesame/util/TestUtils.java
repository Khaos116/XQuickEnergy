package io.github.lazyimmortal.sesame.util;

import android.content.Context;

import io.github.lazyimmortal.sesame.hook.ApplicationHook;
import io.github.lazyimmortal.sesame.model.task.antFarm.AntFarm;
import io.github.lazyimmortal.sesame.model.task.antFarm.AntFarmRpcCall;
import io.github.lazyimmortal.sesame.model.task.antForest.AntForestRpcCall;

/**
 * Author:XX
 * Date:2024/10/2
 * Time:15:50
 */
public class TestUtils {

  public static void test(Context context) {
    ApplicationHook hook = new ApplicationHook();
    hook.test();

    AntFarm farm = new AntFarm();
    farm.test();

    AntFarmRpcCall antFarmRpcCall = new AntFarmRpcCall();
    antFarmRpcCall.test();

    AntForestRpcCall antForestRpcCall = new AntForestRpcCall();
    antForestRpcCall.test();

    AESUtil aesUtil = new AESUtil();
    aesUtil.test();

    SesameValidator sesameValidator = new SesameValidator();
    sesameValidator.test(context);
  }
}
