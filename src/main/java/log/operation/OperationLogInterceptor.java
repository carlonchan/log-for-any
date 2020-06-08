package log.operation;

import com.alibaba.fastjson.JSON;
import com.whatsegg.pelikan.log.AbstractLogInterceptor;
import com.whatsegg.pelikan.log.ILogEntity;
import com.whatsegg.pelikan.log.operation.compare.IValueComparator;
import com.whatsegg.pelikan.log.operation.compare.ValueComparatorRegistry;
import com.whatsegg.pelikan.log.operation.operator.IOperatorService;
import com.whatsegg.pelikan.log.operation.operator.OperatorInfo;
import com.whatsegg.pelikan.log.operation.operator.OperatorServiceRegistry;
import com.whatsegg.pelikan.log.operation.value.EntityIdParserRegistry;
import com.whatsegg.pelikan.log.operation.value.IEntityIdParser;
import com.whatsegg.pelikan.log.operation.value.IValueFetcher;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * 操作日志拦截器
 *
 * @author carlon
 */
public class OperationLogInterceptor extends AbstractLogInterceptor<Operation> implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogInterceptor.class);

    /**
     * spring上下文，用来获取bean对象，实现ApplicationContextAware来实现赋值
     */
    private ApplicationContext applicationContext;

    /**
     * 父类抽象拦截器根据注解类找到代理类对应的真实方法，找到本次真实的注解信息
     */
    public OperationLogInterceptor() {
        super(Operation.class);
    }

    @Override
    protected Object invoke(MethodInvocation methodInvocation, Method specificMethod, Operation annotation) {
        long startTime = System.currentTimeMillis();
        logger.debug("start invoke {} at {}", specificMethod.getName(), startTime);
        Object result;
        String valueFetcherName = annotation.valueFetcher();
        IValueFetcher fetcher = (IValueFetcher) applicationContext.getBean(valueFetcherName);
        Object id = getEntityId(methodInvocation, annotation);
        logger.debug("get entity id: {}", id);
        final ILogEntity preValue = fetcher.getValue(id);
        logger.debug("get entity pre value: {}", JSON.toJSONString(preValue));

        IOperatorService operatorService = OperatorServiceRegistry.getInstance(annotation.operatorService());
        final OperatorInfo operatorInfo = operatorService.getOperatorId(methodInvocation);
        logger.debug("get operatorInfo : {}", JSON.toJSONString(operatorInfo));

        CompletableFuture<Object> cf = new CompletableFuture<>();
        try {
            result = methodInvocation.proceed();
            cf.complete(result);
        } catch (Throwable e) {
            cf.completeExceptionally(e);
        } finally {
            long endTime = System.currentTimeMillis();
            logger.debug("end invoke {} at {}", specificMethod.getName(), endTime);
            OperationLog log = initOperationLog(id, annotation, startTime, endTime, operatorInfo);
            if (annotation.async()) {
                // async execute log
                cf.thenRunAsync(() -> doLog(annotation, log, preValue, fetcher, id))
                        .exceptionally(this::handleException);
            } else {
                cf.thenRun(() -> doLog(annotation, log, preValue, fetcher, id))
                        .exceptionally(this::handleException);
            }
        }
        return cf.join();
    }

    private Void handleException(Throwable ex) {
        logger.error("run doLog exception ", ex);
        throw new RuntimeException(ex);
    }

    /**
     * 初始化log实体的值
     *
     * @param entityId     对应实体编号
     * @param annotation   Operation注解
     * @param startTime    记录日志操作开始时间
     * @param endTime      记录日志操作结束时间
     * @param operatorInfo 操作人信息
     * @return
     */
    private OperationLog initOperationLog(Object entityId, Operation annotation, long startTime, long endTime,
                                          OperatorInfo operatorInfo) {
        OperationLog log = new OperationLog();
        log.setEntityId(String.valueOf(entityId));
        log.setEntityType(annotation.entityType());
        log.setStartTime(startTime);
        log.setEndTime(endTime);
        log.setOperationName(annotation.name());
        if (operatorInfo != null) {
            log.setOperatorId(operatorInfo.getOperatorId());
            log.setOperatorType(operatorInfo.getOperatorType());
        }
        return log;
    }

    /**
     * 通过Id解析器 没指定，默认取被IdField 或<IdClass, IdField> 修饰的id，支持自定义获取id对象
     *
     * @param methodInvocation
     * @param annotation
     * @return
     */
    private Object getEntityId(MethodInvocation methodInvocation, Operation annotation) {
        // 获取指定的Id解析器
        IEntityIdParser entityIdParser = EntityIdParserRegistry.getInstance(annotation.entityIdParser());
        return entityIdParser.parserEntityId(methodInvocation);
    }

    @SuppressWarnings("unchecked")
    private void doLog(Operation annotation, OperationLog log, ILogEntity preValue, IValueFetcher fetcher, Object id) {
        // 修改后的值
        ILogEntity postValue = fetcher.getValue(id);
        logger.debug("get entity post value: {}", JSON.toJSONString(postValue));

        // 记录operation l
        IValueComparator comparator = ValueComparatorRegistry.getComparator(annotation.comparator());
        String diff = comparator.compareDifference(preValue, postValue);

        log.setPreValue(preValue != null ? preValue.toLogString() : null);
        log.setPostValue(postValue != null ? postValue.toLogString() : null);
        log.setDiff(diff);

        OperationLogService service = OperationLogServiceRegistry.getInstance();
        service.save(log);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
