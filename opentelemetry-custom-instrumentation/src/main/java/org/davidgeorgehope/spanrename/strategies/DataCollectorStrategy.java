package org.davidgeorgehope.spanrename.strategies;

import io.opentelemetry.api.trace.Span;

import java.util.Optional;
import java.util.logging.Logger;

public class DataCollectorStrategy extends SpanProcessingStrategy {

    private static final Logger logger = Logger.getLogger(DataCollectorStrategy.class.getName());

    public DataCollectorStrategy(String returnOrArgument, boolean addBaggage, String className, String methodName, String type) {
        super(returnOrArgument, addBaggage, className, methodName, type);
    }

    @Override
    public Optional<Span> enterStrategy(Object argument) {
        Span currentSpan = Span.current();
        String attributeName = getMethodName() + "_" + argument.getClass().getName();
        setSpanAttribute(currentSpan, attributeName, argument);
        addBaggage(attributeName, argument.toString());
        return Optional.empty();
    }

    @Override
    public void exitStrategy(Object returned, Throwable throwable, Optional<Span> span) {
        Span currentSpan = Span.current();
       setSpanAttribute(currentSpan, getMethodName(), returned);
       addBaggage(getMethodName(), returned.toString());
    }
}
