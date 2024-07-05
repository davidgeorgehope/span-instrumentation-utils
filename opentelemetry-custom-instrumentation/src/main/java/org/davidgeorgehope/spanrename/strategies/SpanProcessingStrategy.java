package org.davidgeorgehope.spanrename.strategies;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public abstract class SpanProcessingStrategy {
    private static Logger logger = Logger.getLogger(SpanProcessingStrategy.class.getName());

    private String returnOrArgument;
    private boolean addBaggage;
    private String className;
    private String methodName;
    private String type;

    public SpanProcessingStrategy(String returnOrArgument, boolean addBaggage, String className, String methodName, String type) {
        this.returnOrArgument = returnOrArgument;
        this.addBaggage = addBaggage;
        this.className = className;
        this.methodName = methodName;
        this.type = type;
    }

    public String getReturnOrArgument() {
        return returnOrArgument;
    }

    public boolean getAddBaggage() {
        return addBaggage;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getType() {
        return type;
    }

    public Span enterMethod(Object[] allArguments){
        Object objectToProcess;
        try {
            int argumentIndex = Integer.parseInt(getReturnOrArgument().split("argument_")[1]);
            objectToProcess = allArguments[argumentIndex];
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            logger.warning("Invalid argument index specified in configuration: " + getReturnOrArgument());
            return null;
        }

        return enterStrategy(getObject(objectToProcess));
    }
    public void exitMethod(Object returned, Throwable throwable, Span span){
        exitStrategy(getObject(returned),throwable,span);
    }

    public void setSpanAttribute(Span span, String attributeName, Object argument) {
        if (argument instanceof String) {
            span.setAttribute(attributeName, (String) argument);
        } else if (argument instanceof Integer) {
            span.setAttribute(attributeName, (int) argument);
        } else if (argument instanceof Long) {
            span.setAttribute(attributeName, (long) argument);
        } else if (argument instanceof Double) {
            span.setAttribute(attributeName, (double) argument);
        } else if (argument instanceof Boolean) {
            span.setAttribute(attributeName, (boolean) argument);
        } else {
            span.setAttribute(AttributeKey.stringKey(attributeName), argument.toString());
        }
    }

    public void renameActiveSpan(String newName) {
        logger.warning("Renaming span to: " + newName);
        Span currentSpan = Span.current();
        if (currentSpan != null && !currentSpan.getSpanContext().isRemote()) {
            currentSpan.updateName(newName);
        }
    }

    public void addBaggage(String key, String value) {
        Baggage currentBaggage = Baggage.current();
        Baggage updatedBaggage = currentBaggage.toBuilder().put(key, value).build();
        updatedBaggage.makeCurrent();
        logger.warning("Baggage item added: " + key + " = " + value);

        Span currentSpan = Span.current();
        updatedBaggage.asMap().forEach((s, baggageEntry) -> {
            currentSpan.setAttribute(s, baggageEntry.getValue());
        });
    }


    public Object getObject(Object objectToProcess) {
        String input = getReturnOrArgument();

        // Check if the string contains a dot
        if (input.contains(".")) {
            // Split the string by the dot and remove the first element
            List<String> methods = Arrays.asList(input.split("\\."));
            methods = methods.subList(1, methods.size());
            // Use reflection to sequentially invoke the methods
            objectToProcess = invokeMethods(objectToProcess, methods);
        }

        return objectToProcess;
    }


    private Object invokeMethods(Object obj, List<String> methods) {
        try {
            Object currentObject = obj;
            for (String methodName : methods) {
                // Remove parentheses from method name if present
                if (methodName.contains("(")) {
                    methodName = methodName.substring(0, methodName.indexOf("("));
                }

                // Get the method with no parameters
                Method method = currentObject.getClass().getMethod(methodName);

                // Invoke the method on the current object
                currentObject = method.invoke(currentObject);
            }
            return currentObject;
        } catch (Exception e) {
            return obj;
        }
    }

    public abstract Span enterStrategy(Object arguments);

    public abstract void exitStrategy(Object returned, Throwable throwable, Span span);

}
