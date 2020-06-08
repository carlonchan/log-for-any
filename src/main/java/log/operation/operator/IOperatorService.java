package log.operation.operator;

import org.aopalliance.intercept.MethodInvocation;

/**
 * 操作人service 不同应用获取方式不同, 通过OperatorServiceRegistry注册自己的实现
 * @author carlon
 */
public interface IOperatorService {

    /**
     * 获取操作人id
     * @param methodInvocation 被拦截方法
     * @return OperatorInfo
     */
    OperatorInfo getOperatorId(MethodInvocation methodInvocation);

}
