#include <jni.h>
#include <android/log.h>
#include <string>

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

//private static native String show(Context context);
extern "C" JNIEXPORT jstring JNICALL
Java_io_github_lazyimmortal_sesame_util_SesameValidator_show(JNIEnv *env, jclass cls, jobject context) {
    return env->NewStringUTF("芝麻开门:阿里巴巴");
}

//public static native boolean runBefore();
extern "C" JNIEXPORT jboolean JNICALL
Java_io_github_lazyimmortal_sesame_util_SesameValidator_runBefore(JNIEnv *env, jclass cls) {
    return JNI_TRUE;
}

//private native boolean isRun();
extern "C" JNIEXPORT jboolean JNICALL
Java_io_github_lazyimmortal_sesame_hook_ApplicationHook_isRun(JNIEnv *env, jobject obj) {
    return JNI_TRUE;
}

//private static native boolean initOver();
extern "C" JNIEXPORT jboolean JNICALL
Java_io_github_lazyimmortal_sesame_model_task_antForest_AntForestRpcCall_initOver(JNIEnv *env, jclass cls) {
    return JNI_TRUE;
}

//private native void checkInnerAction(String str);
extern "C" JNIEXPORT void JNICALL
Java_io_github_lazyimmortal_sesame_model_task_antFarm_AntFarm_checkInnerAction(JNIEnv *env, jobject obj, jstring str) {
    const char *cStr = env->GetStringUTFChars(str, NULL);
    if (cStr == NULL) {
        return;
    }
    __android_log_print(ANDROID_LOG_INFO, "懒真人", "InnerAction: %s", cStr);
    env->ReleaseStringUTFChars(str, cStr);
}

//private static native String getBizKeyByChouChouLe(String str);
extern "C" JNIEXPORT jstring JNICALL
Java_io_github_lazyimmortal_sesame_model_task_antFarm_AntFarmRpcCall_getBizKeyByChouChouLe(JNIEnv *env, jclass cls, jstring str) {
    return str;
}

//private static native String getBizKeyByFarm(String str);
extern "C" JNIEXPORT jstring JNICALL
Java_io_github_lazyimmortal_sesame_model_task_antFarm_AntFarmRpcCall_getBizKeyByFarm(JNIEnv *env, jclass cls, jstring str) {
    return str;
}
