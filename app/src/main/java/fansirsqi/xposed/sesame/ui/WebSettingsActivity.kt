package fansirsqi.xposed.sesame.ui;

import android.content.Intent;
import android.os.Bundle;

public class WebSettingsActivity extends BaseActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = new Intent(this, WebSettingsActivity.class);
    intent.putExtra("userId", intent.getStringExtra("userId"));
    intent.putExtra("userName", intent.getStringExtra("userName"));
    finish();
    startActivity(intent);
  }
}
