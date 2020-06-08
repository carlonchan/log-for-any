package log.digest;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * digest log 注解 用于统计接口调用指标
 * @author carlon
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Digest {

    /**
     *
     * @return Logger name
     */
    String name();


    /**
     * log template
     * {"SaleOrder", "update", "${resp.succeeded}", "${args0.orderId}", "${arg1.getUserId()}"}
     * @return
     */
    String[] valueTemplate();

    /**
     * for async invoke resource
     * @return
     */
    boolean async() default false;

}
