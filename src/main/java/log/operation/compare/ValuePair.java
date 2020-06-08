package log.operation.compare;

/**
 * 修改前后的值
 * @author carlon
 */
public class ValuePair {

    private Object preValue;

    private Object postValue;

    public ValuePair(Object preValue, Object postValue) {
        this.preValue = preValue;
        this.postValue = postValue;
    }

    public Object getPreValue() {
        return preValue;
    }

    public void setPreValue(Object preValue) {
        this.preValue = preValue;
    }

    public Object getPostValue() {
        return postValue;
    }

    public void setPostValue(Object postValue) {
        this.postValue = postValue;
    }

}
