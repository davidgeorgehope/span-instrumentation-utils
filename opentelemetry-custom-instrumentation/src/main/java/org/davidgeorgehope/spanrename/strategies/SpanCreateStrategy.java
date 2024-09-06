package org.davidgeorgehope.spanrename.strategies;


import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.davidgeorgehope.spanrename.context.OtelContextHolder;

public class SpanCreateStrategy extends SpanProcessingStrategy {

    public SpanCreateStrategy(String returnOrArgument, boolean addBaggage, String className, String methodName, String type) {
        super(returnOrArgument, addBaggage, className, methodName, type);
    }

    @Override
    public OtelContextHolder enterStrategy(Object argument, OtelContextHolder otelContextHolder) {
        Tracer tracer = GlobalOpenTelemetry.getTracer("spanrename-demo", "semver:1.0.0");
        Span span = tracer.spanBuilder(argument.toString()).startSpan();
        otelContextHolder.setSpan(span);
        if(getAddBaggage()) {
            otelContextHolder.setScope(addBaggage("business_transaction", argument.toString()));
        }
        return otelContextHolder;
    }

    @Override
    public void exitStrategy(Object returned, Throwable throwable, OtelContextHolder otelContextHolder) {}
}
