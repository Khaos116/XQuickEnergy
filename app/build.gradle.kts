import java.text.SimpleDateFormat
import java.util.*

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.rikka.tools.refine)
}
val mVersionName = "1.0.0"
android {
  namespace = "fansirsqi.xposed.sesame"
  compileSdk = 36
  defaultConfig {
    vectorDrawables.useSupportLibrary = true
    applicationId = "fansirsqi.xposed.sesame"
    minSdk = 26
    targetSdk = 36
    val buildDate = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).apply {
      timeZone = TimeZone.getTimeZone("GMT+8")
    }.format(Date())
    val buildTime = SimpleDateFormat("HH:mm:ss", Locale.CHINA).apply {
      timeZone = TimeZone.getTimeZone("GMT+8")
    }.format(Date())
    versionCode = "${buildDate.replace("-", "").takeLast(6)}${buildTime.replace(":", "").take(3)}".toInt()
    versionName = mVersionName
    buildConfigField("String", "BUILD_DATE", "\"$buildDate\"")
    buildConfigField("String", "BUILD_TIME", "\"$buildTime\"")
    ndk { abiFilters.addAll(setOf("arm64-v8a")) }
    testOptions {
      unitTests.all {
        it.enabled = false
      }
    }
  }

  buildFeatures {
    viewBinding = true
    buildConfig = true
    compose = true
    aidl = true
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = false //关闭脱糖
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlin {
    compilerOptions {
      jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
  }

  signingConfigs {
    register("release") {
      storeFile = File("${rootDir}/xqe.jks")
      storePassword = "xqe123456"
      keyAlias = "xqe"
      keyPassword = "xqe123456"
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
      enableV4Signing = true
    }
  }

  buildTypes {
    getByName("debug") {
      isDebuggable = true
      versionNameSuffix = "-debug"
      isShrinkResources = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.findByName("release")
    }
    getByName("release") {
      isDebuggable = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.findByName("release")
    }
  }

  sourceSets {
    getByName("main") {
      jniLibs.srcDirs("src/main/jniLibs")
    }
  }

  packaging {
    jniLibs {
      // 排除所有架构下的所有 .so 文件
      excludes.add("lib/**/*.so")
      //excludes.add("lib/x86/**/*.so")
      //excludes.add("lib/x86_64/**/*.so")
    }
  }
}

//修改后的打包处理逻辑
android.applicationVariants.all {
  val variant = this
  val buildTypeName = variant.buildType.name.replaceFirstChar { it.uppercase() }
  variant.assembleProvider.configure {
    doLast {
      // 通过 variant.outputs 找到原生的生成文件
      variant.outputs.forEach { output ->
        val originFile = output.outputFile
        val destDir = File(rootDir, "APK/$buildTypeName")
        val buildDate = SimpleDateFormat("yyyyMMdd", Locale.CHINA).apply {
          timeZone = TimeZone.getTimeZone("GMT+8")
        }.format(Date())
        val buildTime = SimpleDateFormat("HHmm", Locale.CHINA).apply {
          timeZone = TimeZone.getTimeZone("GMT+8")
        }.format(Date())
        val targetFileName = "XQE_TK_${buildTypeName}_${variant.versionName}_${buildDate}_${buildTime}.apk"
        if (originFile.exists()) {
          if (buildTypeName == "Debug") {
            destDir.deleteRecursively()
          }
          destDir.mkdirs()
          // 使用 copyTo 并覆盖
          originFile.copyTo(File(destDir, targetFileName), overwrite = true)
        }
      }
    }
  }
}

dependencies {
  // Shizuku 相关依赖 - 用于获取系统级权限
  implementation(libs.rikka.shizuku.api)        // Shizuku API
  implementation(libs.rikka.shizuku.provider)   // Shizuku 提供者
  implementation(libs.rikka.refine)             // Rikka 反射工具
  //    implementation(libs.rikka.hidden.stub)
  // implementation(libs.ui.tooling.preview.android)
  implementation(libs.cmd.android)
  implementation(libs.androidx.ui.text.google.fonts)
  implementation(libs.material3) // 用于通过 Shizuku 执行命令

  // Compose 相关依赖 - 现代化 UI 框架
  val composeBom = platform("androidx.compose:compose-bom:2025.12.00")  // Compose BOM 版本管理
  implementation(composeBom)

  testImplementation(composeBom)
  androidTestImplementation(composeBom)
  implementation(libs.androidx.material3)                // Material 3 设计组件
  implementation(libs.androidx.ui.tooling.preview)              // UI 工具预览
  debugImplementation(libs.androidx.ui.tooling)                 // 调试时的 UI 工具
  implementation(libs.androidx.material.icons.extended)         // Material 3 图标

  // 生命周期和数据绑定
  implementation(libs.androidx.lifecycle.viewmodel.compose) // Compose ViewModel 支持

  // JSON 序列化
  implementation(libs.kotlinx.serialization.json) // Kotlin JSON 序列化库

  // Kotlin 协程依赖 - 异步编程（纯协程调度）
  implementation(libs.kotlinx.coroutines.core)     // 协程核心库
  implementation(libs.kotlinx.coroutines.android)  // Android 协程支持

  // 数据观察和 HTTP 服务
  implementation(libs.androidx.lifecycle.livedata.ktx)  // LiveData KTX 扩展
  implementation(libs.androidx.runtime.livedata)        // Compose LiveData 运行时
  implementation(libs.nanohttpd)                   // 轻量级 HTTP 服务器

  // UI 布局和组件
  implementation(libs.androidx.constraintlayout)  // 约束布局

  implementation(libs.activity.compose)           // Compose Activity 支持

  // Android 核心库
  implementation(libs.core.ktx)                   // Android KTX 核心扩展
  implementation(libs.kotlin.stdlib)              // Kotlin 标准库
  implementation(libs.slf4j.api)                  // SLF4J 日志 API
  implementation(libs.logback.android)            // Logback Android 日志实现
  implementation(libs.appcompat)                  // AppCompat 兼容库
  implementation(libs.recyclerview)               // RecyclerView 列表组件
  implementation(libs.viewpager2)                 // ViewPager2 页面滑动
  implementation(libs.material)                   // Material Design 组件
  implementation(libs.webkit)                     // WebView 组件

  // 仅编译时依赖 - Xposed 相关
  compileOnly(files("libs/api-82.jar"))          // Xposed API 82
  compileOnly(files("libs/api-100.aar"))         // Xposed API 100 https://github.com/libxposed/api
  implementation(files("libs/interface-100.aar")) // Xposed 模块接口 https://github.com/libxposed/api
  implementation(files("libs/service-100-1.0.0.aar"))  // https://github.com/libxposed/service

  // 代码生成和工具库
  compileOnly(libs.lombok)                       // Lombok 注解处理器（编译时）
  annotationProcessor(libs.lombok)               // Lombok 注解处理
  implementation(libs.okhttp)                    // OkHttp 网络请求库
  implementation(libs.dexkit)                    // DEX 文件分析工具
  implementation(libs.jackson.kotlin)            // Jackson Kotlin 支持

  // 核心库脱糖和系统 API 访问
  //    coreLibraryDesugaring(libs.desugar)            // Java 8+ API 脱糖支持

  implementation(libs.hiddenapibypass)           // 隐藏 API 访问绕过

  // Jackson JSON 处理库
  implementation(libs.jackson.core)
  implementation(libs.jackson.databind)
  implementation(libs.jackson.annotations)
}