package log.operation.value;

import org.aopalliance.intercept.MethodInvocation;

/**
 * 实体id解析器
 * @author gaorongyu
 */
public interface IEntityIdParser {

    /**
     * 解析id(列表)
     * @param methodInvocation 执行的方法
     * @return
     */
    Object parserEntityId(MethodInvocation methodInvocation);

}
