package org.davidgeorgehope.spanrename;

import io.opentelemetry.api.trace.Span;
import net.bytebuddy.asm.Advice;
import org.davidgeorgehope.spanrename.config.SpanProcessorConfigLoader;
import org.davidgeorgehope.spanrename.strategies.SpanProcessingStrategy;

import java.util.Optional;
import java.util.logging.Logger;

public class SpanProcessorAdvice {

    private static Logger logger = Logger.getLogger(SpanProcessorAdvice.class.getName());

    @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
    public static Optional<Span> enterMethod(@Advice.AllArguments Object[] allArguments,
                                    @Advice.Origin("#t") Class<?> clazz,
                                    @Advice.Origin("#m") String method) {

        logger.info("Entering method: " + clazz.getName() + "." + method);
        SpanProcessingStrategy config = SpanProcessorConfigLoader.getInstance().getConfig(clazz.getName(), method);
        if (config == null) {
            logger.warning("No configuration found for: " + clazz.getName() + "." + method);
            return Optional.empty();
        }

        return config.enterMethod(allArguments);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class, inline = false)
    public static void onExit(@Advice.Return(readOnly = true) Object returned,
                              @Advice.Thrown Throwable throwable,
                              @Advice.Origin("#t") Class<?> clazz,
                              @Advice.Origin("#m") String method,
                              @Advice.Enter Optional<Span> span) {
        logger.info("Exiting method: " + clazz.getName() + "." + method);

        SpanProcessingStrategy config = SpanProcessorConfigLoader.getInstance().getConfig(clazz.getName(), method);
        if (config == null || !config.getReturnOrArgument().contains("return")) {
            logger.warning("No return configuration found for: " + clazz.getName() + "." + method);
            return;
        }

        config.exitMethod(returned, throwable, span);
    }
}
