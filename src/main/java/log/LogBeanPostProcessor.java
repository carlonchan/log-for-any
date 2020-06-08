
package log;

import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;

/**
 * bean的日志后处理器 织入切面
 *
 * @author gaorongyu
 */
public class LogBeanPostProcessor extends AbstractAdvisingBeanPostProcessor {

    public LogBeanPostProcessor(AbstractLogInterceptor logInterceptor) {
        this.advisor = new LogAnnotationAdvisor(logInterceptor);
        // 将当前advisor放在所有advisor的最前方
        this.beforeExistingAdvisors = true;
        // 将setProxyTargetClass的值设定为true，就是以cglib动态代理方式生成代理类，设置为false，就是默认用JDK动态代理技术
        setProxyTargetClass(true);
    }

}
