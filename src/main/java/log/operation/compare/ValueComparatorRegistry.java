package log.operation.compare;

import java.util.HashMap;
import java.util.Map;

/**
 * 比较器注册器
 * @author carlon
 */
public class ValueComparatorRegistry {

    private static final Map<String, IValueComparator> INSTANCES = new HashMap<>();

    private static final IValueComparator DEFAULT_COMPARATOR = new EntityComparator();

    public static void register(String comparatorName, IValueComparator comparator) {
        INSTANCES.put(comparatorName, comparator);
    }

    public static IValueComparator getComparator(String name) {
        IValueComparator comparator = INSTANCES.get(name);
        return comparator != null ? comparator : DEFAULT_COMPARATOR;
    }

}
