package tkaxv7s.xposed.sesame.data.modelFieldExt;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tkaxv7s.xposed.sesame.R;
import tkaxv7s.xposed.sesame.data.ModelField;

public class EmptyModelField extends ModelField {

    private final Runnable clickRunner;

    public EmptyModelField(String code, String name) {
        super(code, name, null);
        this.clickRunner = null;
    }

    public EmptyModelField(String code, String name, Runnable clickRunner) {
        super(code, name, null);
        this.clickRunner = clickRunner;
    }

    @Override
    public void setValue(Object value) {
    }

    @Override
    public Object getValue() {
        return null;
    }

    @JsonIgnore
    public View getView(Context context) {
        Button btn = new Button(context);
        btn.setText(getName());
        btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setTextColor(Color.parseColor("#008175"));
        btn.setBackground(context.getResources().getDrawable(R.drawable.button));
        btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        btn.setMinHeight(150);
        btn.setMaxHeight(180);
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        if (clickRunner != null) {
            btn.setOnClickListener(v -> new AlertDialog.Builder(context)
                    .setTitle("警告")
                    .setMessage("确认执行该操作？")
                    .setPositiveButton(R.string.ok, (dialog, id) -> clickRunner.run())
                    .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                    .create()
                    .show());
        } else {
            btn.setOnClickListener(v -> Toast.makeText(context, "无配置项", Toast.LENGTH_SHORT).show());
        }
        return btn;
    }

}
