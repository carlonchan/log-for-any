package log.operation.value;

import log.ILogEntity;

/**
 * 获取value接口
 * @author carlon
 */
public interface IValueFetcher<T> {

    /**
     * 获取value
     * @param id 实体唯一标示 如单据id, 单据编码
     * @return object
     */
    ILogEntity getValue(T id);

}
