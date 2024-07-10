package tkaxv7s.xposed.sesame.data.modelFieldExt;

import com.fasterxml.jackson.core.type.TypeReference;
import tkaxv7s.xposed.sesame.data.ModelField;
import tkaxv7s.xposed.sesame.util.JsonUtil;

import java.util.Map;

public class MapModelField extends ModelField {

    private static final TypeReference<Map<String, Integer>> typeReference = new TypeReference<Map<String, Integer>>() {
    };

    public MapModelField(String code, String name, Map<String, Integer> value) {
        super(code, name, value);
    }

    @Override
    public String getType() {
        return "MAP";
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            value = defaultValue;
        }
        this.value = JsonUtil.parseObject(value, typeReference);
    }

    @Override
    public Map<String, Integer> getValue() {
        return (Map<String, Integer>) value;
    }

}
