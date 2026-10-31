package com.delivery.shifts.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.qameta.allure.Allure;

public class BookingsPage extends BasePage {
    private static final By TABLE_ROWS = By.cssSelector("#bookings-table tbody tr");
    private static final By STATUS_CELL = By.cssSelector("td:nth-child(6)");
    private static final By CANCEL_BUTTON = By.cssSelector("[data-action='cancel']");
    private static final By NOTIFICATION = By.id("notification");

    public BookingsPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void open() {
        Allure.step("Open the Bookings tab", () -> clickTab("tab-bookings"));
    }

    public String status(String courierName) {
        return wait.until(webDriver -> {
            try {
                for (WebElement row : webDriver.findElements(TABLE_ROWS)) {
                    if (row.getText().contains(courierName)) {
                        return row.findElement(STATUS_CELL).getText();
                    }
                }
            } catch (StaleElementReferenceException ignored) {
                return null;
            }
            return null;
        });
    }

    public void waitForStatus(String courierName, String expectedStatus) {
        wait.until(webDriver -> {
            try {
                for (WebElement row : webDriver.findElements(TABLE_ROWS)) {
                    if (row.getText().contains(courierName)
                            && row.findElement(STATUS_CELL).getText().equals(expectedStatus)) {
                        return true;
                    }
                }
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
            return false;
        });
    }

    public void cancel(String courierName) {
        Allure.step("Locate the cancelled booking action for " + courierName, () -> {});
        WebElement button = wait.until(webDriver -> {
            try {
                for (WebElement row : webDriver.findElements(TABLE_ROWS)) {
                    if (row.getText().contains(courierName)) {
                        return row.findElement(CANCEL_BUTTON);
                    }
                }
            } catch (StaleElementReferenceException ignored) {
                return null;
            }
            return null;
        });
        javascript("arguments[0].click()", button);
        Allure.step("Confirm the browser cancellation alert");
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(NOTIFICATION, "Booking cancelled"));
        attachScreenshot("Booking cancelled - " + courierName);
    }
}
