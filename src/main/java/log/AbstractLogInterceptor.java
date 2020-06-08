package log;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 日志拦截器抽象类，具体实现交给子类
 *
 * @param <T>
 * @author carlon
 */
public abstract class AbstractLogInterceptor<T extends Annotation> implements MethodInterceptor {

    /**
     * 定义在方法上的注解类
     */
    protected final Class<T> annotationClass;

    public AbstractLogInterceptor(Class<T> clazz) {
        this.annotationClass = clazz;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        Method method = methodInvocation.getMethod();
        Class clazz = methodInvocation.getThis().getClass();

        // 从代理对象上的方法，找到真实对象上对应的方法
        Method specificMethod = AopUtils.getMostSpecificMethod(method, clazz);

        T annotation = specificMethod.getAnnotation(annotationClass);
        if (annotation == null) {
            return methodInvocation.proceed();
        }

        return this.invoke(methodInvocation, specificMethod, annotation);
    }

    /**
     * do log
     *
     * @param methodInvocation
     * @param specificMethod
     * @param annotation
     * @return
     */
    protected abstract Object invoke(MethodInvocation methodInvocation, Method specificMethod, T annotation);

}
