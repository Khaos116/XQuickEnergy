package pansong291.xposed.quickenergy.data;

import android.content.Context;
import android.view.View;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

import lombok.Data;

@Data
public class ModelField implements Serializable {

    private static final String extPackage = "pansong291.xposed.quickenergy.data.modelFieldExt.";

    private String type;

    @JsonIgnore
    private String code;

    @JsonIgnore
    private String name;

    protected volatile Object value;

    public ModelField() {
        this.type = this.getClass().getName().replace(extPackage, "");
    }

    public ModelField(String code, String name, Object value) {
        this();
        this.code = code;
        this.name = name;
        setValue(value);
    }

    @JsonIgnore
    public Object getConfigValue() {
        return getValue();
    }

    public void setConfigValue(Object matchValue) {
        setValue(matchValue);
    }

    public View getView(Context context) {
        return null;
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public Class<? extends ModelField> getClazz() {
        try {
            return (Class<? extends ModelField>) Class.forName(extPackage + type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ModelField clone() {
        try {
            ModelField modelField = getClazz().newInstance();
            modelField.setCode(code);
            modelField.setName(name);
            modelField.setValue(value);
            return modelField;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
