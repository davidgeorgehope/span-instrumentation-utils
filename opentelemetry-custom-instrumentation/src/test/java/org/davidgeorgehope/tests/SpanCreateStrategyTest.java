package org.davidgeorgehope.tests;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.davidgeorgehope.spanrename.context.OtelContextHolder;
import org.davidgeorgehope.spanrename.strategies.SpanCreateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
            OtelContextHolder otelContextHolder = new OtelContextHolder();
            OtelContextHolder result = strategy.enterStrategy("testSpan",otelContextHolder);
           // assertNotNull(result.get());
        } finally {
            span.end();
        }
    }
}

