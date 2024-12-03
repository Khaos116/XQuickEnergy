#include <jni.h>
#include <android/log.h>
#include <set>
#include <string>

#define LOG_TAG "懒真人"
#define LogI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LogE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

//封装打印方法
// 声明一个全局变量来存储 JNI 环境
static JNIEnv *g_env = nullptr;

// 初始化 JNI 环境
void initJNI(JNIEnv *env) {
    if (!g_env) {
        g_env = env;
    }
}

// 封装打印函数，支持格式化
extern "C" void logMessage(const char *format, ...) {
    if (!g_env) {
        LogE("需要先调用initJNI");
        return;
    }

    // 创建一个可变参数列表
    va_list args;
    va_start(args, format);

    // 创建一个缓冲区，存储格式化后的日志消息
    char buffer[256];  // 您可以根据需要调整大小
    vsnprintf(buffer, sizeof(buffer), format, args);

    va_end(args);

    // 获取 Log 类的引用
    jclass logClass = g_env->FindClass("io/github/lazyimmortal/sesame/util/Log");
    if (logClass == nullptr) {
        LogE("没有发现Log类");
        return;
    }

    // 获取 Log 类中的 static 方法 'other' 的方法ID
    jmethodID logMethod = g_env->GetStaticMethodID(logClass, "other", "(Ljava/lang/String;)V");
    if (logMethod == nullptr) {
        LogE("没有发现Log.other方法");
        g_env->DeleteLocalRef(logClass);
        return;
    }

    // 创建一个 Java 字符串作为参数
    jstring jMessage = g_env->NewStringUTF(buffer);

    // 调用 Java 的 Log.other 方法
    g_env->CallStaticVoidMethod(logClass, logMethod, jMessage);

    // 释放局部引用
    g_env->DeleteLocalRef(logClass);
    g_env->DeleteLocalRef(jMessage);
}

//static 调用 jclass cls
//非static 调用 jobject obj

//public static native String encryptData(String str);
extern "C" JNIEXPORT jstring JNICALL
Java_io_github_lazyimmortal_sesame_util_AESUtil_encryptData(JNIEnv *env, jclass cls, jstring str) {
    return str;
}

//public static native String decryptData(String str);
extern "C" JNIEXPORT jstring JNICALL
Java_io_github_lazyimmortal_sesame_util_AESUtil_decryptData(JNIEnv *env, jclass cls, jstring str) {
    return str;
}

//private static native boolean libraryCheckFarmTaskStatus(JSONObject task);
extern "C" JNIEXPORT jboolean JNICALL
Java_io_github_lazyimmortal_sesame_util_LibraryUtil_libraryCheckFarmTaskStatus(JNIEnv *env, jclass cls, jobject json) {
    return JNI_TRUE;
}

