package org.davidgeorgehope.spanrename.config;

import org.davidgeorgehope.spanrename.URLConfig;
import org.davidgeorgehope.spanrename.factories.SpanProcessingStrategyFactory;
import org.davidgeorgehope.spanrename.strategies.SpanCreateStrategy;
import org.davidgeorgehope.spanrename.strategies.SpanProcessingStrategy;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SpanProcessorConfigLoader {

    private static SpanProcessorConfigLoader instance;
    private static Logger logger = Logger.getLogger(SpanProcessorConfigLoader.class.getName());

    private final Map<String, List<SpanProcessingStrategy>> instrumentationConfigs = new HashMap<>();
    private final Map<String, URLConfig> urlConfigs = new HashMap<>();

    // Private constructor to prevent instantiation
    private SpanProcessorConfigLoader(String fileName) {
        loadYamlConfig(fileName);
    }

    // Public method to provide access to the instance
    public static SpanProcessorConfigLoader getInstance() {
        if (instance == null) {
            synchronized (SpanProcessorConfigLoader.class) {
                if (instance == null) {
                    instance = new SpanProcessorConfigLoader(SpanProcessorConfigLoader.getSystemProperty("yaml.file.name"));
                }
            }
        }
        return instance;
    }

    public List<SpanProcessingStrategy> getConfig(String className, String methodName) {
        return instrumentationConfigs.getOrDefault(className + "." + methodName, null);
    }

    public URLConfig getUrlConfig(String currentUrl) {
        return urlConfigs.getOrDefault(currentUrl, null);
    }

    public String getOtelServiceName() {
        return getSystemProperty("otel.service.name");
    }

    public void loadYamlConfig(String filePath) {
        logger.warning("loadYamlConfig 1");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            logger.warning("loadYamlConfig 2");

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
                logger.warning("loadYamlConfig 3");

                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("instrumentation:")) {
                    // Reset for new instrumentation block
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
                    addBaggage = line.split(":")[1].trim().equalsIgnoreCase("true");
                } else if (line.startsWith("type:")) {
                    type = line.split(":")[1].trim();
                } else if (line.startsWith("url:")) {
                    // Reset for new URL block
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

                    String key = currentClass + "." + currentMethod;

                    // Check for existing spancreate strategy
                    boolean spanCreateExists = instrumentationConfigs.containsKey(key) &&
                            instrumentationConfigs.get(key).stream().anyMatch(config -> config instanceof SpanCreateStrategy);

                    if (type.equalsIgnoreCase("spancreate") && spanCreateExists) {
                        logger.warning("Ignoring additional spancreate strategy for " + key);
                    } else {
                        SpanProcessingStrategy config = SpanProcessingStrategyFactory.createStrategy(returnOrArgument, addBaggage, currentClass, currentMethod, type);
                        instrumentationConfigs.computeIfAbsent(key, k -> new ArrayList<>()).add(config);
                    }

                    // Reset for next block
                    currentClass = null;
                    currentMethod = null;
                    returnOrArgument = null;
                    addBaggage = null;
                    type = null;
                }

                if (currentUrl != null && name != null && regex != null) {
                    URLConfig urlConfig = new URLConfig(currentUrl, name, regex);
                    urlConfigs.put(currentUrl, urlConfig);
                    logger.warning("loadYamlConfig URL: " + currentUrl);

                    // Reset for next block
                    currentUrl = null;
                    name = null;
                    regex = null;
                }
            }
        } catch (FileNotFoundException e) {
            logger.warning("File not found: " + filePath);
            logger.warning("File path: " + filePath);
            logger.warning("File exists: " + new File(filePath).exists());
            logger.warning("Is directory: " + new File(filePath).isDirectory());
            logger.warning("Is file: " + new File(filePath).isFile());
            logger.warning("Readable: " + new File(filePath).canRead());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, List<SpanProcessingStrategy>> getAllInstrumentationConfigs() {
        return instrumentationConfigs;
    }

    public Map<String, URLConfig> getAllUrlConfigs() {
        return urlConfigs;
    }

    public static String getSystemProperty(String property) {
        try {
            // First, check system properties
            String value = System.getProperty(property);

            // If no system property found, check environment variables
            if (value == null) {
                value = System.getenv(convertToEnvFormat(property));
            }

            if (value == null) {
                value = "";
            }

            return value;
        } catch (Exception e) {
            return "";
        }
    }

    public static String convertToEnvFormat(String configKey) {
        if (configKey == null || configKey.isEmpty()) {
            return configKey;
        }
        return configKey.replace('.', '_').toUpperCase();
    }

}
