package org.davidgeorgehope.tests;

import org.davidgeorgehope.spanrename.URLConfig;
import org.davidgeorgehope.spanrename.config.SpanProcessorConfigLoader;
import org.davidgeorgehope.spanrename.strategies.SpanProcessingStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class SpanProcessorConfigLoaderTest {

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
                    "    returnOrArgument: return\n" +
                    "    type: spancreate\n" +
                    "url:\n" +
                    "  - currentUrl: GET blah/blah\n" +
                    "    name: span.url\n" +
                    "    regEx: .*\n";
    static Path tempFile;

    @BeforeAll
    static void setUp() throws IOException {
        // Create a temporary YAML file for testing
        tempFile = Files.createTempFile("testConfig", ".yaml");
        Files.write(tempFile, TEST_YAML_CONTENT.getBytes());
        // Set the system property to point to the temporary YAML file
        System.setProperty("yaml.file.name", tempFile.toString());
    }

    @AfterAll
    static void tearDown() throws IOException {
        // Delete the temporary file after tests
        Files.deleteIfExists(tempFile);
    }

    @BeforeEach
    void resetSingleton() throws NoSuchFieldException, IllegalAccessException {
        // Reset the singleton instance to ensure a fresh start for each test
        java.lang.reflect.Field instance = SpanProcessorConfigLoader.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void testGetInstance() {
        SpanProcessorConfigLoader instance = SpanProcessorConfigLoader.getInstance();
        assertNotNull(instance, "Instance should not be null");
    }

    @Test
    void testGetConfig() {
        SpanProcessorConfigLoader instance = SpanProcessorConfigLoader.getInstance();
        List<SpanProcessingStrategy> config = instance.getConfig("com.example.ClassB", "methodB");
        assertNotNull(config, "Config should not be null");
        assertEquals("return", config.get(0).getReturnOrArgument());
        assertEquals(false, config.get(0).getAddBaggage());

        config = instance.getConfig("com.movieapi.ApiServlet", "getUserFavorites");
        assertNotNull(config, "Config should not be null");
        assertEquals("argument_0", config.get(0).getReturnOrArgument());
        assertEquals(true, config.get(0).getAddBaggage());

        config = instance.getConfig("NonExistentClass", "nonExistentMethod");
        assertNull(config, "Config should be null for non-existent class and method");
    }

    @Test
    void testGetOtelServiceName() {
        System.setProperty("otel.service.name", "testService");
        SpanProcessorConfigLoader instance = SpanProcessorConfigLoader.getInstance();
        String serviceName = instance.getOtelServiceName();
        assertEquals("testService", serviceName, "Service name should match the system property");

        System.clearProperty("otel.service.name");
        serviceName = instance.getOtelServiceName();
        assertEquals("", serviceName, "Service name should be empty when system property is not set");
    }

    @Test
    void testGetAllInstrumentationConfigs() {
        SpanProcessorConfigLoader instance = SpanProcessorConfigLoader.getInstance();
        Map<String, List<SpanProcessingStrategy>> configs = instance.getAllInstrumentationConfigs();
        assertEquals(2, configs.size(), "There should be 2 instrumentation configs");
    }

    @Test
    void testConvertToEnvFormat() {
        assertEquals("OTEL_SERVICE_NAME", SpanProcessorConfigLoader.convertToEnvFormat("otel.service.name"));
        assertEquals("MY_PROPERTY", SpanProcessorConfigLoader.convertToEnvFormat("my.property"));
        assertEquals("", SpanProcessorConfigLoader.convertToEnvFormat(""));
    }


    @Test
    void testLoadYamlConfig() {
        SpanProcessorConfigLoader configUtils = SpanProcessorConfigLoader.getInstance();

        // Test instrumentation configurations
        SpanProcessingStrategy config1 = configUtils.getConfig("com.movieapi.ApiServlet", "getUserFavorites").get(0);
        assertNotNull(config1);
        assertEquals("argument_0", config1.getReturnOrArgument());
        assertTrue(config1.getAddBaggage());
        assertEquals("renamespan", config1.getType());

        SpanProcessingStrategy config2 = configUtils.getConfig("com.example.ClassB", "methodB").get(0);
        assertNotNull(config2);
        assertEquals("return", config2.getReturnOrArgument());
        assertFalse(config2.getAddBaggage());
        assertEquals("datacollector", config2.getType());

        // Test URL configurations
        URLConfig urlConfig = configUtils.getUrlConfig("GET blah/blah");
        assertNotNull(urlConfig);
        assertEquals("span.url", urlConfig.getName());
        assertEquals(Pattern.compile(".*").pattern(), urlConfig.getRegex().pattern());
    }

}
