package com.delivery.shifts.config;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ExtentReportManager {
    private static final Path REPORT_DIRECTORY =
            Path.of("target", "extent-reports");
    private static final Path SCREENSHOT_DIRECTORY =
            REPORT_DIRECTORY.resolve("screenshots");
    private static final ExtentReports REPORTS = createReports();
    private static final ThreadLocal<ExtentTest> CURRENT_TEST =
            new ThreadLocal<>();
    private static final AtomicBoolean CREATED = new AtomicBoolean(false);

    private ExtentReportManager() {
    }

    private static ExtentReports createReports() {
        try {
            Files.createDirectories(SCREENSHOT_DIRECTORY);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create Extent report directories", exception);
        }

        ExtentSparkReporter reporter =
                new ExtentSparkReporter(REPORT_DIRECTORY.resolve("ExtentReport.html").toFile());
        reporter.config().setTheme(Theme.STANDARD);
        reporter.config().setDocumentTitle("Courier Shifts Automation - Current Execution");
        reporter.config().setReportName("Courier Shifts Test Execution Report");
        reporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");

        ExtentReports reports = new ExtentReports();
        reports.attachReporter(reporter);
        reports.setSystemInfo("Application UI", TestConfig.uiBaseUrl());
        reports.setSystemInfo("Application API", TestConfig.apiBaseUrl());
        reports.setSystemInfo("Browser Headless", Boolean.toString(TestConfig.headless()));
        return reports;
    }

    public static synchronized ExtentTest startTest(String className, String methodName, String displayName) {
        ExtentTest test = REPORTS.createTest(className + "." + methodName)
                .assignCategory(className)
                .createNode(displayName);
        CURRENT_TEST.set(test);
        CREATED.set(true);
        return test;
    }

    public static ExtentTest currentTest() {
        return CURRENT_TEST.get();
    }

    public static void logStep(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.info(message);
        }
    }

    public static void logPass(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.pass(message);
        }
    }

    public static void logFail(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.fail(message);
        }
    }

    public static void pass(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.pass(message);
        }
    }

    public static void fail(String message, Throwable throwable) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.fail(message).fail(throwable);
        }
    }

    public static void skip(String message) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) {
            test.skip(message);
        }
    }

    public static String screenshotDirectory() {
        return SCREENSHOT_DIRECTORY.toString();
    }

    public static synchronized void flush() {
        if (CREATED.get()) {
            REPORTS.flush();
        }
    }

    public static void clearCurrentTest() {
        CURRENT_TEST.remove();
    }
}
