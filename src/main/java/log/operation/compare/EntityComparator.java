package log.operation.compare;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.whatsegg.pelikan.log.ILogEntity;
import com.whatsegg.pelikan.log.operation.value.IgnoreCompareList;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对比两个实体差异
 * 暂不支持数组对象中再套一层数组的场景
 *
 * @author carlon
 */
public class EntityComparator implements IValueComparator {

    @Override
    public String compareDifference(ILogEntity obj, ILogEntity another) {
        List<ValueDifference> valueDifferences = compareLogEntity(obj, another, null, null);
        return JSON.toJSONString(valueDifferences, SerializerFeature.WriteMapNullValue);
    }

    /**
     * 对比日志记录实体
     *
     * @param obj     实体
     * @param another
     * @param key
     * @return
     */
    public List<ValueDifference> compareLogEntity(Object obj, Object another, String key, String parentKey) {
        if (CompareUtil.oneIsNull(obj, another)) {
            return Collections.singletonList(CompareUtil.wrapDiff(obj, another, key, null));
        }
        if (obj != null && another != null) {
            List<Field> allFields = CompareUtil.getAllFields(obj.getClass());
            List<ValueDifference> valueDifferences = allFields.stream()
                    .map(e -> compareObject(e, obj, another, CompareUtil.getDisplayKey(key, parentKey)))
                    .reduce(MergeListOperatorFactory.VALUE_DIFFERENCE_MERGE)
                    .orElse(null);
            return valueDifferences;
        }
        return new ArrayList<>();
    }

