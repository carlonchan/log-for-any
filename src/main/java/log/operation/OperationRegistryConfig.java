package log.operation;

import  log.operation.compare.ComparableIdFetcherRegistry;
import  log.operation.compare.IComparableIdFetcher;
import  log.operation.compare.IValueComparator;
import  log.operation.compare.ValueComparatorRegistry;
import  log.operation.operator.IOperatorService;
import  log.operation.operator.OperatorServiceRegistry;
import  log.operation.value.EntityIdParserRegistry;
import  log.operation.value.IEntityIdParser;

/**
 * 操作日志配置
 * @author carlon
 */
public class OperationRegistryConfig {

    public void registerOperationLogService(OperationLogService operationLogService) {
        OperationLogServiceRegistry.register(operationLogService);
    }

    public void registerOperatorService(String name, IOperatorService operatorService) {
        OperatorServiceRegistry.register(name, operatorService);
    }

    public void registerValueComparator(String name, IValueComparator comparator) {
        ValueComparatorRegistry.register(name, comparator);
    }

    public void registerEntityIdParser(String name, IEntityIdParser parser) {
        EntityIdParserRegistry.register(name, parser);
    }

    public void registerComparableIdFetcher(Class clazz, IComparableIdFetcher fetcher) {
        ComparableIdFetcherRegistry.register(clazz, fetcher);
    }

}
