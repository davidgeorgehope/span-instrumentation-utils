package org.davidgeorgehope.spanrename;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.davidgeorgehope.spanrename.config.SpanProcessorConfigLoader;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpanRenameProcessor implements SpanProcessor {

    private static final Logger logger = Logger.getLogger(SpanRenameProcessor.class.getName());

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        logSpanAttributes(span);
        renameSpan(span);
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        logSpanAttributes(span);
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }

    public void renameSpan(ReadWriteSpan span) {
        SpanData spanData = span.toSpanData();
        getUrlConfig(spanData)
                .ifPresent(config -> processSpanRename(span, spanData, config));
    }

    private Optional<URLConfig> getUrlConfig(SpanData spanData) {
        URLConfig config = SpanProcessorConfigLoader.getInstance().getUrlConfig(spanData.getName());
        if (config == null) {
            logger.warning("No URLConfig found for span: " + spanData.getName());
        }
        return Optional.ofNullable(config);
    }

    private void processSpanRename(ReadWriteSpan span, SpanData spanData, URLConfig config) {
        String attributesString = buildAttributesString(spanData.getAttributes(), config.getName().split(","));
        matchAndRenameSpan(span, attributesString, config.getRegex());
    }

    private String buildAttributesString(Attributes attributes, String[] attributeKeys) {
        String attributesString = Arrays.stream(attributeKeys)
                .map(key -> attributes.get(AttributeKey.stringKey(key.trim())))
                .filter(value -> value != null)
                .collect(Collectors.joining(" "));

        return attributesString.isEmpty() ? String.join(" ", attributeKeys) : attributesString;
    }

    private void matchAndRenameSpan(ReadWriteSpan span, String attributesString, Pattern pattern) {
        Matcher matcher = pattern.matcher(attributesString);
        if (matcher.matches()) {
            String newSpanName = matcher.group();
            span.updateName(newSpanName);
            logger.info("Span name updated to: " + newSpanName);
        } else {
            logger.warning("No regex match found for attributes string: " + attributesString);
        }
    }

    private void logSpanAttributes(ReadableSpan span) {
        SpanData spanData = span.toSpanData();
        logger.info("Logging attributes for span: " + spanData.getName());
        spanData.getAttributes().forEach((key, value) ->
                logger.log(Level.FINE, "{0}: {1}", new Object[]{key, value})
        );
    }
}