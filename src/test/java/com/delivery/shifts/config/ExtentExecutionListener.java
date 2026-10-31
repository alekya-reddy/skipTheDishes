package com.delivery.shifts.config;

import com.aventstack.extentreports.ExtentTest;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.Optional;

public class ExtentExecutionListener implements TestExecutionListener {
    private TestPlan testPlan;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (!testIdentifier.isTest()) {
            return;
        }
        Optional<TestIdentifier> parent = testPlan.getParent(testIdentifier);
        String className = parent.map(identifier -> identifier.getLegacyReportingName())
                .orElse("Unknown test class");
        ExtentReportManager.startTest(
                className,
                testIdentifier.getLegacyReportingName(),
                testIdentifier.getDisplayName());
        ExtentReportManager.logStep("Test started: " + testIdentifier.getDisplayName());
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier,
                                  TestExecutionResult testExecutionResult) {
        if (!testIdentifier.isTest()) {
            return;
        }
        switch (testExecutionResult.getStatus()) {
            case SUCCESSFUL -> ExtentReportManager.pass("Test passed");
            case FAILED -> ExtentReportManager.fail(
                    "Test failed",
                    testExecutionResult.getThrowable().orElse(null));
            case ABORTED -> ExtentReportManager.skip("Test aborted by the test runner");
        }
        ExtentReportManager.clearCurrentTest();
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        ExtentReportManager.flush();
    }
}
