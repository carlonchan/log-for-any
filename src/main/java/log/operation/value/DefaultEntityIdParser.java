package log.operation.value;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * 默认id解析器
 * @author carlon
 */
public class DefaultEntityIdParser implements IEntityIdParser {

    @Override
    public Object parserEntityId(MethodInvocation methodInvocation) {
        Object[] arguments = methodInvocation.getArguments();
        Parameter[] parameters = methodInvocation.getMethod().getParameters();
        // 优先找id参数
        for(int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(IdField.class)) {
                return arguments[i];
            }
        }
        // 再找对象参数里的id字段
        Object id;
        for(int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class clazz = parameter.getType();
            if (clazz.isPrimitive()) {
                continue;
            }
            Object objParam = arguments[i];
            // 如果注解在父类也找出来
            while (!Object.class.equals(clazz) && clazz != null) {
                id = getEntityIdFromObjectParam(clazz, objParam);
                if (id != null) {
                    return id;
                }
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private Object getEntityIdFromObjectParam(Class clazz, Object objParam) {
        if (clazz.isAnnotationPresent(IdClass.class)) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(IdField.class)) {
                    field.setAccessible(true);
                    try {
                        return field.get(objParam);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("get id fail " + clazz.getName() + "." + field.getName(), e);
                    }
                }
            }
        }
        return null;
    }

}
