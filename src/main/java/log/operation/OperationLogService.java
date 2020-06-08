package log.operation;

/**
 * 日志服务
 * @author carlon
 */
public interface OperationLogService {

    /**
     * 新增日志
     * @param log 日志内容
     * @return
     */
    void save(OperationLog log);

}
