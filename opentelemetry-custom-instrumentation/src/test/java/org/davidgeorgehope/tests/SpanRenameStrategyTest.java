package org.davidgeorgehope.tests;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import org.davidgeorgehope.spanrename.context.OtelContextHolder;
import org.davidgeorgehope.spanrename.strategies.SpanRenameStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpanRenameStrategyTest {

    private SpanRenameStrategy strategy;

    @BeforeEach
    public void setup() {
        strategy = new SpanRenameStrategy("return", true, "TestClassName", "testMethod", "type");
    }

    @Test
    public void testProcessValue() {
        ReadWriteSpan span = MockSpan.createRealSpan();
        try (Scope scope = span.makeCurrent()) {
            String spanName = "testValue";
            OtelContextHolder otelContextHolder = new OtelContextHolder();
            strategy.processValue(spanName,otelContextHolder);
            String updatedName = span.toSpanData().getName();
            assertEquals("testValue", updatedName);
        } finally {
            span.end();
        }
    }
}


