package org.davidgeorgehope.spanrename.strategies;

import io.opentelemetry.api.trace.Span;

import java.util.Optional;
import java.util.logging.Logger;

public class SpanRenameStrategy extends SpanProcessingStrategy {

    private static final Logger logger = Logger.getLogger(SpanRenameStrategy.class.getName());

    public SpanRenameStrategy(String returnOrArgument, boolean addBaggage, String className, String methodName, String type) {
        super(returnOrArgument, addBaggage, className, methodName, type);
    }

    @Override
    public Optional<Span> enterStrategy(Object argument) {
        processValue(argument);
        return Optional.empty();
    }

    @Override
    public void exitStrategy(Object returned, Throwable throwable, Optional<Span> span) {
        processValue(returned);
    }

    public void processValue(Object value) {
        String info = (value == null) ? "null" : value.toString();
        renameActiveSpan(info);
        if(getAddBaggage()) {
            addBaggage("business_transaction", info);
        }
    }
}
