package fansirsqi.xposed.sesame.ui.extension

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import fansirsqi.xposed.sesame.BuildConfig
import fansirsqi.xposed.sesame.entity.UserEntity
import fansirsqi.xposed.sesame.ui.SettingActivity
import fansirsqi.xposed.sesame.ui.WebSettingsActivity
import fansirsqi.xposed.sesame.ui.model.UiMode
import fansirsqi.xposed.sesame.ui.repository.ConfigRepository
import fansirsqi.xposed.sesame.util.Detector
import fansirsqi.xposed.sesame.util.Log
import fansirsqi.xposed.sesame.util.MyUtils
import fansirsqi.xposed.sesame.util.ToastUtil

/**
 * 扩展函数：打开浏览器
 */

fun Context.openUrl(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(this, "未找到可用的浏览器", Toast.LENGTH_SHORT).show()
    }
}

/**
 * 扩展函数：带密码验证的执行器
 */
fun Context.executeWithVerification(action: () -> Unit) {
    if (BuildConfig.DEBUG) {
        action()
    } else {
        MyUtils.CHANGE_KT15.trim()
        //showPasswordDialog(action)
        action()
    }
}

/**
 * 扩展函数：显示密码对话框
 */
@SuppressLint("SetTextI18n")
private fun Context.showPasswordDialog(onSuccess: () -> Unit) {
    val container = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(50, 30, 50, 10)
    }

    val label = TextView(this).apply {
        text = "非必要情况无需查看异常日志\n（有困难联系闲鱼卖家帮你处理）"
        textSize = 16f
        setTextColor(Color.DKGRAY)
        setPadding(0, 0, 0, 20)
        textAlignment = View.TEXT_ALIGNMENT_CENTER
    }

    val editText = EditText(this).apply {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        hint = "请输入密码"
        setTextColor(Color.BLACK)
        setHintTextColor(Color.GRAY)
        setPadding(40, 30, 40, 30)
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        background = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadii = floatArrayOf(60f, 60f, 60f, 60f, 60f, 60f, 60f, 60f)
            setStroke(3, Color.LTGRAY)
        }
    }

    container.addView(label)
    container.addView(editText)

    val dialog = AlertDialog.Builder(this)
        .setTitle("🔐 防呆验证")
        .setView(container)
        .setPositiveButton("确定", null)
        .setNegativeButton("取消") { d, _ -> d.dismiss() }
        .create()

    dialog.setOnShowListener {
        dialog.window?.setBackgroundDrawable(GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 60f
        })

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
            setTextColor("#3F51B5".toColorInt())
            setOnClickListener {
                if (editText.text.toString() == "Sesame-TK") {
                    ToastUtil.showToast(context, "验证成功😊")
                    onSuccess()
                    dialog.dismiss()
                } else {
                    ToastUtil.showToast(context, "密码错误😡")
                    editText.text.clear()
                }
            }
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.DKGRAY)
    }
    dialog.show()
}

fun joinQQGroup(context: Context) {
    val intent = Intent()
//    intent.data = Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D$key")
    // 或者使用更通用的协议：
    intent.data = Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&card_type=group&uin=1002616652")
//    intent.data = Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=1002616652&card_type=group&source=qrcode")

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // 如果没安装 QQ 或唤起失败，回退到打开网页
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, "https://qm.qq.com/q/Aj0Xby6AGQ".toUri()) // 这里的 URL 结构可能需要根据实际生成的链接调整
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }
}


fun Context.performNavigationToSettings(user: UserEntity) {
    if (Detector.loadLibrary("checker")) {
        Log.record("载入用户配置 ${user.showName}")
        try {
            // 1. 【改动点】从仓库获取当前模式
            val currentMode = ConfigRepository.uiMode.value
            // 2. 【改动点】获取对应的 Activity 类 (使用上面定义的扩展属性)
            val targetActivity = currentMode.targetActivity

            val intent = Intent(this, targetActivity).apply {
                putExtra("userId", user.userId)
                putExtra("userName", user.showName)
            }
            startActivity(intent)
        } catch (e: Exception) {
            ToastUtil.showToast(this, "无法启动设置页面: ${e.message}")
        }
    } else {
        Detector.tips(this, "缺少必要依赖！")
    }
}

val UiMode.targetActivity: Class<*>
    get() = when (this) {
        UiMode.Web -> WebSettingsActivity::class.java
        UiMode.New -> SettingActivity::class.java
    }

