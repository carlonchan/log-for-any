package log;

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;

/**
 * 日志切面
 *
 * @author carlon
 */
public class LogAnnotationAdvisor extends AbstractPointcutAdvisor {

    private Pointcut pointcut;

    private AbstractLogInterceptor logInterceptor;


    public LogAnnotationAdvisor(AbstractLogInterceptor logInterceptor) {
        this.logInterceptor = logInterceptor;
        this.pointcut = new LogPointCut(logInterceptor);
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        // 实现了MethodInterceptor，其父类是Advice类型
        return this.logInterceptor;
    }

    private static class LogPointCut implements Pointcut {

        // 类过滤器 AnnotationClassFilter类实现了ClassFilter接口，用于过滤出被LogAutoProxy注解的类
        // checkInherited传true,检查父类或接口是否被该注解修饰，若有，则符合
        private final ClassFilter classFilter = new AnnotationClassFilter(LogAutoProxy.class, true);

        // 方法匹配器 用于匹配被该方法注解修饰的方法
        private final MethodMatcher methodMatcher;


        public LogPointCut(AbstractLogInterceptor logInterceptor) {
            methodMatcher = new AnnotationMethodMatcher(logInterceptor.annotationClass);
        }

        @Override
        public ClassFilter getClassFilter() {
            return this.classFilter;
        }

        @Override
        public MethodMatcher getMethodMatcher() {
            return this.methodMatcher;
        }
    }

}
