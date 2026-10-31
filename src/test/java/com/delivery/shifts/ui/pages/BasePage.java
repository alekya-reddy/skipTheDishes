package com.delivery.shifts.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.qameta.allure.Allure;
import com.delivery.shifts.config.ExtentReportManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    protected void clickTab(String tabId) {
        WebElement tab = wait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(By.id(tabId)));
        try {
            tab.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException intercepted) {
            javascript("arguments[0].click()", tab);
        }
    }

    protected JavascriptExecutor javascript() {
        return (JavascriptExecutor) driver;
    }

    protected Object javascript(String script, Object... args) {
        return javascript().executeScript(script, args);
    }

    protected WebElement find(By locator) {
        return driver.findElement(locator);
    }

    protected void attachScreenshot(String name) {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
            String safeName = name.replaceAll("[^a-zA-Z0-9.-]+", "_");
            Path screenshotPath = Path.of(ExtentReportManager.screenshotDirectory(),
                    System.currentTimeMillis() + "-" + safeName + ".png");
            Files.write(screenshotPath, screenshot);
            ExtentReportManager.logStep(
                    name + "<br><img src='screenshots/" + screenshotPath.getFileName() + "' width='960'>");
        } catch (Exception exception) {
            Allure.addAttachment(name + " - capture error", "text/plain", exception.toString());
        }
    }
}
