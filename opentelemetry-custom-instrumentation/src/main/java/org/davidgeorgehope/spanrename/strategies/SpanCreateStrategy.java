package org.davidgeorgehope.spanrename.strategies;


import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

public class SpanCreateStrategy extends SpanProcessingStrategy {

    public SpanCreateStrategy(String returnOrArgument, boolean addBaggage, String className, String methodName, String type) {
        super(returnOrArgument, addBaggage, className, methodName, type);
    }

    @Override
    public Span enterStrategy(Object argument) {
        Tracer tracer = GlobalOpenTelemetry.getTracer("spanrename-demo", "semver:1.0.0");
        Span span = tracer.spanBuilder(argument.toString()).startSpan();
        return span;
    }

    @Override
    public void exitStrategy(Object returned, Throwable throwable, Span span) {
        if (throwable != null) {
            span.setStatus(StatusCode.ERROR, "Exception thrown in method");
        }
        span.end();
    }
}
