package com.delivery.shifts.config;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class BrowserWaits {
    private BrowserWaits() {
    }

    public static WebDriverWait wait(WebDriver driver, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    public static WebElement visible(WebDriverWait wait, By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement clickable(WebDriverWait wait, By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean textPresent(WebDriverWait wait, By locator, String text) {
        return Boolean.TRUE.equals(wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text)));
    }

    public static boolean gone(WebDriverWait wait, By locator) {
        return Boolean.TRUE.equals(wait.until(ExpectedConditions.invisibilityOfElementLocated(locator)));
    }
}
