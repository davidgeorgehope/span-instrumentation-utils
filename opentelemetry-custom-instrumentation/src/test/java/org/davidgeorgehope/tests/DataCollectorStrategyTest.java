package org.davidgeorgehope.tests;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import org.davidgeorgehope.spanrename.strategies.DataCollectorStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataCollectorStrategyTest {

    private DataCollectorStrategy strategy;

    @BeforeEach
    public void setup() {
        strategy = new DataCollectorStrategy("argument_0", true, "TestClassName", "testMethod", "type");
    }

    @Test
    public void testEnterStrategy() {
        ReadWriteSpan span = MockSpan.createRealSpan();
        try (Scope scope = span.makeCurrent()) {
            TestClass testClass = new TestClass();

            strategy.enterStrategy(testClass);

        } finally {
            span.end();
        }
    }
}

