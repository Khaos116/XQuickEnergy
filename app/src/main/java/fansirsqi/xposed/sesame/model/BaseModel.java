package fansirsqi.xposed.sesame.model;

import fansirsqi.xposed.sesame.util.Maps.BeachMap;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fansirsqi.xposed.sesame.model.modelFieldExt.BooleanModelField;
import fansirsqi.xposed.sesame.model.modelFieldExt.ChoiceModelField;
import fansirsqi.xposed.sesame.model.modelFieldExt.IntegerModelField;
import fansirsqi.xposed.sesame.model.modelFieldExt.ListModelField;
import fansirsqi.xposed.sesame.task.antOcean.AntOceanRpcCall;
import fansirsqi.xposed.sesame.task.reserve.ReserveRpcCall;
import fansirsqi.xposed.sesame.util.*;

/** 基础配置模块 */
public class BaseModel extends Model {

  /** 是否保持唤醒状态 */
  @Getter private static final BooleanModelField stayAwake = new BooleanModelField("stayAwake", "保持唤醒", true);

  /** 执行间隔时间（分钟） */
  @Getter
  private static final IntegerModelField.MultiplyIntegerModelField checkInterval =
      new IntegerModelField.MultiplyIntegerModelField("checkInterval", "执行间隔(分钟)", 30, 1, 12 * 60, 60_000);//此处调整至30分钟执行一次，可能会比平常耗电一点。。

  /** 定时执行的时间点列表 */
  @Getter
  private static final ListModelField.ListJoinCommaToStringModelField execAtTimeList =
      new ListModelField.ListJoinCommaToStringModelField("execAtTimeList", "定时执行(关闭:-1)", ListUtil.newArrayList("065530", "2359", "24"));

  /** 定时唤醒的时间点列表 */
  @Getter
  private static final ListModelField.ListJoinCommaToStringModelField wakenAtTimeList =
      new ListModelField.ListJoinCommaToStringModelField("wakenAtTimeList", "定时唤醒(关闭:-1)", ListUtil.newArrayList("0650","1250", "2350"));

  /** 能量收集的时间范围 */
  @Getter
  private static final ListModelField.ListJoinCommaToStringModelField energyTime =
      new ListModelField.ListJoinCommaToStringModelField("energyTime", "只收能量时间(范围)", ListUtil.newArrayList("0000-2359"));

  /** 定时任务模式选择 */
  @Getter private static final ChoiceModelField timedTaskModel = new ChoiceModelField("timedTaskModel", "定时任务模式", TimedTaskModel.SYSTEM, TimedTaskModel.nickNames);

  /** 超时是否重启 */
  @Getter private static final BooleanModelField timeoutRestart = new BooleanModelField("timeoutRestart", "超时重启", true);

  /** 异常发生时的等待时间（分钟） */
  @Getter
  private static final IntegerModelField.MultiplyIntegerModelField waitWhenException =
      new IntegerModelField.MultiplyIntegerModelField("waitWhenException", "异常等待时间(分钟)", 60, 0, 24 * 60, 60_000);

  /** 是否启用新接口（最低支持版本 v10.3.96.8100） */
  @Getter private static final BooleanModelField newRpc = new BooleanModelField("newRpc", "使用新接口(最低支持v10.3.96.8100)", true);

  /** 是否开启抓包调试模式 */
  @Getter private static final BooleanModelField debugMode = new BooleanModelField("debugMode", "开启抓包(基于新接口)", false);

  /** 是否申请支付宝的后台运行权限 */
  @Getter private static final BooleanModelField batteryPerm = new BooleanModelField("batteryPerm", "为支付宝申请后台运行权限", true);

  /** 是否记录日志 */
  @Getter private static final BooleanModelField recordLog = new BooleanModelField("recordLog", "记录日志", true);

  /** 是否显示气泡提示 */
  @Getter private static final BooleanModelField showToast = new BooleanModelField("showToast", "气泡提示", true);

