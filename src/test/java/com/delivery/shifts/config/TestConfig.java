package com.delivery.shifts.config;

public final class TestConfig {
    private TestConfig() {
    }

    public static String apiBaseUrl() {
        return setting("api.baseUrl", "API_BASE_URL", "http://localhost:8080");
    }

    public static String uiBaseUrl() {
        return setting("ui.baseUrl", "UI_BASE_URL", "http://localhost:3000");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(setting("browser.headless", "BROWSER_HEADLESS", "true"));
    }

    public static int browserTimeoutSeconds() {
        return integerSetting("browser.timeoutSeconds", "BROWSER_TIMEOUT_SECONDS", 15);
    }

    public static String reportDirectory() {
        return setting("allure.reportDirectory", "ALLURE_REPORT_DIRECTORY", "target/site/allure-maven-plugin");
    }

    private static int integerSetting(String property, String environment, int fallback) {
        String value = setting(property, environment, Integer.toString(fallback));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid integer setting for " + property + ": " + value, exception);
        }
    }

    private static String setting(String property, String environment, String fallback) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environment);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