    /**
     * 对比字段 如果是简单对象直接对比值, 如果是列表则用列表对比方法, 如果是杂对象则递归
     *
     * @param field
     * @param obj
     * @param another
     * @param parentKey
     * @return
     */
    private List<ValueDifference> compareObject(Field field, Object obj, Object another, String parentKey) {
        field.setAccessible(true);
        Object fieldValue;
        Object anotherFieldValue;
        try {
            // Field.get(obj)   返回obj对象上该field的值
            fieldValue = field.get(obj);
            anotherFieldValue = field.get(another);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        if (simpleType(field)) {
            return compareSimpleType(field, fieldValue, anotherFieldValue, parentKey);
        }
        if (List.class.isAssignableFrom(field.getType())) {
            return compareList(field, fieldValue, anotherFieldValue, parentKey);
        }
        return compareLogEntity(fieldValue, anotherFieldValue, field.getName(), parentKey);
    }

    /**
     * 对比列表
     *
     * @param field
     * @param fieldValue
     * @param anotherFieldValue
     * @param parentKey
     * @return 例（包含删除记录、新增记录、修改记录）: [{"key":"order.orderDetailList","valuePair":{"postValue":null,"preValue":{"id":1,"price":null,"qty":2,"skuName":"test"}}},{"key":"order.orderDetailList","valuePair":{"postValue":{"id":3,"price":null,"qty":2,"skuName":"test3"},"preValue":null}},{"key":"order.orderDetailList","valuePair":{"postValue":{"skuName":"test222","id":2},"preValue":{"skuName":"test2","id":2}}}]
     */
    @SuppressWarnings("unchecked")
    private List<ValueDifference> compareList(Field field, Object fieldValue, Object anotherFieldValue,
                                              String parentKey) {
        List listValue = (List) fieldValue;
        List anotherListValue = (List) anotherFieldValue;
        List<ValueDifference> diffs = new ArrayList<>();
        // 一个为空 另一个不为空
        if (CompareUtil.oneCollectionIsNull(listValue, anotherListValue)) {
            diffs.add(CompareUtil.wrapDiff(fieldValue, anotherFieldValue, field.getName(), parentKey));
            return diffs;
        }

        if (!CollectionUtils.isEmpty(listValue) && !CollectionUtils.isEmpty(anotherListValue)) {

            // 1.判断List<Long> 数组中是简单对象的情况
            // 2.List<>中的对象没有唯一键，需要直接转json 放入diffs
            if (simpleListType(field) || ignoreListCompare(field)) {
                diffs.addAll(compareSimpleList(listValue, anotherListValue, field.getName(), parentKey));
            }
            // 比较List<Object>是复杂对象的情况
            else {
                // 找到id 转成map join对比是否存在
                Map<Object, Object> mappedValue = (Map<Object, Object>) listValue.stream()
                        .collect(Collectors.toMap(e -> ComparableIdFetcherRegistry.getInstance(e.getClass()).getIdValue(e),
                                i -> i, (a, b) -> a));
                Map<Object, Object> anotherMappedValue = (Map<Object, Object>) anotherListValue.stream()
                        .collect(Collectors.toMap(e -> ComparableIdFetcherRegistry.getInstance(e.getClass()).getIdValue(e),
                                i -> i, (a, b) -> a));

                // 被删除的记录 相当于left outer join
                List<ValueDifference> deletedDiff = mappedValue.entrySet().stream()
                        .filter(x -> anotherMappedValue.get(x.getKey()) == null)
                        .map(y -> CompareUtil.wrapDiff(y.getValue(), null, field.getName(), parentKey))
                        .collect(Collectors.toList());
                if (deletedDiff != null) {
                    diffs.addAll(deletedDiff);
                }
                // 新增的记录 相当于right outer join
                List<ValueDifference> addedDiff = anotherMappedValue.entrySet().stream()
                        .filter(x -> mappedValue.get(x.getKey()) == null)
                        .map(y -> CompareUtil.wrapDiff(null, y.getValue(), field.getName(), parentKey))
                        .collect(Collectors.toList());
                if (addedDiff != null) {
                    diffs.addAll(addedDiff);
                }

                // 修改的记录 对比每个元素的每个字段  相当于inner join
                List<ValueDifference> modifiedList = mappedValue.entrySet().stream()
                        .filter(x -> anotherMappedValue.get(x.getKey()) != null)
                        .map(Map.Entry::getKey)
                        .map(x -> compareEntityInList(mappedValue.get(x), anotherMappedValue.get(x),
                                CompareUtil.getDisplayKey(field.getName(), parentKey)))
                        .reduce(MergeListOperatorFactory.VALUE_DIFFERENCE_MERGE)
                        .orElse(null);
                if (modifiedList != null) {
                    diffs.addAll(modifiedList);
                }
            }
        }

        return diffs;
    }

    /**
     * 比较简单数组对象，将修改前后的值直接记录下来
     *
     * @param listValue
     * @param anotherListValue
     * @param key
     * @param parentKey
     * @return
     */
    private Collection<? extends ValueDifference> compareSimpleList(Collection listValue, Collection anotherListValue, String key, String parentKey) {
        Object objJson = JSON.toJSON(listValue);
        Object anotherJson = JSON.toJSON(anotherListValue);

        List<ValueDifference> diffs = new ArrayList<>();
        if (!objJson.equals(anotherJson)) {
            ValueDifference diff = CompareUtil.wrapDiff(objJson, anotherJson, key, parentKey);
            diffs.add(diff);
        }
        return diffs;
    }

    /**
     * 对比列表中的属性
     *
     * @param obj
     * @param another
     * @param parentKey
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<ValueDifference> compareEntityInList(Object obj, Object another, String parentKey) {
        List<ValueDifference> diffs = new ArrayList<>();

        // 考虑到比较前后有可能pre：List[] post：list[]
        List<Field> allFields = CompareUtil.getAllFields(obj.getClass());
        // 找出有差异的简单类型字段
        List<Field> diffFields = allFields.stream()
                .filter(e -> !entityInListFieldEquals(e, obj, another))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(diffFields)) {
            // 把id 和有差异的值放到Map
            IComparableIdFetcher fetcher = ComparableIdFetcherRegistry.getInstance(obj.getClass());

            Map<String, Object> diffMap = diffFields.stream()
                    .collect(Collectors.toMap(Field::getName,
                            e -> Optional.ofNullable(CompareUtil.getFieldValue(e, obj))));
            diffMap.put(fetcher.getIdFieldName(), fetcher.getIdValue(obj));

            Map<String, Object> anotherDiffMap = diffFields.stream()
                    .collect(Collectors.toMap(Field::getName,
                            e -> Optional.ofNullable(CompareUtil.getFieldValue(e, another))));
            anotherDiffMap.put(fetcher.getIdFieldName(), fetcher.getIdValue(another));

            diffs.add(CompareUtil.wrapDiff(diffMap, anotherDiffMap, parentKey, null));
        }

        // 将数组中的对象进行递归比较
        List<ValueDifference> entityInListDiff = allFields.stream().filter(
                e -> (!List.class.isAssignableFrom(e.getType()) && entityInListFieldEquals(e, obj, another)))
                .map(e -> compareObject(e, obj, another, parentKey))
                .reduce(MergeListOperatorFactory.VALUE_DIFFERENCE_MERGE)
                .orElse(null);
        if (!CollectionUtils.isEmpty(entityInListDiff)) {
            diffs.addAll(entityInListDiff);
        }

        // 将数组中的数组进行比较
        List<ValueDifference> listDiff = allFields.stream().filter(e -> List.class.isAssignableFrom(e.getType()))
                .map(e -> compareObject(e, obj, another, parentKey))
                .reduce(MergeListOperatorFactory.VALUE_DIFFERENCE_MERGE)
                .orElse(null);
        if (!CollectionUtils.isEmpty(listDiff)) {
            diffs.addAll(listDiff);
        }

        return diffs;
    }

    /**
     * 列表中对象的属性是否相等的判断
     *
     * @param field
     * @param obj
     * @param another
     * @return
     */
    private boolean entityInListFieldEquals(Field field, Object obj, Object another) {
        field.setAccessible(true);
        Object fieldValue;
        Object anotherFieldValue;
        try {
            fieldValue = field.get(obj);
            anotherFieldValue = field.get(another);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        if (fieldValue == null && anotherFieldValue == null) {
            return true;
        }
        if (CompareUtil.oneIsNull(fieldValue, anotherFieldValue)) {
            return false;
        }
        if (fieldValue != null && anotherFieldValue != null) {
            if (simpleType(field)) {
                return CompareUtil.primitiveEquals(field, fieldValue, anotherFieldValue);
            }
        }
        return true;
    }

    /**
     * 简单类型比较
     *
     * @param field
     * @param fieldValue
     * @param anotherFieldValue
     * @param parentKey
     * @return
     */
    private List<ValueDifference> compareSimpleType(Field field, Object fieldValue, Object anotherFieldValue,
                                                    String parentKey) {
        List<ValueDifference> diffs = new ArrayList<>();

        if (CompareUtil.oneIsNull(fieldValue, anotherFieldValue)) {
            return Collections.singletonList(CompareUtil
                    .wrapDiff(fieldValue, anotherFieldValue, field.getName(), parentKey));
        }
        if (fieldValue != null && anotherFieldValue != null) {
            if (!CompareUtil.primitiveEquals(field, fieldValue, anotherFieldValue)) {
                diffs.add(CompareUtil.wrapDiff(fieldValue, anotherFieldValue, field.getName(), parentKey));
            }
        }
        return diffs;
    }

    /**
     * 判断是否简单类型
     *
     * @param e
     * @return
     */
    private boolean simpleType(Field e) {
        return simpleType(e.getType());
    }

    /**
     * 判断该类是否为简单对象
     *
     * @param clazz
     * @return
     */
    private boolean simpleType(Class clazz) {
        return clazz.isPrimitive() || clazz.equals(String.class) || clazz.equals(Date.class)
                || Number.class.isAssignableFrom(clazz) || clazz.isEnum()
                || clazz.equals(Boolean.class);
    }

    /**
     * 判断该属性是否为List<T>简单对象 例：List<Long>
     *
     * @param field
     * @return
     */
    private boolean simpleListType(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            Class clazz = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            return simpleType(clazz);
        }
        return false;
    }

    /**
     * 判断该属性是否被 @IgnoreCompareList注解标注
     *
     * @param field
     * @return
     */
    private boolean ignoreListCompare(Field field) {
        return ignoreFieldByAnnotation(field, IgnoreCompareList.class);
    }

    /**
     * 判断是否被某注解标注
     *
     * @param f
     * @param annotationClass
     * @return
     */
    private boolean ignoreFieldByAnnotation(Field f, Class<?> annotationClass) {
        Annotation[] annotations = f.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotationClass.isAssignableFrom(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

}
