package org.davidgeorgehope.spanrename.config;

import org.davidgeorgehope.spanrename.URLConfig;
import org.davidgeorgehope.spanrename.factories.SpanProcessingStrategyFactory;
import org.davidgeorgehope.spanrename.strategies.SpanCreateStrategy;
import org.davidgeorgehope.spanrename.strategies.SpanProcessingStrategy;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SpanProcessorConfigLoader {
    private static final Logger logger = Logger.getLogger(SpanProcessorConfigLoader.class.getName());
    private static volatile SpanProcessorConfigLoader instance;

    private final Map<String, List<SpanProcessingStrategy>> instrumentationConfigs = new HashMap<>();
    private final Map<String, URLConfig> urlConfigs = new HashMap<>();

    private SpanProcessorConfigLoader(String fileName) {
        loadConfig(fileName);
    }

    public static SpanProcessorConfigLoader getInstance() {
        if (instance == null) {
            synchronized (SpanProcessorConfigLoader.class) {
                if (instance == null) {
                    instance = new SpanProcessorConfigLoader(getSystemProperty("yaml.file.name"));
                }
            }
        }
        return instance;
    }

    public List<SpanProcessingStrategy> getConfig(String className, String methodName) {
        return instrumentationConfigs.get(className + "." + methodName);
    }

    public URLConfig getUrlConfig(String currentUrl) {
        return urlConfigs.get(currentUrl);
    }

    public String getOtelServiceName() {
        return getSystemProperty("otel.service.name");
    }

    private void loadConfig(String filePath) {
        logger.info("Loading configuration from: " + filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentClass = null;
            String currentMethod = null;
            String returnOrArgument = null;
            Boolean addBaggage = null;
            String type = null;
            String currentUrl = null;
            String name = null;
            Pattern regex = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("instrumentation:")) {
                    currentClass = null;
                    currentMethod = null;
                    returnOrArgument = null;
                    addBaggage = null;
                    type = null;
                } else if (line.startsWith("- class:")) {
                    currentClass = line.split(":")[1].trim();
                } else if (line.startsWith("method:")) {
                    currentMethod = line.split(":")[1].trim();
                } else if (line.startsWith("returnOrArgument:")) {
                    returnOrArgument = line.split(":")[1].trim();
                } else if (line.startsWith("addBaggage:")) {
                    addBaggage = Boolean.parseBoolean(line.split(":")[1].trim());
                } else if (line.startsWith("type:")) {
                    type = line.split(":")[1].trim();
                } else if (line.startsWith("url:")) {
                    currentUrl = null;
                    name = null;
                    regex = null;
                } else if (line.startsWith("- currentUrl:")) {
                    currentUrl = line.split(":")[1].trim();
                } else if (line.startsWith("name:")) {
                    name = line.split(":")[1].trim();
                } else if (line.startsWith("regEx:")) {
                    regex = Pattern.compile(line.split(":")[1].trim());
                }

                if (currentClass != null && currentMethod != null && returnOrArgument != null && addBaggage != null && type != null) {
                    processInstrumentationConfig(currentClass, currentMethod, returnOrArgument, addBaggage, type);
                    currentClass = null;
                    currentMethod = null;
                    returnOrArgument = null;
                    addBaggage = null;
                    type = null;
                }

                if (currentUrl != null && name != null && regex != null) {
                    urlConfigs.put(currentUrl, new URLConfig(currentUrl, name, regex));
                    logger.info("Added URL configuration: " + currentUrl);
                    currentUrl = null;
                    name = null;
                    regex = null;
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading configuration file: " + filePath, e);
        }
    }

    private void processInstrumentationConfig(String className, String methodName, String returnOrArgument, boolean addBaggage, String type) {
        String key = className + "." + methodName;
        List<SpanProcessingStrategy> strategies = instrumentationConfigs.computeIfAbsent(key, k -> new ArrayList<>());

        boolean spanCreateExists = strategies.stream().anyMatch(config -> config instanceof SpanCreateStrategy);

        if (!"spancreate".equalsIgnoreCase(type) || !spanCreateExists) {
            SpanProcessingStrategy config = SpanProcessingStrategyFactory.createStrategy(returnOrArgument, addBaggage, className, methodName, type);
            strategies.add(config);
        } else {
            logger.warning("Ignoring additional spancreate strategy for " + key);
        }
    }

    public Map<String, List<SpanProcessingStrategy>> getAllInstrumentationConfigs() {
        return new HashMap<>(instrumentationConfigs);
    }

    public Map<String, URLConfig> getAllUrlConfigs() {
        return new HashMap<>(urlConfigs);
    }

    public static String getSystemProperty(String property) {
        String value = System.getProperty(property);
        if (value == null) {
            value = System.getenv(convertToEnvFormat(property));
        }
        return value != null ? value : "";
    }

    public static String convertToEnvFormat(String configKey) {
        return configKey != null ? configKey.replace('.', '_').toUpperCase() : "";
    }
}