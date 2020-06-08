package log.operation.operator;

import java.util.HashMap;
import java.util.Map;

/**
 * 比较器注册器
 * @author gaorongyu
 */
public class OperatorServiceRegistry {

    private static final Map<String, IOperatorService> INSTANCES = new HashMap<>();

    public static void register(String name, IOperatorService operatorService) {
        INSTANCES.put(name, operatorService);
    }

    public static IOperatorService getInstance(String name) {
        if (name == null || "".equals(name.trim())) {
            if (INSTANCES.size() == 1) {
                return INSTANCES.entrySet().stream().findFirst().get().getValue();
            } else {
                throw new RuntimeException("must specify operatorService when registered more than 1 operatorService");
            }
        }
        IOperatorService instance = INSTANCES.get(name);
        if (instance == null) {
            throw new RuntimeException("operatorService is not defined");
        }
        return instance;
    }

}
