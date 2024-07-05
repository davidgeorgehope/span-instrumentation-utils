package org.davidgeorgehope.tests;

import io.opentelemetry.api.trace.Span;
import org.davidgeorgehope.spanrename.strategies.SpanProcessingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpanProcessingStrategyTest {

    private SpanProcessingStrategy strategy;

    @BeforeEach
    public void setup() {
        strategy = new SpanProcessingStrategy("argument_0.getValue()", true, "TestClassName", "testMethod", "type") {
            @Override
            public Span enterStrategy(Object arguments) {
                return null;
            }

            @Override
            public void exitStrategy(Object returned, Throwable throwable, Span span) {
            }
        };
    }

    @Test
    public void testGetObject() {
        // Assuming the method exists in the class being tested
        TestClass obj = new TestClass();
        Object result = strategy.getObject(obj);
        assertEquals(obj.getValue(), result);
    }

}

