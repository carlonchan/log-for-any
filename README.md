# log-for-any
灵活配置，日志框架

## 如何使用
### 依赖

```
        <dependency>
           <groupId>log</groupId>
           <artifactId>log-for-any</artifactId>
           <version>1.0-SNAPSHOT</version>
        </dependency>
```

这个组件对Java应用侵入，需要Java应用引入log包,有正式包版本会在此更新。

### 配置
使用该组件需要你在Java应用中添加相应的配置
初始化MysqlOperationLogServiceImpl时，可以指定数据源，表名，及表中字段是否驼峰命名。
每个应用需要指定自己获取操作人的实现类 ClientUserOperatorServiceImpl。
示例：

```

@SpringBootConfiguration
public class AutoLogConfig {

    @Autowired
    DataSource dataSource;

    @Bean
    LogBeanPostProcessor logBeanPostProcessor() {
        return new LogBeanPostProcessor(operationLogInterceptor());
    }

    @Bean
    OperationLogInterceptor operationLogInterceptor() {
        return new OperationLogInterceptor();
    }

    @Bean
    OperationLogService operationLogService() {
        return new MysqlOperationLogServiceImpl(dataSource, "test_table", true);
    }

    @Bean
    OperationRegistryConfig operationRegistryConfig() {
        OperationRegistryConfig config = new OperationRegistryConfig();

        config.registerOperatorService("clientUserOperatorServiceImpl", operatorService());
        config.registerOperationLogService(operationLogService());

        return config;
    }

    @Bean
    IOperatorService operatorService() {
        return new ClientUserOperatorServiceImpl();
    }

}

public class ClientUserOperatorServiceImpl implements IOperatorService {

    @Autowired
    private SessionService sessionService;

    @Override
    public OperatorInfo getOperatorId(MethodInvocation methodInvocation) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();

        Session session = (Session) request.getAttribute(AuthConstants.SESSION);
        if (session == null) {
            AuthToken authToken = (AuthToken) request.getAttribute(AuthConstants.SESSION_AUTH_TOKEN);
            session = this.sessionService.getSession(authToken);
            request.setAttribute(AuthConstants.SESSION, session);
        }
        OperatorInfo operatorInfo = new OperatorInfo();
        operatorInfo.setOperatorId(session.getUserId());
        operatorInfo.setOperatorType("erp");
        return operatorInfo;
    }

}
```


### 使用方式
在接口上添加注解
@Operation(name = "clientUserLog", valueFetcher = "profileService", operatorService = "clientUserOperatorServiceImpl")
注解中的comparator()属性，不填会有一个默认json比较器，若自己实现，可以实现IValueComparator接口，自己实现比较逻辑。
id属性上加注解 @IdField final Long id
若id在对象中，对象上加@IdClass 并在对应id字段上加 @IdField。参考Login.java

@IdClass
public class Login {

    private String username;
    private String cellphone, mobile, prefix;

    @IdField
    private Long userId;
}
