package tkaxv7s.xposed.sesame.data.modelFieldExt;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.util.JsonUtil;
import tkaxv7s.xposed.sesame.util.MyChangeUtils;

public class BooleanModelField extends ModelField<Boolean> {

    public BooleanModelField(String code, String name, Boolean value) {
        super(code, name, value);
        Boolean newValue = MyChangeUtils.getDefaultBooleanModelField(code);
        if (newValue != null) {
          this.defaultValue = newValue;
          this.value = newValue;
        }
    }

    @Override
    public String getType() {
        return "BOOLEAN";
    }

    @Override
    public View getView(Context context) {
        Switch sw = new Switch(context);
        sw.setText(getName());
        sw.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        sw.setMinHeight(150);
        sw.setMaxHeight(180);
        sw.setPaddingRelative(40, 0, 40, 0);
        sw.setChecked(getValue());
        sw.setOnClickListener(v -> setObjectValue(((Switch) v).isChecked()));
        return sw;
    }

}
