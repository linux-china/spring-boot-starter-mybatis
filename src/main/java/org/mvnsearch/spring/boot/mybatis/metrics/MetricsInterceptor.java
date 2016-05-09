package org.mvnsearch.spring.boot.mybatis.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.mvnsearch.spring.boot.mybatis.MybatisProperties;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mybatis metrics interceptor
 *
 * @author linux_china
 */
@Intercepts({
        @Signature(
                type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}) //
})
public class MetricsInterceptor implements Interceptor {
    /**
     * mybatis properties
     */
    private MybatisProperties properties;
    /**
     * instrumentation cache
     */
    private Map<String, BasicInstrumentation> instrumentations = new ConcurrentHashMap<>();
    /**
     * metric registry
     */
    private MetricRegistry metrics = new MetricRegistry();

    public MetricsInterceptor(MybatisProperties properties) {
        this.properties = properties;
    }

    public Object intercept(Invocation invocation) throws Throwable {
        BasicInstrumentation instrumentation = getInstrumentation(invocation);
        try (Timer.Context ctx = instrumentation.openTimerContext()) {
            instrumentation.markInvoked();
            return invocation.proceed();
        } catch (Throwable e) {
            instrumentation.markFailed();
            throw e;
        }
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {
    }

    protected BasicInstrumentation getInstrumentation(Invocation invocation) {
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        if (!instrumentations.containsKey(statement.getId())) {
            instrumentations.put(statement.getId(), new BasicInstrumentation(metrics, statement.getId()));
        }
        return instrumentations.get(statement.getId());
    }

    public MetricRegistry getMetrics() {
        return metrics;
    }
}