  /** 气泡提示的纵向偏移量 */
  @Getter private static final IntegerModelField toastOffsetY = new IntegerModelField("toastOffsetY", "气泡纵向偏移", 85);

  /** 只显示中文并设置时区 */
  @Getter private static final BooleanModelField languageSimplifiedChinese = new BooleanModelField("languageSimplifiedChinese", "只显示中文并设置时区", true);

  /** 是否开启状态栏禁删 */
  @Getter private static final BooleanModelField enableOnGoing = new BooleanModelField("enableOnGoing", "开启状态栏禁删", false);

  @Override
  public String getName() {
    return "基础";
  }

  @Override
  public ModelGroup getGroup() {
    return ModelGroup.BASE;
  }

  @Override
  public String getEnableFieldName() {
    return "启用模块";
  }

  @Override
  public ModelFields getFields() {
    ModelFields modelFields = new ModelFields();
    modelFields.addField(stayAwake);
    modelFields.addField(checkInterval);
    modelFields.addField(execAtTimeList);
    modelFields.addField(wakenAtTimeList);
    modelFields.addField(energyTime);
    modelFields.addField(timedTaskModel);
    modelFields.addField(timeoutRestart);
    modelFields.addField(waitWhenException);
    modelFields.addField(newRpc);
    modelFields.addField(debugMode);
    modelFields.addField(batteryPerm);
    modelFields.addField(recordLog);
    modelFields.addField(showToast);
    modelFields.addField(enableOnGoing);
    modelFields.addField(languageSimplifiedChinese);
    modelFields.addField(toastOffsetY);
    return modelFields;
  }

  /** 初始化数据，通过异步线程加载初始化 Reserve 和 Beach 任务数据。 */
  public static void initData() {
    new Thread(
            () -> {
              try {
                initReserve();
                initBeach();
              } catch (Exception e) {
                Log.printStackTrace(e);
              }
            })
        .start();
  }

  /** 清理数据，在模块销毁时调用，清空 Reserve 和 Beach 数据。 */
  public static void destroyData() {
    try {
      ReserveIdMapUtil.clear();
      BeachMap.clear();
    } catch (Exception e) {
      Log.printStackTrace(e);
    }
  }

  /** 初始化保护地任务。通过 ReserveRpc 接口查询可兑换的树项目，将符合条件的保护地任务存入 ReserveIdMapUtil。 条件：项目类型为 "RESERVE" 且状态为 "AVAILABLE"。若调用失败则加载备份的 ReserveIdMapUtil。 */
  private static void initReserve() {
    try {
      // 调用 ReserveRpc 接口，查询可兑换的树项目列表
      String response = ReserveRpcCall.queryTreeItemsForExchange();

      // 若首次调用结果为空，进行延迟后再次调用
      if (response == null) {
        Thread.sleep(RandomUtil.delay());
        response = ReserveRpcCall.queryTreeItemsForExchange();
      }

      JSONObject jsonResponse = new JSONObject(response);

      // 检查接口调用是否成功，resultCode 为 SUCCESS 表示成功
      if ("SUCCESS".equals(jsonResponse.optString("resultCode", ""))) {
        JSONArray treeItems = jsonResponse.optJSONArray("treeItems");

        // 遍历所有树项目，筛选符合条件的保护地项目
        if (treeItems != null) {
          for (int i = 0; i < treeItems.length(); i++) {
            JSONObject item = treeItems.getJSONObject(i);

            // 跳过未定义 projectType 字段的项目
            if (!item.has("projectType")) {
              continue;
            }

            // 过滤出 projectType 为 "RESERVE" 且 applyAction 为 "AVAILABLE" 的项目
            if ("RESERVE".equals(item.getString("projectType")) && "AVAILABLE".equals(item.getString("applyAction"))) {

              // 将符合条件的项目添加到 ReserveIdMapUtil
              String itemId = item.getString("itemId");
              String itemName = item.getString("itemName");
              int energy = item.getInt("energy");
              ReserveIdMapUtil.add(itemId, itemName + "(" + energy + "g)");
            }
          }
        }

        // 将筛选结果保存到 ReserveIdMapUtil
        ReserveIdMapUtil.save();
      } else {
        // 若 resultCode 不为 SUCCESS，记录错误描述
        Log.runtime(jsonResponse.optString("resultDesc", "未知错误"));
      }
    } catch (JSONException e) {
      // 捕获 JSON 解析错误并记录日志
      Log.runtime("JSON 解析错误：" + e.getMessage());
      Log.printStackTrace(e);
      ReserveIdMapUtil.load(); // 若出现异常则加载保存的 ReserveIdMapUtil 备份
    } catch (Exception e) {
      // 捕获所有其他异常并记录
      Log.runtime("初始化保护地任务时出错：" + e.getMessage());
      Log.printStackTrace(e);
      ReserveIdMapUtil.load(); // 加载备份的 ReserveIdMapUtil
    }
  }

