package org.davidgeorgehope.tests;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import org.davidgeorgehope.spanrename.SpanRenameProcessor;
import org.davidgeorgehope.spanrename.config.SpanProcessorConfigLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpanRenameProcessorTest {

    private SpanRenameProcessor spanRenameProcessor;
    private SpanProcessorConfigLoader configUtils;
    private ReadWriteSpan realSpan;
    private static final String TEST_YAML_CONTENT =
            "instrumentation:\n" +
                    "  - class: com.movieapi.ApiServlet\n" +
                    "    method: getUserFavorites\n" +
                    "    returnOrArgument: argument_0\n" +
                    "    addBaggage: TRUE\n" +
                    "    type: renamespan\n" +
                    "  - class: com.example.ClassB\n" +
                    "    method: methodB\n" +
                    "    returnOrArgument: return\n" +
                    "    addBaggage: FALSE\n" +
                    "    type: datacollector\n" +
                    "  - class: com.example.ClassB\n" +
                    "    method: methodB\n" +
                    "    type: spancreate\n" +
                    "url:\n" +
                    "  - currentUrl: GET blah/blah\n" +
                    "    name: http.request.method,url.path\n" +
                    "    regEx: .*\n";
    static Path tempFile;

    @BeforeEach
    void setup() throws IOException {
        // Create a temporary YAML file for testing
        tempFile = Files.createTempFile("testConfig", ".yaml");
        Files.write(tempFile, TEST_YAML_CONTENT.getBytes());
        // Set the system property to point to the temporary YAML file
        System.setProperty("yaml.file.name", tempFile.toString());

        spanRenameProcessor = new SpanRenameProcessor();
        configUtils = SpanProcessorConfigLoader.getInstance(); // Use real instance
        // Create a real ReadWriteSpan and SpanData
        realSpan = MockSpan.createRealSpan();
    }

    @Test
    void testRenameSpan() {
        spanRenameProcessor.renameSpan(realSpan);

        // Assuming renameSpan modifies the name of the span directly
        String updatedName = realSpan.toSpanData().getName();
        String expectedName = "GET /favorites";
        assertEquals(expectedName, updatedName);
    }

    @AfterAll
    static void tearDown() throws IOException {
        // Delete the temporary file after tests
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testOnStart() {
        Context context = Context.current();
        spanRenameProcessor.onStart(context, realSpan);

        // Assuming onStart modifies the name of the span directly
        String updatedName = realSpan.toSpanData().getName();
        String expectedName = "GET /favorites";
        assertEquals(expectedName, updatedName);
    }

}
