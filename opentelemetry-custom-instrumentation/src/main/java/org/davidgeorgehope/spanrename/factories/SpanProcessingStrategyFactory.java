package org.davidgeorgehope.spanrename.factories;

import org.davidgeorgehope.spanrename.strategies.DataCollectorStrategy;
import org.davidgeorgehope.spanrename.strategies.SpanCreateStrategy;
import org.davidgeorgehope.spanrename.strategies.SpanProcessingStrategy;
import org.davidgeorgehope.spanrename.strategies.SpanRenameStrategy;

public class SpanProcessingStrategyFactory {

    public static SpanProcessingStrategy createStrategy(String returnOrArgument, boolean addBaggage, String className, String methodName, String type) {
        switch (type.toLowerCase()) {
            case "spancreate":
                return new SpanCreateStrategy(returnOrArgument, addBaggage, className, methodName, type);
            case "datacollector":
                return new DataCollectorStrategy(returnOrArgument, addBaggage, className, methodName, type);
            case "renamespan":
                return new SpanRenameStrategy(returnOrArgument, addBaggage, className, methodName, type);
            default:
                throw new IllegalArgumentException("Unknown strategy type: " + type);
        }
    }
}
