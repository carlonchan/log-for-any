package log.operation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * @author carlon
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Operation {

    /**
     * 实体类型
     */
    String entityType() default "";

    /**
     *
     * @return Logger name
     */
    String name();


    /**
     * value fetcher
     */
    String valueFetcher();

    /**
     * value comparator
     * @return
     */
    String comparator() default "";

    /**
     * 操作人服务
     * @return
     */
    String operatorService();

    /**
     * for async invoke resource
     * @return
     */
    boolean async() default true;

    /**
     * entityIdParser
     * @return
     */
    String entityIdParser() default "";

}
