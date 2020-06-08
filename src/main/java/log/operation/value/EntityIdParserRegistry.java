package log.operation.value;

import java.util.HashMap;
import java.util.Map;

/**
 * id 解析器注册
 * @author gaorongyu
 */
public class EntityIdParserRegistry {

    private static final IEntityIdParser DEFAULT_ID_PARSER = new DefaultEntityIdParser();

    private static Map<String, IEntityIdParser> INSTANCES = new HashMap<>();

    public static void register(String name, IEntityIdParser parser) {
        INSTANCES.put(name, parser);
    }

    public static IEntityIdParser getInstance(String name) {
        IEntityIdParser parser = INSTANCES.get(name);
        return parser != null ? parser : DEFAULT_ID_PARSER;
    }

}
