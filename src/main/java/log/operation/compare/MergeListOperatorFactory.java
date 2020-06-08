package log.operation.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * 合并list
 *
 * @author gaorongyu
 */
public class MergeListOperatorFactory {

    public static final BinaryOperator<List<ValueDifference>> VALUE_DIFFERENCE_MERGE = new MergeDifferenceValueListOperator();

    public static final BinaryOperator<List<Object>> OBJECT_MERGE = new MergeListOperator();

    public static class MergeDifferenceValueListOperator implements BinaryOperator<List<ValueDifference>> {

        @Override
        public List<ValueDifference> apply(List<ValueDifference> list, List<ValueDifference> list2) {
            if (list2 != null && list != null) {
                list.addAll(list2);
            } else if (list == null && list2 != null) {
                list = list2;
            } else if (list == null && list2 == null) {
                return new ArrayList<>();
            }
            return list;
        }
    }

    public static class MergeListOperator implements BinaryOperator<List<Object>> {

        @Override
        public List<Object> apply(List<Object> list, List<Object> list2) {
            if (list2 != null && list != null) {
                list.addAll(list2);
            }
            return list;
        }
    }

}
