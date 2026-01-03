package io.github.lazyimmortal.sesame.hook

/**
 * Author:XX
 * Date:2026/1/3
 * Time:23:56
 */
class RequestManager {
  @JvmStatic
  fun requestString(method: String?, data: String?): String {
    return ApplicationHook.requestString(method ?: "", data ?: "") ?: "{}"
  }
}