package log.operation.compare;

import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 比较工具
 *
 * @author carlon
 */
public class CompareUtil {

    /**
     * 封装差异
     *
     * @param value        修改前的值
     * @param anotherValue 修改后的值
     * @param key          当前比较的属性名
     * @param parentKey    上级属性名
     * @return 直接返回修改前后的值
     */
    public static ValueDifference wrapDiff(Object value, Object anotherValue, String key, String parentKey) {
        ValuePair pair = new ValuePair(value, anotherValue);
        ValueDifference diff = new ValueDifference();
        diff.setKey(getDisplayKey(key, parentKey));
        diff.setValuePair(pair);
        return diff;
    }

    /**
     * 显示key拼接
     *
     * @param key       子key
     * @param parentKey 上级key
     * @return 返回拼好的字符串 例：sale.id
     */
    public static String getDisplayKey(String key, String parentKey) {
        return parentKey != null ? parentKey + "." + key : key;
    }

    /**
     * 是否简单类型
     *
     * @param field             属性对象
     * @param fieldValue        该属性值
     * @param anotherFieldValue 比较的另一个属性的值
     * @return
     */
    public static boolean primitiveEquals(Field field, Object fieldValue, Object anotherFieldValue) {
        if (BigDecimal.class.equals(field.getClass())) {
            BigDecimal decimal = (BigDecimal) fieldValue;
            BigDecimal anotherDecimal = (BigDecimal) anotherFieldValue;
            return decimal.compareTo(anotherDecimal) == 0;
        }
        return fieldValue.equals(anotherFieldValue);
    }

    /**
     * 获取所有filed 包含父类
     *
     * @param clazz 目标类
     * @return 返回所有属性（包括父类）
     */
    public static List<Field> getAllFields(Class clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<Field> allFields = new ArrayList<>(Arrays.asList(fields));
        // 如果在父类也找出来
        while (!Object.class.equals(clazz)) {
            clazz = clazz.getSuperclass();
            allFields.addAll(getAllFields(clazz));
        }
        return allFields;
    }

    /**
     * 获取id的值
     *
     * @param obj         目标实体
     * @param idFieldName 指定实体唯一键的字段名
     * @return
     */
    public static Object getIdValue(Object obj, String idFieldName) {
        Class itemClazz = obj.getClass();
        List<Field> fields = getAllFields(itemClazz);

        return fields.stream()
                .filter(e -> e.getName().equals(idFieldName))
                .map(e -> getFieldValue(e, obj))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取属性值
     *
     * @param field
     * @param object
     * @return
     */
    public static Object getFieldValue(Field field, Object object) {
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 其中一个是null 另外一个不是null
     *
     * @param object
     * @param another
     * @return
     */
    public static boolean oneIsNull(Object object, Object another) {
        return (object == null && another != null) || (object != null && another == null);
    }

    /**
     * 其中一个是空集合 另外一个不是空集合
     *
     * @param col
     * @param anotherCol
     * @return
     */
    public static boolean oneCollectionIsNull(Collection col, Collection anotherCol) {
        return (CollectionUtils.isEmpty(col) && !CollectionUtils.isEmpty(anotherCol))
                || (!CollectionUtils.isEmpty(col) && CollectionUtils.isEmpty(anotherCol));

    }

    /**
     * 判断集合是否为空，包括其中对象的属性全为空
     *
     * @param col
     * @return
     */
    public static boolean collectionIsNull(Collection col) {
        if (CollectionUtils.isEmpty(col)) {
            return true;
        }
        AtomicReference<Boolean> allNull = new AtomicReference<>(true);
        col.forEach(x -> {
                    Field[] fields = x.getClass().getDeclaredFields();
                    for (int i = 0; i < fields.length; i++) {
                        Field field = fields[i];
                        field.setAccessible(true);
                        //获取属性值
                        try {
                            Object value = field.get(x);
                            if (value != null) {
                                allNull.set(false);
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }
                }
        );
        return allNull.get();
    }

}
