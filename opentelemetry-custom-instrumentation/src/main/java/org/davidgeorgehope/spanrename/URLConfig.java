package org.davidgeorgehope.spanrename;

import java.util.regex.Pattern;

public class URLConfig {
    private final String currentUrl;
    private final String name;
    private final Pattern regex;

    public URLConfig(String currentUrl, String attribute, Pattern regex) {
        this.currentUrl = currentUrl;
        this.name = attribute;
        this.regex = regex;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public String getName() {
        return name;
    }

    public Pattern getRegex() {
        return regex;
    }
}
