package log.operation.compare;

/**
 * 比较值差异
 * @author gaorongyu
 */
public class ValueDifference {

    /**
     * key 目前是实体字段 调用方展示时可以根据key定义展示的alias 转换成业务方可读信息
     */
    private String key;

    /**
     * 修改前后的值对
     */
    private ValuePair valuePair;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ValuePair getValuePair() {
        return valuePair;
    }

    public void setValuePair(ValuePair valuePair) {
        this.valuePair = valuePair;
    }

}
