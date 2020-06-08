package log.operation.compare;

/**
 * id取值
 * @author gaorongyu
 */
public class DefaultComparableIdFetcher implements IComparableIdFetcher {

    @Override
    public String getIdFieldName() {
        return "id";
    }

    @Override
    public Object getIdValue(Object object) {
        return CompareUtil.getIdValue(object, getIdFieldName());
    }

}
