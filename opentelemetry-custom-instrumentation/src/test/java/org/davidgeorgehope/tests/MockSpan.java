package org.davidgeorgehope.tests;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MockSpan {


    public static ReadWriteSpan createRealSpan() {
        // Implement this method to create a real ReadWriteSpan instance with the given attributes
        // This part may depend on your specific implementation and available constructors/methods
        // You may need to use your actual Span implementation or factory methods
        return new ReadWriteSpan() {
            String name = "GET blah/blah";
            @Override
            public <T> Span setAttribute(AttributeKey<T> attributeKey, T t) {
                return null;
            }

            @Override
            public Span addEvent(String s, Attributes attributes) {
                return null;
            }

            @Override
            public Span addEvent(String s, Attributes attributes, long l, TimeUnit timeUnit) {
                return null;
            }

            @Override
            public Span setStatus(StatusCode statusCode, String s) {
                return null;
            }

            @Override
            public Span recordException(Throwable throwable, Attributes attributes) {
                return null;
            }

            @Override
            public Span updateName(String s) {
                name = s;
                return this;
            }

            @Override
            public void end() {

            }

            @Override
            public void end(long l, TimeUnit timeUnit) {

            }

            @Override
            public SpanContext getSpanContext() {
                return new SpanContext() {
                    @Override
                    public String getTraceId() {
                        return null;
                    }

                    @Override
                    public String getSpanId() {
                        return null;
                    }

                    @Override
                    public TraceFlags getTraceFlags() {
                        return null;
                    }

                    @Override
                    public TraceState getTraceState() {
                        return null;
                    }

                    @Override
                    public boolean isRemote() {
                        return false;
                    }
                };
            }

            @Override
            public boolean isRecording() {
                return false;
            }


            @Override
            public SpanContext getParentSpanContext() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public SpanData toSpanData() {
                SpanData sd = new SpanData() {
                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public SpanKind getKind() {
                        return null;
                    }

                    @Override
                    public SpanContext getSpanContext() {
                        return null;
                    }

                    @Override
                    public SpanContext getParentSpanContext() {
                        return null;
                    }

                    @Override
                    public StatusData getStatus() {
                        return null;
                    }

                    @Override
                    public long getStartEpochNanos() {
                        return 0;
                    }

                    @Override
                    public Attributes getAttributes() {
                        Attributes attributes = Attributes.builder()
                                .put(AttributeKey.stringKey("client.address"), "0:0:0:0:0:0:0:1")
                                .put(AttributeKey.stringKey("http.request.method"), "GET")
                                .put(AttributeKey.stringKey("server.address"), "localhost")
                                .put(AttributeKey.stringKey("server.port"), "8081")
                                .put(AttributeKey.stringKey("thread.id"), "43")
                                .put(AttributeKey.stringKey("thread.name"), "http-nio-8081-exec-1")
                                .put(AttributeKey.stringKey("url.path"), "/favorites")
                                .put(AttributeKey.stringKey("url.query"), "user_id=user1")
                                .put(AttributeKey.stringKey("url.scheme"), "http")
                                .put(AttributeKey.stringKey("user_agent.original"), "curl/8.4.0")
                                .build();
                        return attributes;
                    }

                    @Override
                    public List<EventData> getEvents() {
                        return null;
                    }

                    @Override
                    public List<LinkData> getLinks() {
                        return null;
                    }

                    @Override
                    public long getEndEpochNanos() {
                        return 0;
                    }

                    @Override
                    public boolean hasEnded() {
                        return false;
                    }

                    @Override
                    public int getTotalRecordedEvents() {
                        return 0;
                    }

                    @Override
                    public int getTotalRecordedLinks() {
                        return 0;
                    }

                    @Override
                    public int getTotalAttributeCount() {
                        return 0;
                    }

                    @Override
                    public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
                        return null;
                    }

                    @Override
                    public Resource getResource() {
                        return null;
                    }
                };
                return sd;
            }

            @Override
            public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
                return null;
            }

            @Override
            public boolean hasEnded() {
                return false;
            }

            @Override
            public long getLatencyNanos() {
                return 0;
            }

            @Override
            public SpanKind getKind() {
                return null;
            }

            @Nullable
            @Override
            public <T> T getAttribute(AttributeKey<T> attributeKey) {
                return null;
            }
        };
    }
}
