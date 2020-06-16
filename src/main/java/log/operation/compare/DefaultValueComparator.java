package log.operation.compare;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import log.ILogEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 默认对象比较器 目前没有使用该比较器  ps EntityComparator为默认比较器
 *
 * @author carlon
 */
public class DefaultValueComparator implements IValueComparator {

    @Override
    public String compareDifference(ILogEntity obj, ILogEntity another) {
        // 转 json 对比
        return compareLogEntity(obj, another, null);
    }

    @SuppressWarnings("unchecked")
    public String compareLogEntity(ILogEntity obj, ILogEntity another, String key) {
        if (obj == null || another == null) {
            // 如果是新增或者删除
            ValueDifference valueDifference = new ValueDifference();
            ValuePair pair = new ValuePair(obj, another);
            valueDifference.setKey("entity");
            valueDifference.setValuePair(pair);
            return JSON.toJSONString(Arrays.asList(valueDifference), SerializerFeature.WriteMapNullValue);
        }
        JSONObject objJson = (JSONObject) JSON.toJSON(obj);
        JSONObject anotherJson = (JSONObject) JSON.toJSON(another);

        Set<String> keys = objJson.keySet();
        List<ValueDifference> diffs = keys.stream().map(e -> compareObject(objJson.get(e), anotherJson.get(e), e, null))
                .filter(i -> i != null && i.size() > 0)
                .reduce(MergeListOperatorFactory.VALUE_DIFFERENCE_MERGE)
                .orElse(null);
        return JSON.toJSONString(diffs, SerializerFeature.WriteMapNullValue);
    }

    public List<ValueDifference> compareObject(Object json, Object anotherJson, String key, String parentKey) {
        List<ValueDifference> diffs = null;
        if (json == null && anotherJson == null) {
            return diffs;
        }
        if (CompareUtil.oneIsNull(json, anotherJson)) {
            ValueDifference diff = CompareUtil.wrapDiff(json, anotherJson, key, parentKey);
            diffs = new ArrayList<>();
            diffs.add(diff);
            return diffs;
        }
        // 都不为null的情况
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            JSONObject anotherJsonObject = (JSONObject) anotherJson;

            Set<String> keys = jsonObject.keySet();
            String displayKey = CompareUtil.getDisplayKey(key, parentKey);

            diffs = keys.stream().map(e -> compareObject(jsonObject.get(e), anotherJsonObject.get(e), e, displayKey))
                    .filter(i -> i != null && i.size() > 0)
                    .reduce(MergeListOperatorFactory.VALUE_DIFFERENCE_MERGE)
                    .orElse(null);
        } else if (json instanceof JSONArray) {
            diffs = compareJsonArray((JSONArray) json, (JSONArray) anotherJson, key, parentKey);
        } else if (json instanceof String) {
            ValueDifference diff = compareString((String) json, (String) anotherJson, key, parentKey);
            if (diff != null) {
                diffs = new ArrayList<>();
                diffs.add(diff);
            }
        } else if (json instanceof Number) {
            ValueDifference diff = compareNumber((Number) json, (Number) anotherJson, key, parentKey);
            if (diff != null) {
                diffs = new ArrayList<>();
                diffs.add(diff);
            }
        }
        return diffs;
    }

    /**
     * 关键方法，用来记录不同属性值
     *
     * @param str1
     * @param str2
     * @param key
     */
    public ValueDifference compareString(String str1, String str2, String key, String parentKey) {
        if (!str1.equals(str2)) {
            ValueDifference diff = CompareUtil.wrapDiff(str1, str2, key, parentKey);
            return diff;
        } else {
            return null;
        }
    }

    public ValueDifference compareNumber(Number str1, Number str2, String key, String parentKey) {
        if (!str1.equals(str2)) {
            ValueDifference diff = CompareUtil.wrapDiff(str1, str2, key, parentKey);
            return diff;
        } else {
            return null;
        }
    }


    public List<ValueDifference> compareJsonArray(JSONArray json1, JSONArray json2, String key, String parentKey) {
        List<ValueDifference> diffs = new ArrayList<>();
        ValueDifference diff = CompareUtil.wrapDiff(json1, json2, key, parentKey);
        diffs.add(diff);
        return diffs;
    }

}
