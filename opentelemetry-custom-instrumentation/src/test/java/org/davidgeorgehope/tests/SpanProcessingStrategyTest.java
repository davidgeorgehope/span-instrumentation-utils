package org.davidgeorgehope.tests;

import org.davidgeorgehope.spanrename.context.OtelContextHolder;
import org.davidgeorgehope.spanrename.strategies.SpanProcessingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpanProcessingStrategyTest {

    private SpanProcessingStrategy strategy;

    @BeforeEach
    public void setup() {
        strategy = new SpanProcessingStrategy("return.getValue()", true, "TestClassName", "testMethod", "type") {
            @Override
            public OtelContextHolder enterStrategy(Object arguments, OtelContextHolder otelContextHolder) {
                return null;
            }

            @Override
            public void exitStrategy(Object returned, Throwable throwable, OtelContextHolder otelContextHolder) {
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

