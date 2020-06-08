package log.operation;

import javax.sql.DataSource;
import java.sql.*;

/**
 * 日志存储到mysql数据库实现
 * @author carlon
 */
public class MysqlOperationLogServiceImpl implements OperationLogService {

    private DataSource dataSource;

    private String logTableName;

    private boolean camelName = false;

    public MysqlOperationLogServiceImpl(DataSource dataSource, String logTableName) {
        this.dataSource = dataSource;
        this.logTableName = logTableName;
    }

    public MysqlOperationLogServiceImpl(DataSource dataSource, String logTableName, boolean camelName) {
        this(dataSource, logTableName);
        this.camelName = camelName;
    }

    @Override
    public void save(OperationLog log) {
        // insert mysql
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = dataSource.getConnection();

            statement = conn.prepareStatement(getInsertSql());
            statement.setString(1, log.getOperationName());
            statement.setLong(2, log.getStartTime());
            statement.setLong(3, log.getEndTime());
            statement.setString(4, log.getEntityId());
            statement.setString(5, log.getPreValue());
            statement.setString(6, log.getPostValue());
            statement.setString(7, log.getDiff());
            statement.setLong(8, log.getOperatorId());
            statement.setString(9, log.getOperatorType());
            statement.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
            statement.setString(11, log.getEntityType());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 拼接插入db的sql
     * @return
     */
    private String getInsertSql() {
        StringBuffer sql = new StringBuffer();

        sql.append("insert into ").append(this.logTableName);
        sql.append(" (");
        if (this.camelName) {
            sql.append("operationName, startTime, endTime, entityId, preValue, postValue, diff, operatorId, operatorType, createTime, entityType");
        } else {
            sql.append("operation_name, start_time, end_time, entity_id, pre_value, post_value, diff, operator_id, operator_type, create_time, entity_type");
        }
        sql.append(") \n");
        sql.append("values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");

        return sql.toString();
    }

}