//private static native boolean libraryDoFarmTask(JSONObject task);
extern "C" JNIEXPORT jboolean JNICALL
Java_io_github_lazyimmortal_sesame_util_LibraryUtil_libraryDoFarmTask(JNIEnv *env, jclass cls, jobject json) {
    initJNI(env);
    // 获取 ApplicationHook 类的引用
    jclass appHookClass = env->FindClass("io/github/lazyimmortal/sesame/hook/ApplicationHook");
    if (appHookClass == nullptr) {
        LogE("庄园任务ApplicationHook未找到");
        env->DeleteLocalRef(appHookClass);
        return JNI_FALSE; // 类未找到
    }
    // 获取 requestString 方法的 ID
    jmethodID requestStringMethod = env->GetStaticMethodID(appHookClass, "requestString", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (requestStringMethod == nullptr) {
        LogE("庄园任务ApplicationHook.requestString未找到");
        env->DeleteLocalRef(appHookClass);
        return JNI_FALSE; // 方法未找到
    }
    //=================================================读取参数=================================================//
    // 获取 JSONObject 类
    jclass jOBC = env->GetObjectClass(json);
    if (jOBC == nullptr) {
        LogE("庄园任务JSON异常");
        env->DeleteLocalRef(appHookClass);
        env->DeleteLocalRef(jOBC);
        return JNI_FALSE; // 类未找到
    }

    // 获取 optString 方法的 ID
    jmethodID optSM = env->GetMethodID(jOBC, "optString", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (optSM == nullptr) {
        LogE("庄园任务JSON读取String异常");
        env->DeleteLocalRef(appHookClass);
        env->DeleteLocalRef(jOBC);
        return JNI_FALSE; // 方法未找到
    }

    // 创建 bizKey 和 taskMode 的 Java 字符串
    jstring bizKeyName = env->NewStringUTF("bizKey");
    jstring taskModeName = env->NewStringUTF("taskMode");
    jstring titleName = env->NewStringUTF("title");
    jstring jDefaultValue = env->NewStringUTF("");

    // 调用 optString 方法获取 bizKey 和 taskMode
    auto bizKey = (jstring) env->CallObjectMethod(json, optSM, bizKeyName, jDefaultValue);
    auto taskMode = (jstring) env->CallObjectMethod(json, optSM, taskModeName, jDefaultValue);
    auto title = (jstring) env->CallObjectMethod(json, optSM, titleName, jDefaultValue);
    env->DeleteLocalRef(jDefaultValue);
    if (bizKey != nullptr && taskMode != nullptr) {
        // 将 bizKey 和 taskMode 转换为 C 字符串
        const char *bizKeyStr = env->GetStringUTFChars(bizKey, nullptr);
        const char *taskModeStr = env->GetStringUTFChars(taskMode, nullptr);
        const char *titleStr = env->GetStringUTFChars(title, nullptr);

        LogI("庄园任务:bizKey=%s", bizKeyStr);
        LogI("庄园任务:taskMode=%s", taskModeStr);
        LogI("庄园任务:title=%s", titleStr);
        // 定义 bizKey 有效值集合
        std::set<std::string> validBizKeys = {
                "ADD_GONGGE_NEW",
                "USER_STARVE_PUSH",
                "YEB_PURCHASE",
                "WIDGET_addzujian",
                "HIRE_LOW_ACTIVITY",
                "DIANTAOHUANDUAN",
                "TAO_GOLDEN_V2",
                "SHANGYEHUA_90_1",
                "TAOBAO_tab2gzy",
                "YITAO_appgyg",
                "ANTFARM_chouchoule",
                "TB_qiandao2023",
                "BABAFARM_TB",
                "TB_chongzhi",
                "ALIPAIMAI_gygzy",
                "BABA_FARM_SPREAD_MANURE",
                "ELM_hudong2024",
                "2024XIANYU_huanduan",
                "JINGTAN_FEED_FISH",
                "UC_gygzy",
                "TAOBAO_renshenggyg",
                "SLEEP",
                "HEART_DONATION_ADVANCED_FOOD_V2",
                "xincun2023",
                "BBNC_gyg",
                "XJLYKBX1_sl90",
                "2023ZMF_gyg_v2",
                "FAMILY_COOPERATE_TASK",
                "TOUTIAO_daoduan",
                "KUAISHOU_DAODUAN2023",
                "ANTMEMBER_RICHANGQIANDAO",
                "HUABEI2023",
                "KUAISHOU_daoduanv2",
                "alty_leyoujigyg",
        };

        // 检查 taskMode 和 bizKey 的条件
        if (strcmp(taskModeStr, "VIEW") == 0 || validBizKeys.find(bizKeyStr) != validBizKeys.end()) {
            //=================================================执行调用=================================================//
            jstring version = env->NewStringUTF("1.8.2302070202.46");
            const char *versionStr = env->GetStringUTFChars(version, nullptr);
            // 构建 JSON 字符串
            char temp[256]; // 确保这个缓冲区足够大以容纳 JSON 字符串
            snprintf(temp, sizeof(temp), R"([{"bizKey":"%s","requestType":"NORMAL","sceneCode":"ANTFARM","source":"H5","version":"%s"}])", bizKeyStr, versionStr);

            // 创建 Java 字符串
            jstring requestData = env->NewStringUTF(temp);
            if (requestData == nullptr) {
                LogE("庄园任务请求参数创建失败");
                env->DeleteLocalRef(appHookClass);
                env->DeleteLocalRef(jOBC);
                env->ReleaseStringUTFChars(bizKey, bizKeyStr);
                env->ReleaseStringUTFChars(taskMode, taskModeStr);
                env->ReleaseStringUTFChars(title, titleStr);
                env->DeleteLocalRef(bizKeyName);
                env->DeleteLocalRef(taskModeName);
                env->ReleaseStringUTFChars(version, versionStr);
                env->DeleteLocalRef(requestData);
                validBizKeys.clear();
                return JNI_FALSE; // 创建字符串失败
            }
            // 创建参数字符串
            jstring method = env->NewStringUTF("com.alipay.antfarm.doFarmTask");
            // 调用 requestString 方法
            auto result = (jstring) env->CallStaticObjectMethod(appHookClass, requestStringMethod, method, requestData);
            // 处理结果
            if (result != nullptr) {
                const char *resultStr = env->GetStringUTFChars(result, nullptr);
                // 将 C 字符串转换为 Java 字符串
                jstring resultJsonString = env->NewStringUTF(resultStr);

                // 获取 JSONObject 类
                jclass jsonObjectClass = env->FindClass("org/json/JSONObject");
                if (jsonObjectClass == nullptr) {
                    LogE("庄园任务JSONObject类未找到");
                    env->DeleteLocalRef(appHookClass);
                    env->DeleteLocalRef(jOBC);
                    env->ReleaseStringUTFChars(bizKey, bizKeyStr);
                    env->ReleaseStringUTFChars(taskMode, taskModeStr);
                    env->ReleaseStringUTFChars(title, titleStr);
                    env->DeleteLocalRef(bizKeyName);
                    env->DeleteLocalRef(taskModeName);
                    env->ReleaseStringUTFChars(version, versionStr);
                    env->DeleteLocalRef(requestData);
                    validBizKeys.clear();
                    env->DeleteLocalRef(method);
                    env->ReleaseStringUTFChars(result, resultStr);
                    env->DeleteLocalRef(resultJsonString);
                    return JNI_FALSE; // 类未找到
                }

                // 获取构造函数 ID
                jmethodID jsonConstructor = env->GetMethodID(jsonObjectClass, "<init>", "(Ljava/lang/String;)V");
                if (jsonConstructor == nullptr) {
                    LogE("庄园任务JSONObject构造函数未找到");
                    env->DeleteLocalRef(appHookClass);
                    env->DeleteLocalRef(jOBC);
                    env->ReleaseStringUTFChars(bizKey, bizKeyStr);
                    env->ReleaseStringUTFChars(taskMode, taskModeStr);
                    env->ReleaseStringUTFChars(title, titleStr);
                    env->DeleteLocalRef(bizKeyName);
                    env->DeleteLocalRef(taskModeName);
                    env->ReleaseStringUTFChars(version, versionStr);
                    env->DeleteLocalRef(requestData);
                    validBizKeys.clear();
                    env->DeleteLocalRef(method);
                    env->ReleaseStringUTFChars(result, resultStr);
                    env->DeleteLocalRef(resultJsonString);
                    env->DeleteLocalRef(jsonObjectClass);
                    return JNI_FALSE; // 方法未找到
                }

                // 创建 JSONObject 实例
                jobject jsonObject = env->NewObject(jsonObjectClass, jsonConstructor, resultJsonString);
                if (jsonObject == nullptr) {
                    LogE("庄园任务创建JSONObject失败");
                    env->DeleteLocalRef(appHookClass);
                    env->DeleteLocalRef(jOBC);
                    env->ReleaseStringUTFChars(bizKey, bizKeyStr);
                    env->ReleaseStringUTFChars(taskMode, taskModeStr);
                    env->ReleaseStringUTFChars(title, titleStr);
                    env->DeleteLocalRef(bizKeyName);
                    env->DeleteLocalRef(taskModeName);
                    env->ReleaseStringUTFChars(version, versionStr);
                    env->DeleteLocalRef(requestData);
                    validBizKeys.clear();
                    env->DeleteLocalRef(method);
                    env->ReleaseStringUTFChars(result, resultStr);
                    env->DeleteLocalRef(resultJsonString);
                    env->DeleteLocalRef(jsonObjectClass);
                    return JNI_FALSE; // 创建失败
                }

                // 获取 optString 方法的 ID
                jmethodID optStringMethod = env->GetMethodID(jsonObjectClass, "optString", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
                if (optStringMethod == nullptr) {
                    LogE("庄园任务JSONObject.getString方法未找到");
                    env->DeleteLocalRef(appHookClass);
                    env->DeleteLocalRef(jOBC);
                    env->ReleaseStringUTFChars(bizKey, bizKeyStr);
                    env->ReleaseStringUTFChars(taskMode, taskModeStr);
                    env->ReleaseStringUTFChars(title, titleStr);
                    env->DeleteLocalRef(bizKeyName);
                    env->DeleteLocalRef(taskModeName);
                    env->ReleaseStringUTFChars(version, versionStr);
                    env->DeleteLocalRef(requestData);
                    validBizKeys.clear();
                    env->DeleteLocalRef(method);
                    env->ReleaseStringUTFChars(result, resultStr);
                    env->DeleteLocalRef(resultJsonString);
                    env->DeleteLocalRef(jsonObjectClass);
                    env->DeleteLocalRef(jsonObject);
                    return JNI_FALSE; // 方法未找到
                }

                // 创建 memo 字符串
                jstring memoName = env->NewStringUTF("memo");
                jstring jdv = env->NewStringUTF("");
                // 调用 optString 方法获取 memo
                auto memo = (jstring) env->CallObjectMethod(jsonObject, optStringMethod, memoName, jdv);

                // 清理局部引用
                env->DeleteLocalRef(jdv);
                env->DeleteLocalRef(memoName);

                if (memo != nullptr) {
                    const char *memoStr = env->GetStringUTFChars(memo, nullptr);
                    // 检查 memo 的值
                    bool isSuccess = strcmp(memoStr, "SUCCESS") == 0;
                    LogE("庄园任务【%s】执行结果【%s】", titleStr, isSuccess ? "Success" : "Fail");
                    jstring awardCountName = env->NewStringUTF("awardCount");
                    jstring jdvT = env->NewStringUTF("");
                    // 调用 optString 方法获取 awardCount
                    auto awardCount = (jstring) env->CallObjectMethod(jsonObject, optStringMethod, awardCountName, jdvT);
                    env->DeleteLocalRef(awardCountName);
                    env->DeleteLocalRef(jdvT);
                    if (awardCount != nullptr) {
                        const char *awardCountStr = env->GetStringUTFChars(awardCount, nullptr);
                        LogI("庄园任务🧾[%s]#获得饲料%sg", titleStr, awardCountStr);
                        logMessage("SO庄园任务🧾[%s]#获得饲料%sg", titleStr, awardCountStr);
                        env->ReleaseStringUTFChars(awardCount, awardCountStr);
                    } else {
                        logMessage("SO庄园任务【%s】执行【%s】", titleStr, isSuccess ? "成功" : "失败");
                    }
                    // 释放资源
                    env->DeleteLocalRef(appHookClass);
                    env->DeleteLocalRef(jOBC);
                    env->ReleaseStringUTFChars(bizKey, bizKeyStr);
                    env->ReleaseStringUTFChars(taskMode, taskModeStr);
                    env->ReleaseStringUTFChars(title, titleStr);
                    env->ReleaseStringUTFChars(version, versionStr);
                    env->DeleteLocalRef(requestData);
                    validBizKeys.clear();
                    env->DeleteLocalRef(method);
                    env->ReleaseStringUTFChars(result, resultStr);
                    env->DeleteLocalRef(resultJsonString);
                    env->DeleteLocalRef(jsonObjectClass);
                    env->DeleteLocalRef(jsonObject);
                    env->ReleaseStringUTFChars(memo, memoStr);
                    return isSuccess ? JNI_TRUE : JNI_FALSE; // 返回 true 或 false
                }

                // 清理资源
                env->DeleteLocalRef(resultJsonString);
                env->DeleteLocalRef(jsonObjectClass);
                env->DeleteLocalRef(jsonObject);
                env->DeleteLocalRef(memo);
            } else {
                LogE("庄园任务响应值为空");
            }

            // 清理局部引用
            env->ReleaseStringUTFChars(version, versionStr);
            env->DeleteLocalRef(requestData);
            validBizKeys.clear();
            env->DeleteLocalRef(method);
            env->DeleteLocalRef(result);
        } else {
            // 否则释放资源并返回 false
            LogI("庄园无效任务");
            env->ReleaseStringUTFChars(bizKey, bizKeyStr);
            env->ReleaseStringUTFChars(taskMode, taskModeStr);
            env->ReleaseStringUTFChars(title, titleStr);
            env->DeleteLocalRef(bizKeyName);
            env->DeleteLocalRef(taskModeName);
            env->DeleteLocalRef(bizKey);
            env->DeleteLocalRef(taskMode);
            return JNI_FALSE;
        }

        // 释放资源
        env->ReleaseStringUTFChars(bizKey, bizKeyStr);
        env->ReleaseStringUTFChars(taskMode, taskModeStr);
        env->ReleaseStringUTFChars(title, titleStr);
        env->DeleteLocalRef(bizKey);
        env->DeleteLocalRef(taskMode);
    }
    env->DeleteLocalRef(appHookClass);
    env->DeleteLocalRef(jOBC);
    env->DeleteLocalRef(bizKey);
    env->DeleteLocalRef(taskMode);
    env->DeleteLocalRef(bizKeyName);
    env->DeleteLocalRef(taskModeName);
    return JNI_FALSE;
}

//private static native boolean libraryDoFarmDrawTimesTask(JSONObject task);
/**
 * const char *jsonChars = env->GetStringUTFChars(jsonString, nullptr);
 * 释放资源代码：env->ReleaseStringUTFChars(jsonString, jsonChars);
 */
/**
 * jobject jsonObject = env->NewObject(jsonClass, jsonConstructor, jsonString);
 * 释放资源代码：env->DeleteLocalRef(jsonObject);
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_io_github_lazyimmortal_sesame_util_LibraryUtil_libraryDoFarmDrawTimesTask(JNIEnv *env, jclass cls, jobject json) {
    initJNI(env);
    // 获取 ApplicationHook 类的引用
    jclass appHookClass = env->FindClass("io/github/lazyimmortal/sesame/hook/ApplicationHook");
    if (appHookClass == nullptr) {
        LogE("抽抽乐ApplicationHook未找到");
        env->DeleteLocalRef(appHookClass);
        return JNI_FALSE; // 类未找到
    }

    // 获取 requestString 方法的 ID
    jmethodID requestStringMethod = env->GetStaticMethodID(appHookClass, "requestString", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    if (requestStringMethod == nullptr) {
        LogE("抽抽乐ApplicationHook.requestString未找到");
        env->DeleteLocalRef(appHookClass);
        return JNI_FALSE; // 方法未找到
    }
    // 获取 JSONObject 的类
    jclass jsonOBC = env->GetObjectClass(json);
    if (jsonOBC == nullptr) {
        LogE("抽抽乐JSON异常");
        env->DeleteLocalRef(appHookClass);
        env->DeleteLocalRef(jsonOBC);
        return JNI_FALSE;
    }

    // 获取 optString 和 optInt 方法
    jmethodID optSM = env->GetMethodID(jsonOBC, "optString", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    jmethodID optIM = env->GetMethodID(jsonOBC, "optInt", "(Ljava/lang/String;I)I");

    // 创建 Java 字符串参数
    jstring titleKey = env->NewStringUTF("title");
    jstring taskIdKey = env->NewStringUTF("taskId");
    jstring rightsTimesKey = env->NewStringUTF("rightsTimes");
    jstring rightsTimesLimitKey = env->NewStringUTF("rightsTimesLimit");
    jstring jdv = env->NewStringUTF("");
    // 调用 optString 获取 title 和 taskId
    auto title = (jstring) env->CallObjectMethod(json, optSM, titleKey, jdv);
    auto taskId = (jstring) env->CallObjectMethod(json, optSM, taskIdKey, jdv);
    env->DeleteLocalRef(jdv);

    // 调用 optInt 获取 rightsTimes 和 rightsTimesLimit
    jint rightsTimes = env->CallIntMethod(json, optIM, rightsTimesKey, 0);
    jint rightsTimesLimit = env->CallIntMethod(json, optIM, rightsTimesLimitKey, 0);

    // 将 jstring 转换为 C 字符串
    const char *titleStr = env->GetStringUTFChars(title, nullptr);
    const char *taskIdStr = env->GetStringUTFChars(taskId, nullptr);

    // 打印日志
    LogI("抽抽乐:title=%s", titleStr);
    LogI("抽抽乐:taskId=%s", taskIdStr);
    LogI("抽抽乐:rightsTimes=%d", rightsTimes);
    LogI("抽抽乐:rightsTimesLimit=%d", rightsTimesLimit);

    // 计算循环次数
    int loopCount = rightsTimesLimit - rightsTimes;
    if (loopCount <= 0) {
        LogE("抽抽乐任务【%s】没有执行次数了", titleStr);
        logMessage("SO抽抽乐任务【%s】没有执行次数了", titleStr);
        env->ReleaseStringUTFChars(title, titleStr);
        env->ReleaseStringUTFChars(taskId, taskIdStr);
        env->DeleteLocalRef(appHookClass);
        env->DeleteLocalRef(jsonOBC);
        env->DeleteLocalRef(titleKey);
        env->DeleteLocalRef(taskIdKey);
        env->DeleteLocalRef(rightsTimesKey);
        env->DeleteLocalRef(rightsTimesLimitKey);
        return JNI_FALSE;
    }
    // 准备参数
    jstring method = env->NewStringUTF("com.alipay.antfarm.doFarmTask");
    // 构建 JSON 字符串
    char temp[256]; // 确保这个缓冲区足够大以容纳 JSON 字符串
    snprintf(temp, sizeof(temp), R"([{"bizKey":"%s","requestType":"RPC","sceneCode":"ANTFARM","source":"H5","taskSceneCode":"ANTFARM_DRAW_TIMES_TASK"}])", taskIdStr);

    // 创建 Java 字符串
    jstring requestData = env->NewStringUTF(temp);
    if (requestData == nullptr) {
        LogE("抽抽乐请求参数创建失败");
        env->ReleaseStringUTFChars(title, titleStr);
        env->ReleaseStringUTFChars(taskId, taskIdStr);
        env->DeleteLocalRef(appHookClass);
        env->DeleteLocalRef(jsonOBC);
        env->DeleteLocalRef(titleKey);
        env->DeleteLocalRef(taskIdKey);
        env->DeleteLocalRef(rightsTimesKey);
        env->DeleteLocalRef(rightsTimesLimitKey);
        env->DeleteLocalRef(method);
        env->DeleteLocalRef(requestData);
        return JNI_FALSE; // 创建字符串失败
    }
    int sucCount = 0;
    for (int i = (rightsTimes + 1); i < (rightsTimesLimit + 1); ++i) {
        // 调用 requestString 方法
        auto result = (jstring) env->CallStaticObjectMethod(appHookClass, requestStringMethod, method, requestData);
        if (result == nullptr) {
            continue;
        }
        const char *resultStr = env->GetStringUTFChars(result, nullptr);
        // 将 C 字符串转换为 Java 字符串
        jstring resultJsonString = env->NewStringUTF(resultStr);
        env->ReleaseStringUTFChars(result, resultStr);

        // 解析 JSON 字符串
        jclass jsonClass = env->FindClass("org/json/JSONObject");
        jmethodID jsonConstructor = env->GetMethodID(jsonClass, "<init>", "(Ljava/lang/String;)V");
        jobject jsonObject = env->NewObject(jsonClass, jsonConstructor, resultJsonString);

        jmethodID optBooleanMethod = env->GetMethodID(jsonClass, "optBoolean", "(Ljava/lang/String;Z)Z");
        jstring successKey = env->NewStringUTF("success");
        jboolean success = env->CallBooleanMethod(jsonObject, optBooleanMethod, successKey, JNI_FALSE);
        // 清理局部引用
        env->DeleteLocalRef(jsonClass);
        env->DeleteLocalRef(jsonObject);
        env->DeleteLocalRef(successKey);
        if (success) {
            LogI("抽抽乐任务【第%d次】🧾️[完成:抽抽乐->%s]", i, titleStr);
            logMessage("SO抽抽乐小鸡【第%d次】🧾️[完成:抽抽乐->%s]", i, titleStr);
            sucCount++;
        } else {
            logMessage("SO抽抽乐小鸡【第%d次】【%s】执行【失败】", i, titleStr);
        }
    }
    LogE("抽抽乐任务【%s】总次数[%d],成功次数[%d]", titleStr, loopCount, sucCount);
    // 释放资源
    env->ReleaseStringUTFChars(title, titleStr);
    env->ReleaseStringUTFChars(taskId, taskIdStr);
    env->DeleteLocalRef(appHookClass);
    env->DeleteLocalRef(jsonOBC);
    env->DeleteLocalRef(titleKey);
    env->DeleteLocalRef(taskIdKey);
    env->DeleteLocalRef(rightsTimesKey);
    env->DeleteLocalRef(rightsTimesLimitKey);
    env->DeleteLocalRef(method);
    env->DeleteLocalRef(requestData);
    return (sucCount == loopCount) ? JNI_TRUE : JNI_FALSE;
}
