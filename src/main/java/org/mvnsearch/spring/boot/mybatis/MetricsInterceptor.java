package org.mvnsearch.spring.boot.mybatis;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class MetricsInterceptor implements Interceptor, PublicMetrics {
    public final ConcurrentHashMap<String, AtomicLong> sentences = new ConcurrentHashMap<String, AtomicLong>();

    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        wrap((mappedStatement.getId())).incrementAndGet();
        return invocation.proceed();
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {

    }

    private AtomicLong wrap(String metricName) {
        if (this.sentences.containsKey(metricName)) {
            return this.sentences.get(metricName);
        }
        AtomicLong atomic = new AtomicLong(0);
        this.sentences.put(metricName, atomic);
        return atomic;
    }

    public ConcurrentHashMap<String, AtomicLong> getSentences() {
        return sentences;
    }

    public Collection<Metric<?>> metrics() {
        return sentences.entrySet().stream().map(entry -> new Metric<>("mybatis." + entry.getKey(), entry.getValue().get())).collect(Collectors.toCollection(ArrayList::new));
    }
}
