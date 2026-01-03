package io.github.lazyimmortal.sesame.util.idMap

/**
 * Author:khaos116
 * Date:2025/12/24
 * Time:10:54
 */
object UserMap {
  val currentUid: String?
    get() {
      return UserIdMap.getMyUid()
    }

  fun getUserIdSet(): Set<String> = UserIdMap.getUserIdSet()

  fun getMaskName(uid: String?): String? = UserIdMap.getMaskName(uid ?: "") ?: ""
}