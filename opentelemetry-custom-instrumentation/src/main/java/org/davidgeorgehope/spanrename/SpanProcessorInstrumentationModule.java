/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.davidgeorgehope.spanrename;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.davidgeorgehope.spanrename.context.OtelContextHolder;
import org.davidgeorgehope.spanrename.factories.SpanProcessingStrategyFactory;
import org.davidgeorgehope.spanrename.strategies.DataCollectorStrategy;
import org.davidgeorgehope.spanrename.strategies.SpanCreateStrategy;
import org.davidgeorgehope.spanrename.strategies.SpanProcessingStrategy;
import org.davidgeorgehope.spanrename.config.SpanProcessorConfigLoader;
import org.davidgeorgehope.spanrename.strategies.SpanRenameStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static net.bytebuddy.matcher.ElementMatchers.namedOneOf;

@AutoService(InstrumentationModule.class)
public final class SpanProcessorInstrumentationModule extends InstrumentationModule {
    public SpanProcessorInstrumentationModule() {
        super("spanrename-demo", "spanrename");
    }
    private static Logger logger = Logger.getLogger(SpanProcessorInstrumentationModule.class.getName());

    @Override
    public int order() {
        return 1;
    }

    @Override
    public List<String> getAdditionalHelperClassNames() {
        return List.of(
                SpanProcessorConfigLoader.class.getName(),
                SpanProcessingStrategyFactory.class.getName(),
                SpanProcessorAdvice.class.getName(),
                SpanRenameStrategy.class.getName(),
                SpanProcessingStrategy.class.getName(),
                SpanCreateStrategy.class.getName(),
                DataCollectorStrategy.class.getName(),
                OtelContextHolder.class.getName()
        );
    }

    @Override
    public boolean isHelperClass(String className) {
        return className.startsWith("org.davidgeorgehope");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        List<TypeInstrumentation> typeInstrumentations = new ArrayList<>();
        SpanProcessorConfigLoader.getInstance();
        logger.warning("LOADED CONFIGURATION");
        Map<String, List<SpanProcessingStrategy>> configs = SpanProcessorConfigLoader.getInstance().getAllInstrumentationConfigs();
        logger.warning("CONFIG "+configs);

        for (Map.Entry<String, List<SpanProcessingStrategy>> entry : configs.entrySet()) {
            String className = entry.getValue().get(0).getClassName();
            String methodName = entry.getValue().get(0).getMethodName();
            logger.warning("CLASS "+className + "   "+methodName);

            typeInstrumentations.add(createTypeInstrumentation(className, methodName));
        }
        logger.warning("CLASS "+typeInstrumentations);

        return typeInstrumentations;
    }

    public static TypeInstrumentation createTypeInstrumentation(String instrumentationClass, String instrumentationMethod) {
        logger.warning("createTypeInstrumentation");
        return new TypeInstrumentation() {

            @Override
            public ElementMatcher<TypeDescription> typeMatcher() {
                logger.warning("TypeInstrumentation matcher" +instrumentationClass);
                return ElementMatchers.named(instrumentationClass);
            }

            @Override
            public void transform(TypeTransformer typeTransformer) {
                logger.warning("TypeInstrumentation transform>" +instrumentationMethod + "<");
                logger.warning("TypeInstrumentation transform" +typeTransformer);

                typeTransformer.applyAdviceToMethod(namedOneOf(instrumentationMethod), SpanProcessorAdvice.class.getName());
            }
        };
    }
}
