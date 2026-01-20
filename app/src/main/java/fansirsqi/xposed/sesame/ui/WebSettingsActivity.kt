package fansirsqi.xposed.sesame.ui

import android.content.Intent
import android.os.Bundle
import fansirsqi.xposed.sesame.util.MyUtils

class WebSettingsActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    MyUtils.CHANGE_KT5.trim()
    val intent = Intent(this, SettingActivity::class.java)
    intent.putExtra("userId", intent.getStringExtra("userId"))
    intent.putExtra("userName", intent.getStringExtra("userName"))
    finish()
    startActivity(intent)
  }
}
