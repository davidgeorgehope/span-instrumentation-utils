package org.davidgeorgehope.tests;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.davidgeorgehope.spanrename.strategies.SpanCreateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SpanCreateStrategyTest {

    private SpanCreateStrategy strategy;
    private Tracer tracer;

    @BeforeEach
    public void setup() {
        strategy = new SpanCreateStrategy("argument_0", true, "TestClassName", "testMethod", "type");
        tracer = GlobalOpenTelemetry.getTracer("spanrename-demo", "semver:1.0.0");
    }

    @Test
    public void testEnterStrategy() {
        Span span = tracer.spanBuilder("testSpan").startSpan();
        try (Scope scope = span.makeCurrent()) {
            Span result = strategy.enterStrategy("testSpan");
            assertNotNull(result);
        } finally {
            span.end();
        }
    }
}