  /** 初始化沙滩任务。通过调用 AntOceanRpc 接口查询养成列表，并将符合条件的任务加入 BeachMap。 条件：养成项目的类型必须为 BEACH、COOPERATE_SEA_TREE 或 SEA_ANIMAL， 并且其状态为 AVAILABLE。最后将符合条件的任务保存到 BeachMap 中。 */
  private static void initBeach() {
    try {
      // 调用 AntOceanRpc 接口，查询养成列表信息
      String response = AntOceanRpcCall.queryCultivationList();
      JSONObject jsonResponse = new JSONObject(response);

      // 判断调用是否成功，resultCode 为 SUCCESS 表示成功
      if ("SUCCESS".equals(jsonResponse.optString("resultCode", ""))) {
        // 获取 cultivationItemVOList 列表，包含所有养成项目
        JSONArray cultivationList = jsonResponse.optJSONArray("cultivationItemVOList");

        // 遍历养成列表，筛选符合条件的项目
        if (cultivationList != null) {
          for (int i = 0; i < cultivationList.length(); i++) {
            JSONObject item = cultivationList.getJSONObject(i);

            // 跳过未定义 templateSubType 字段的项目
            if (!item.has("templateSubType")) {
              continue;
            }

            // 检查 templateSubType 是否符合指定类型
            String templateSubType = item.getString("templateSubType");
            if (!"BEACH".equals(templateSubType) && !"COOPERATE_SEA_TREE".equals(templateSubType) && !"SEA_ANIMAL".equals(templateSubType)) {
              continue;
            }

            // 检查 applyAction 是否为 AVAILABLE
            if (!"AVAILABLE".equals(item.getString("applyAction"))) {
              continue;
            }

            // 将符合条件的项目添加到 BeachMap
            String templateCode = item.getString("templateCode");
            String cultivationName = item.getString("cultivationName");
            int energy = item.getInt("energy");
            BeachMap.add(templateCode, cultivationName + "(" + energy + "g)");
          }
        }

        // 将所有筛选结果保存到 BeachMap
        BeachMap.save();
      } else {
        // 若 resultCode 不为 SUCCESS，记录错误描述
        Log.runtime(jsonResponse.optString("resultDesc", "未知错误"));
      }
    } catch (JSONException e) {
      // 记录 JSON 解析过程中的异常
      Log.runtime("JSON 解析错误：" + e.getMessage());
      Log.printStackTrace(e);
      BeachMap.load(); // 若出现异常则加载保存的 BeachMap 备份
    } catch (Exception e) {
      // 捕获所有其他异常并记录
      Log.runtime("初始化沙滩任务时出错：" + e.getMessage());
      Log.printStackTrace(e);
      BeachMap.load(); // 加载保存的 BeachMap 备份
    }
  }

  public interface TimedTaskModel {

    int SYSTEM = 0;

    int PROGRAM = 1;

    String[] nickNames = {"系统计时", "程序计时"};
  }
}
