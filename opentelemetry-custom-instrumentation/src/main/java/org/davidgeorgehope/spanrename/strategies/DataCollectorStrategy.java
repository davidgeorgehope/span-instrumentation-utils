package org.davidgeorgehope.spanrename.strategies;

import io.opentelemetry.api.trace.Span;
import org.davidgeorgehope.spanrename.context.OtelContextHolder;

import java.util.logging.Logger;

public class DataCollectorStrategy extends SpanProcessingStrategy {

    private static final Logger logger = Logger.getLogger(DataCollectorStrategy.class.getName());

    public DataCollectorStrategy(String returnOrArgument, boolean addBaggage, String className, String methodName, String type) {
        super(returnOrArgument, addBaggage, className, methodName, type);
    }

    @Override
    public OtelContextHolder enterStrategy(Object argument,OtelContextHolder otelContextHolder) {
        Span currentSpan = Span.current();
        String attributeName = getMethodName() + "_" + argument.getClass().getName();
        setSpanAttribute(currentSpan, attributeName, argument);
        if(getAddBaggage()) {
            otelContextHolder.setScope(addBaggage(attributeName, argument.toString()));
        }
        return otelContextHolder;
    }

    @Override
    public void exitStrategy(Object returned, Throwable throwable, OtelContextHolder otelContextHolder) {
      Span currentSpan = Span.current();
       setSpanAttribute(currentSpan, getMethodName(), returned);

        if(getAddBaggage()) {
            otelContextHolder.setScope(addBaggage(getMethodName(), returned.toString()));
        }
    }
}
