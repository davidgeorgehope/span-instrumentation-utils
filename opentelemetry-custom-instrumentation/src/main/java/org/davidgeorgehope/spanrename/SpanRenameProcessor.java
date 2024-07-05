package org.davidgeorgehope.spanrename;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.davidgeorgehope.spanrename.config.SpanProcessorConfigLoader;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpanRenameProcessor implements SpanProcessor {

    private static Logger logger = Logger.getLogger(SpanRenameProcessor.class.getName());

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {

        printSpanAttributes(span);
        renameSpan(span);

    }
    public static void renameSpan(ReadWriteSpan span) {
        SpanData spanData = span.toSpanData();
        URLConfig config = SpanProcessorConfigLoader.getInstance().getUrlConfig(spanData.getName());

        if (config != null) {
            String[] attributesToCheck = config.getName().split(",");
            Pattern pattern = config.getRegex();
            Attributes attributes = spanData.getAttributes();

            // Build the attributes string
            StringBuilder attributesStringBuilder = new StringBuilder();
            for (String attributeKey : attributesToCheck) {
                String attributeValue = attributes.get(AttributeKey.stringKey(attributeKey.trim()));
                if (attributeValue != null) {
                    attributesStringBuilder.append(attributeValue).append(" ");
                }
            }

            if (attributesStringBuilder.length() == 0) {
                attributesStringBuilder.append(attributesToCheck);
            }

            String attributesString = attributesStringBuilder.toString().trim();

            // Apply the regex to the attributes string
            Matcher matcher = pattern.matcher(attributesString);
            if (matcher.matches()) {
                String newSpanName = matcher.group();
                span.updateName(newSpanName);
                logger.info("Span name updated to: " + newSpanName);
            } else {
                logger.warning("No regex match found for attributes string: " + attributesString);
            }
        } else {
            logger.warning("No URLConfig found for span: " + spanData.getName());
        }

    }

    public static void printSpanAttributes(ReadableSpan span) {
        SpanData spanData = ((ReadableSpan) span).toSpanData();
        spanData.getName();

        Attributes attributes = spanData.getAttributes();
        attributes.forEach((key, value) -> {
            logger.info(key + ": " + value);
        });
    }


    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        printSpanAttributes(span);
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }
}
