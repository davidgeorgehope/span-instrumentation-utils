package org.davidgeorgehope.spanrename.strategies;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.davidgeorgehope.spanrename.context.OtelContextHolder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public abstract class SpanProcessingStrategy {
    private static final Logger logger = Logger.getLogger(SpanProcessingStrategy.class.getName());

    private final String returnOrArgument;
    private final boolean addBaggage;
    private final String className;
    private final String methodName;
    private final String type;

    protected SpanProcessingStrategy(String returnOrArgument, boolean addBaggage, String className, String methodName, String type) {
        this.returnOrArgument = returnOrArgument;
        this.addBaggage = addBaggage;
        this.className = className;
        this.methodName = methodName;
        this.type = type;
    }

    // Getters
    public String getReturnOrArgument() { return returnOrArgument; }
    public boolean getAddBaggage() { return addBaggage; }
    public String getClassName() { return className; }
    public String getMethodName() { return methodName; }
    public String getType() { return type; }

    public OtelContextHolder enterMethod(Object[] allArguments, OtelContextHolder otelContextHolder) {
        Object objectToProcess = getArgumentToProcess(allArguments);
        return enterStrategy(getObject(objectToProcess), otelContextHolder);
    }

    public void exitMethod(Object returned, Throwable throwable, OtelContextHolder otelContextHolder) {
        exitStrategy(getObject(returned), throwable, otelContextHolder);
    }

    protected void setSpanAttribute(Span span, String attributeName, Object argument) {
        if (argument instanceof String) {
            span.setAttribute(attributeName, (String) argument);
        } else if (argument instanceof Number) {
            span.setAttribute(attributeName, ((Number) argument).doubleValue());
        } else if (argument instanceof Boolean) {
            span.setAttribute(attributeName, (boolean) argument);
        } else {
            span.setAttribute(AttributeKey.stringKey(attributeName), String.valueOf(argument));
        }
    }

    protected void renameActiveSpan(String newName) {
        logger.warning("Renaming span to: " + newName);
        Span currentSpan = Span.current();
        if (currentSpan != null && !currentSpan.getSpanContext().isRemote()) {
            currentSpan.updateName(newName);
        }
    }

    protected Scope addBaggage(String key, String value) {
        Context context = Context.current();
        Baggage currentBaggage = Baggage.fromContext(context);
        Baggage updatedBaggage = currentBaggage.toBuilder().put(key, value).build();

        logger.warning("Baggage item added: " + key + " = " + value);

        Span currentSpan = Span.fromContext(context);
        if (currentSpan != null) {
            updatedBaggage.asMap().forEach((s, baggageEntry) ->
                    currentSpan.setAttribute(s, baggageEntry.getValue()));
            currentSpan.setAttribute("baggage_processed", true);
        }

        return context.with(updatedBaggage).makeCurrent();
    }

    public Object getObject(Object objectToProcess) {
        if (returnOrArgument.contains(".")) {
            List<String> methods = Arrays.asList(returnOrArgument.split("\\."));
            return invokeMethods(objectToProcess, methods.subList(1, methods.size()));
        }
        return objectToProcess;
    }

    private Object getArgumentToProcess(Object[] allArguments) {
        try {
            int argumentIndex = Integer.parseInt(returnOrArgument.split("argument_")[1]);
            return allArguments[argumentIndex];
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.warning("Invalid argument index specified in configuration: " + returnOrArgument);
            return null;
        }
    }

    private Object invokeMethods(Object obj, List<String> methods) {
        try {
            for (String methodName : methods) {
                methodName = methodName.split("\\(")[0]; // Remove parentheses if present
                Method method = obj.getClass().getMethod(methodName);
                obj = method.invoke(obj);
            }
            return obj;
        } catch (Exception e) {
            logger.warning("Error invoking methods: " + e.getMessage());
            return obj;
        }
    }

    public abstract OtelContextHolder enterStrategy(Object arguments, OtelContextHolder otelContextHolder);
    public abstract void exitStrategy(Object returned, Throwable throwable, OtelContextHolder otelContextHolder);
}