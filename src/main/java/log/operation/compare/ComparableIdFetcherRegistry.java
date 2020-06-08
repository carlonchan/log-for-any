package log.operation.compare;

import java.util.HashMap;
import java.util.Map;

/**
 * 对比实体id取值注册
 * @author carlon
 */
public class ComparableIdFetcherRegistry {

    private static Map<Class, IComparableIdFetcher> INSTANCES = new HashMap<>();

    private static IComparableIdFetcher DEFAULT_FETCHER = new DefaultComparableIdFetcher();

    public static void register(Class clazz, IComparableIdFetcher fetcher) {
        INSTANCES.put(clazz, fetcher);
    }

    public static IComparableIdFetcher getInstance(Class clazz) {
        return INSTANCES.get(clazz) != null ? INSTANCES.get(clazz) : DEFAULT_FETCHER;
    }

}
