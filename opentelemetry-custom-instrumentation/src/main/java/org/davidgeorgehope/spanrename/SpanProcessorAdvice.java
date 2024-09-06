package org.davidgeorgehope.spanrename;

import net.bytebuddy.asm.Advice;
import org.davidgeorgehope.spanrename.config.SpanProcessorConfigLoader;
import org.davidgeorgehope.spanrename.context.OtelContextHolder;
import org.davidgeorgehope.spanrename.strategies.SpanProcessingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SpanProcessorAdvice {

    private static Logger logger = Logger.getLogger(SpanProcessorAdvice.class.getName());

    @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
    public static List<OtelContextHolder> enterMethod(@Advice.AllArguments Object[] allArguments,
                                    @Advice.Origin("#t") Class<?> clazz,
                                    @Advice.Origin("#m") String method) {

        List<OtelContextHolder> otelContextHolderList = new ArrayList<>();
        logger.info("Entering method: " + clazz.getName() + "." + method);
        List<SpanProcessingStrategy> config = SpanProcessorConfigLoader.getInstance().getConfig(clazz.getName(), method);
        if (config == null) {
            logger.warning("No configuration found for: " + clazz.getName() + "." + method);
            return otelContextHolderList;
        }

        for (SpanProcessingStrategy strategy : config) {
            OtelContextHolder otelContextHolder = new OtelContextHolder();

            if (config == null || !strategy.getReturnOrArgument().contains("argument")) {
                logger.warning("No argument configuration found for: " + clazz.getName() + "." + method);
                continue;
            }
            strategy.enterMethod(allArguments, otelContextHolder);
            otelContextHolderList.add(otelContextHolder);
        }

        return otelContextHolderList;
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class, inline = false)
    public static void onExit(@Advice.Return(readOnly = true) Object returned,
                              @Advice.Thrown Throwable throwable,
                              @Advice.Origin("#t") Class<?> clazz,
                              @Advice.Origin("#m") String method,
                              @Advice.Enter List<OtelContextHolder> otelContextHolderList) {
        logger.info("Exiting method: " + clazz.getName() + "." + method);


        List<SpanProcessingStrategy> config = SpanProcessorConfigLoader.getInstance().getConfig(clazz.getName(), method);

        for (SpanProcessingStrategy strategy : config) {
            OtelContextHolder otelContextHolder = new OtelContextHolder();

            if (config == null || !strategy.getReturnOrArgument().contains("return")) {
                logger.warning("No return configuration found for: " + clazz.getName() + "." + method);
                continue;
            }
            strategy.exitMethod(returned, throwable, otelContextHolder);
            otelContextHolderList.add(otelContextHolder);
        }

        for(OtelContextHolder otelContextHolder:otelContextHolderList){
            otelContextHolder.closeContext();
        }
    }
}
