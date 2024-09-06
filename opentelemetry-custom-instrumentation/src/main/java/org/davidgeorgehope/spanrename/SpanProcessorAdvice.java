package org.davidgeorgehope.spanrename;

import net.bytebuddy.asm.Advice;
import org.davidgeorgehope.spanrename.config.SpanProcessorConfigLoader;
import org.davidgeorgehope.spanrename.context.OtelContextHolder;
import org.davidgeorgehope.spanrename.strategies.SpanProcessingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SpanProcessorAdvice {
    private static final Logger logger = Logger.getLogger(SpanProcessorAdvice.class.getName());

    @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
    public static List<OtelContextHolder> enterMethod(@Advice.AllArguments Object[] allArguments,
                                                      @Advice.Origin("#t") Class<?> clazz,
                                                      @Advice.Origin("#m") String method) {
        logger.info("Entering method: " + clazz.getName() + "." + method);
        return processMethod(clazz, method, allArguments, null, null, "argument");
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class, inline = false)
    public static void onExit(@Advice.Return(readOnly = true) Object returned,
                              @Advice.Thrown Throwable throwable,
                              @Advice.Origin("#t") Class<?> clazz,
                              @Advice.Origin("#m") String method,
                              @Advice.Enter List<OtelContextHolder> enteredContexts) {
        logger.info("Exiting method: " + clazz.getName() + "." + method);
        List<OtelContextHolder> exitContexts = processMethod(clazz, method, null, returned, throwable, "return");

        List<OtelContextHolder> allContexts = new ArrayList<>(enteredContexts);
        allContexts.addAll(exitContexts);

        allContexts.forEach(OtelContextHolder::closeContext);
    }

    private static List<OtelContextHolder> processMethod(Class<?> clazz, String method, Object[] arguments,
                                                         Object returned, Throwable throwable, String processingType) {
        List<OtelContextHolder> contextHolders = new ArrayList<>();
        List<SpanProcessingStrategy> strategies = SpanProcessorConfigLoader.getInstance().getConfig(clazz.getName(), method);

        if (strategies == null || strategies.isEmpty()) {
            logger.warning("No configuration found for: " + clazz.getName() + "." + method);
            return contextHolders;
        }

        strategies.stream()
                .filter(strategy -> strategy.getReturnOrArgument().contains(processingType))
                .forEach(strategy -> {
                    OtelContextHolder contextHolder = new OtelContextHolder();
                    if ("argument".equals(processingType)) {
                        strategy.enterMethod(arguments, contextHolder);
                    } else {
                        strategy.exitMethod(returned, throwable, contextHolder);
                    }
                    contextHolders.add(contextHolder);
                });

        return contextHolders;
    }
}