package org.mvnsearch.spring.boot.mybatis.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Contains full set of counters/meters/timers for one metric. This class having
 * no mutable state and each counter/meter/timer being thread safe makes this
 * class thread safe too.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
final class BasicInstrumentation {
    private final Counter totalInvocations;
    private final Counter totalFailures;
    private final Meter invocationsPerSecond;
    private final Meter failuresPerSecond;
    private final Timer elapsed;

    public BasicInstrumentation(MetricRegistry registry, String baseMetricName) {
        this.totalInvocations = registry.counter(name(baseMetricName, "totalInvocations"));
        this.totalFailures = registry.counter(name(baseMetricName, "totalFailures"));
        this.invocationsPerSecond = registry.meter(name(baseMetricName, "invocationsPerSecond"));
        this.failuresPerSecond = registry.meter(name(baseMetricName, "failuresPerSecond"));
        this.elapsed = registry.timer(name(baseMetricName, "elapsed"));
    }

    public void markInvoked() {
        this.totalInvocations.inc();
        this.invocationsPerSecond.mark();
    }

    public void markFailed() {
        this.totalFailures.inc();
        this.failuresPerSecond.mark();
    }

    /**
     * Opens new timer context that needs to be closed by the caller
     */
    public Timer.Context openTimerContext() {
        return elapsed.time();
    }
}
