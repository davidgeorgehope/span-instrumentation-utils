package org.davidgeorgehope.spanrename.strategies;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import org.davidgeorgehope.spanrename.context.OtelContextHolder;

import java.util.logging.Logger;

public class SpanRenameStrategy extends SpanProcessingStrategy {

    private static final Logger logger = Logger.getLogger(SpanRenameStrategy.class.getName());

    public SpanRenameStrategy(String returnOrArgument, boolean addBaggage, String className, String methodName, String type) {
        super(returnOrArgument, addBaggage, className, methodName, type);
    }

    @Override
    public OtelContextHolder enterStrategy(Object argument, OtelContextHolder otelContextHolder) {
        return processValue(argument,otelContextHolder);
    }

    @Override
    public void exitStrategy(Object returned, Throwable throwable, OtelContextHolder otelContextHolder) {
        processValue(returned, otelContextHolder);
    }

    public OtelContextHolder processValue(Object value, OtelContextHolder otelContextHolder) {
        String info = (value == null) ? "null" : value.toString();
        Context context = Context.current();
        Baggage baggage = Baggage.fromContext(context);
        String businessTransaction = baggage.getEntryValue("business_transaction");
        if (businessTransaction == null) {
            // Use the business transaction value
            renameActiveSpan(info);
        }

        if(getAddBaggage()) {
            otelContextHolder.setScope(addBaggage("business_transaction", info));
        }
        return otelContextHolder;
    }


}
