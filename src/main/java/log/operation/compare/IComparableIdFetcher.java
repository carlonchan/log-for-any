package log.operation.compare;

/**
 * 对比列表时 取列表元素的id 做为两边关联的key. 默认取id,可通过ComparableIdFetcherRegistry注册
 * @param <T>
 * @author carlon
 */
public interface IComparableIdFetcher<T> {

    /**
     * 获取id字段名称
     * @return
     */
    String getIdFieldName();

    /**
     * 获取id值
     * @param object
     * @return
     */
    Object getIdValue(T object);

}
