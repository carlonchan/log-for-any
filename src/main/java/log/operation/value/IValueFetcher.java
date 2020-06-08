package log.operation.value;

import com.whatsegg.pelikan.log.ILogEntity;

/**
 * 获取value接口
 * @author gaorongyu
 */
public interface IValueFetcher<T> {

    /**
     * 获取value
     * @param id 实体唯一标示 如单据id, 单据编码
     * @return object
     */
    ILogEntity getValue(T id);

}
