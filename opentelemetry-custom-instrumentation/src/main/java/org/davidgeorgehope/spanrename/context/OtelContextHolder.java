package org.davidgeorgehope.spanrename.context;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;

import java.util.Optional;

public class OtelContextHolder {
    private Optional<Span> span;
    private Optional<Scope> scope;

    public OtelContextHolder() {
        this.span = Optional.empty();
        this.scope = Optional.empty();
    }

    public void setSpan(Span span) {
        this.span = Optional.ofNullable(span);
    }

    public void setScope(Scope scope) {
        this.scope = Optional.ofNullable(scope);
    }

    public void closeContext() {
        scope.ifPresent(s -> {
            s.close();
            scope = Optional.empty();
        });
        span.ifPresent(s -> {
            s.end();
            span = Optional.empty();
        });
    }

    // Getters
    public Optional<Span> getSpan() {
        return span;
    }

    public Optional<Scope> getScope() {
        return scope;
    }

}