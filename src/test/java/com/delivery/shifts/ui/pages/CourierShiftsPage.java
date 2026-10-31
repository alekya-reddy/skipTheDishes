package com.delivery.shifts.ui.pages;

import com.delivery.shifts.config.BrowserWaits;
import com.delivery.shifts.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import io.qameta.allure.Allure;

import java.io.ByteArrayInputStream;

public class CourierShiftsPage implements AutoCloseable {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final CouriersPage couriers;
    private final ShiftsPage shifts;
    private final BookingsPage bookings;

    public CourierShiftsPage() {
        ChromeOptions options = new ChromeOptions();
        if (TestConfig.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000", "--no-sandbox");
        driver = new ChromeDriver(options);
        wait = BrowserWaits.wait(driver, TestConfig.browserTimeoutSeconds());
        driver.get(TestConfig.uiBaseUrl());
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("#shift-zone option"), 0));

        couriers = new CouriersPage(driver, wait);
        shifts = new ShiftsPage(driver, wait);
        bookings = new BookingsPage(driver, wait);
    }

    public CouriersPage couriers() {
        return couriers;
    }

    public ShiftsPage shifts() {
        return shifts;
    }

    public BookingsPage bookings() {
        return bookings;
    }

    public void attachScreenshot(String name) {
        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
        } catch (RuntimeException exception) {
            Allure.addAttachment(name + " - capture error", "text/plain", exception.toString());
        }
    }

    @Override
    public void close() {
        driver.quit();
    }
}
