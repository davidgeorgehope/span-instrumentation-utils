package org.davidgeorgehope.tests;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import org.davidgeorgehope.spanrename.context.OtelContextHolder;
import org.davidgeorgehope.spanrename.strategies.DataCollectorStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataCollectorStrategyTest {

    private DataCollectorStrategy strategy;

    @BeforeEach
    public void setup() {
        strategy = new DataCollectorStrategy("return", true, "TestClassName", "testMethod", "type");
    }

    @Test
    public void testEnterStrategy() {
        ReadWriteSpan span = MockSpan.createRealSpan();
        try (Scope scope = span.makeCurrent()) {
            TestClass testClass = new TestClass();
            OtelContextHolder otelContextHolder = new OtelContextHolder();
            strategy.enterStrategy(testClass,otelContextHolder);

        } finally {
            span.end();
        }
    }
}

