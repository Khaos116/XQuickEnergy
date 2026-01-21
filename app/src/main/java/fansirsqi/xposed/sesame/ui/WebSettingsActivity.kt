package fansirsqi.xposed.sesame.ui

import android.content.Intent
import android.os.Bundle
import fansirsqi.xposed.sesame.util.MyUtils
import fansirsqi.xposed.sesame.util.ToastUtil

class WebSettingsActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    MyUtils.CHANGE_KT5.trim()
    val userId = intent?.getStringExtra("userId")
    val userName = intent?.getStringExtra("userName")
    if (userId.isNullOrBlank()) {
      ToastUtil.showToast("用户ID不能为空")
      finish()
      return
    }
    val intentNew = Intent(this, SettingActivity::class.java)
    intentNew.putExtra("userId", userId)
    intentNew.putExtra("userName", userName)
    finish()
    startActivity(intentNew)
  }
}
