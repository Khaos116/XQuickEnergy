package pansong291.xposed.quickenergy.data.modelFieldExt;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

import pansong291.xposed.quickenergy.R;
import pansong291.xposed.quickenergy.data.ModelField;
import pansong291.xposed.quickenergy.ui.StringDialog;
import pansong291.xposed.quickenergy.util.JsonUtil;

public class ListModelField extends ModelField {

    private static final TypeReference<List<String>> typeReference = new TypeReference<List<String>>() {
    };

    public ListModelField() {
    }

    public ListModelField(Object value) {
        super(value);
    }

    public ListModelField(Object value, Object defaultValue) {
        super(value, defaultValue);
    }

    public ListModelField(String code, String name, List<String> value) {
        super(code, name, value);
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            value = defaultValue;
        }
        this.value = JsonUtil.parseObject(value, typeReference);
    }

    @Override
    public List<String> getValue() {
        return (List<String>) value;
    }

    @Override
    public View getView(Context context) {
        Button btn = new Button(context);
        btn.setText(getName());
        btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btn.setTextColor(Color.parseColor("#008175"));
        btn.setBackground(context.getResources().getDrawable(R.drawable.button));
        btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        btn.setMinHeight(150);
        btn.setPaddingRelative(40, 0, 40, 0);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> StringDialog.showEditDialog(v.getContext(), ((Button) v).getText(), this));
        return btn;
    }

    public static class ListJoinCommaToStringModelField extends ListModelField {

        public ListJoinCommaToStringModelField() {
        }

        public ListJoinCommaToStringModelField(List<String> value) {
            super(value);
        }

        public ListJoinCommaToStringModelField(List<String> value, List<String> defaultValue) {
            super(value, defaultValue);
        }

        public ListJoinCommaToStringModelField(String code, String name, List<String> value) {
            super(code, name, value);
        }

        @Override
        public void setConfigValue(String value) {
            if (value == null) {
                setValue(null);
                return;
            }
            List<String> list = new ArrayList<>();
            for (String str : value.split(",")) {
                if (!str.isEmpty()) {
                    list.add(str);
                }
            }
            setValue(list);
        }

        @Override
        public String getConfigValue() {
            return String.join(",", getValue());
        }
    }
}
