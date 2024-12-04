package fansirsqi.xposed.sesame.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import fansirsqi.xposed.sesame.R;
import fansirsqi.xposed.sesame.util.FileUtil;
import fansirsqi.xposed.sesame.util.LanguageUtil;
import fansirsqi.xposed.sesame.util.LogUtil;
import fansirsqi.xposed.sesame.util.ToastUtil;

public class HtmlViewerActivity extends BaseActivity {
  private static final String TAG = HtmlViewerActivity.class.getSimpleName();

  MyWebView mWebView;
  ProgressBar progressBar;
  private Uri uri;
  private Boolean canClear;

  @SuppressLint("ObsoleteSdkInt")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LanguageUtil.setLocale(this);
    setContentView(R.layout.activity_html_viewer);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
    setBaseSubtitleTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
    }else{
    setBaseSubtitleTextColor(getResources().getColor(R.color.textColorPrimary));
    }
    // 设置标题栏
    // 初始化 WebView 和进度条
    mWebView = findViewById(R.id.mwv_webview);
    progressBar = findViewById(R.id.pgb_webview);

    // 设置 WebView 的客户端
    setupWebView();
  }

  /** 设置 WebView 的 WebChromeClient 和进度变化监听 */
  private void setupWebView() {
    mWebView.setWebChromeClient(
        new WebChromeClient() {
          @SuppressLint("WrongConstant")
          @Override
          public void onProgressChanged(WebView view, int progress) {
            progressBar.setProgress(progress);
            if (progress < 100) {
              setBaseSubtitle("Loading...");
              progressBar.setVisibility(View.VISIBLE);
            } else {
              setBaseSubtitle(mWebView.getTitle());
              progressBar.setVisibility(View.GONE);
            }
          }
        });
  }

  @Override
  protected void onResume() {
    super.onResume();
    // 获取传递过来的 Intent
    Intent intent = getIntent();
    WebSettings settings = mWebView.getSettings();

    settings.setSupportZoom(true); // 支持缩放
    settings.setBuiltInZoomControls(true); // 启用内置缩放机制
    settings.setDisplayZoomControls(false); // 不显示缩放控件

    // 启用触摸缩放
    mWebView.getSettings().setUseWideViewPort(true);
    mWebView.getSettings().setLoadWithOverviewMode(true);
    mWebView.getSettings().setSupportZoom(true);
    mWebView.getSettings().setBuiltInZoomControls(true);
    mWebView.getSettings().setDisplayZoomControls(false);

    // 根据 intent 设置 WebView
    if (intent != null) {
      configureWebViewSettings(intent, settings);
      uri = intent.getData();
      if (uri != null) {
        mWebView.loadUrl(uri.toString());
      }
      canClear = intent.getBooleanExtra("canClear", false);
      return;
    }
    // 设置默认的 WebView 参数
    settings.setTextZoom(100);
    settings.setUseWideViewPort(true);
  }

  /**
   * 配置 WebView 的设置项
   *
   * @param intent 传递的 Intent
   * @param settings WebView 的设置
   */
  private void configureWebViewSettings(Intent intent, WebSettings settings) {
    if (intent.getBooleanExtra("nextLine", true)) {
      settings.setTextZoom(85);
      settings.setUseWideViewPort(false);
    } else {
      settings.setTextZoom(100);
      settings.setUseWideViewPort(true);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // 创建菜单选项
    menu.add(0, 1, 1, getString(R.string.export_file));
    if (canClear) {
      menu.add(0, 2, 2, getString(R.string.clear_file));
    }
    menu.add(0, 3, 3, getString(R.string.open_with_other_browser));
    menu.add(0, 4, 4, getString(R.string.copy_the_url));
    menu.add(0, 5, 5, getString(R.string.scroll_to_top));
    menu.add(0, 6, 6, getString(R.string.scroll_to_bottom));
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case 1:
        // 导出文件
        exportFile();
        break;
      case 2:
        // 清空文件
        clearFile();
        break;
      case 3:
        // 使用其他浏览器打开
        openWithBrowser();
        break;
      case 4:
        // 复制 URL 到剪贴板
        copyUrlToClipboard();
        break;
      case 5:
        // 滚动到顶部
        mWebView.scrollTo(0, 0);
        break;
      case 6:
        // 滚动到底部
        mWebView.scrollToBottom();
        break;
    }
    return true;
  }

  /** 导出当前文件 */
  private void exportFile() {
    try {
      if (uri != null) {
        String path = uri.getPath();
        LogUtil.runtime(TAG, "URI path: " + path);
        if (path != null) {
          File exportFile = FileUtil.exportFile(new File(path));
          if (exportFile != null && exportFile.exists()) {
            ToastUtil.showToast(this, getString(R.string.file_exported) + exportFile.getPath());
          } else {
            LogUtil.runtime(TAG, "导出失败，exportFile 对象为 null 或不存在！");
          }
        } else {
          LogUtil.runtime(TAG, "路径为 null！");
        }
      } else {
        LogUtil.runtime(TAG, "URI 为 null！");
      }
    } catch (Exception e) {
      LogUtil.printStackTrace(TAG, e);
    }
  }

  /** 清空当前文件 */
  private void clearFile() {
    try {
      if (uri != null) {
        String path = uri.getPath();
        if (path != null) {
          File file = new File(path);
          if (FileUtil.clearFile(file)) {
            ToastUtil.makeText(this, "文件已清空", Toast.LENGTH_SHORT).show();
            mWebView.reload();
          }
        }
      }
    } catch (Exception e) {
      LogUtil.printStackTrace(TAG, e);
    }
  }

  /** 使用其他浏览器打开当前 URL */
  private void openWithBrowser() {
    if (uri != null) {
      String scheme = uri.getScheme();
      if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
      } else if ("file".equalsIgnoreCase(scheme)) {
        ToastUtil.makeText(this, "该文件不支持用浏览器打开", Toast.LENGTH_SHORT).show();
      } else {
        ToastUtil.makeText(this, "不支持用浏览器打开", Toast.LENGTH_SHORT).show();
      }
    }
  }

  /** 复制当前 WebView 的 URL 到剪贴板 */
  private void copyUrlToClipboard() {
    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    if (clipboardManager != null) {
      clipboardManager.setPrimaryClip(ClipData.newPlainText(null, mWebView.getUrl()));
      ToastUtil.makeText(this, getString(R.string.copy_success), Toast.LENGTH_SHORT).show();
    }
  }
}