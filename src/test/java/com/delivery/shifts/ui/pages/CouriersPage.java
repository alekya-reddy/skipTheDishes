package com.delivery.shifts.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.qameta.allure.Allure;

public class CouriersPage extends BasePage {
    private static final By NEW_COURIER_BUTTON = By.id("new-courier-btn");
    private static final By DIALOG = By.id("courier-dialog");
    private static final By NAME = By.id("courier-name");
    private static final By EMAIL = By.id("courier-email");
    private static final By PHONE = By.id("courier-phone");
    private static final By VEHICLE = By.id("courier-vehicle");
    private static final By SAVE_BUTTON = By.id("courier-save-btn");
    private static final By TABLE_BODY = By.cssSelector("#couriers-table tbody");
    private static final By TABLE_ROWS = By.cssSelector("#couriers-table tbody tr");
    private static final By NOTIFICATION = By.id("notification");

    public CouriersPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public void create(String name, String email, String phone, String vehicleType) {
        Allure.step("Open the Couriers tab", this::open);
        Allure.step("Click New courier and open the courier form", () -> {
            find(NEW_COURIER_BUTTON).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG));
        });
        Allure.step("Enter courier details: name=" + name + ", email=" + email
                + ", phone=" + phone + ", vehicle=" + vehicleType, () -> {
            find(NAME).sendKeys(name);
            find(EMAIL).sendKeys(email);
            find(PHONE).sendKeys(phone);
            new Select(find(VEHICLE)).selectByValue(vehicleType);
            attachScreenshot("Courier form - before save");
        });
        Allure.step("Save the courier", () -> find(SAVE_BUTTON).click());
        wait.until(ExpectedConditions.invisibilityOfElementLocated(DIALOG));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(TABLE_BODY, email));
        Allure.step("Verify the courier appears in the Couriers table with email " + email,
                () -> attachScreenshot("Courier created - " + email));
    }

    public void attemptDuplicate(String name, String email) {
        open();
        find(NEW_COURIER_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG));
        find(NAME).sendKeys(name);
        find(EMAIL).sendKeys(email);
        find(PHONE).sendKeys("+1-555-0199");
        new Select(find(VEHICLE)).selectByValue("BIKE");
        find(SAVE_BUTTON).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(NOTIFICATION, "email"));
    }

    public String attemptInvalidEmail(String name, String email) {
        open();
        find(NEW_COURIER_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG));
        find(NAME).sendKeys(name);
        find(EMAIL).sendKeys(email);
        find(PHONE).sendKeys("+1-555-0198");
        new Select(find(VEHICLE)).selectByValue("BIKE");
        find(SAVE_BUTTON).click();
        return (String) javascript(
                "return document.getElementById('courier-email').validationMessage || ''");
    }

    public void openCourierDialog(String email) {
        open();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(TABLE_BODY, email));
        WebElement row = wait.until(webDriver -> webDriver.findElements(TABLE_ROWS).stream()
                .filter(candidate -> candidate.getText().contains(email))
                .findFirst()
                .orElse(null));
        row.findElement(By.cssSelector("[data-action='edit']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG));
    }

    public void setActive(boolean active) {
        WebElement activeCheckbox = find(By.id("courier-active"));
        if (activeCheckbox.isSelected() != active) {
            activeCheckbox.click();
        }
        find(SAVE_BUTTON).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(DIALOG));
    }

    public boolean isVisible(String email) {
        javascript("var dialog = document.getElementById('courier-dialog'); if (dialog) dialog.close();");
        open();
        return wait.until(webDriver -> {
            try {
                return webDriver.findElements(TABLE_ROWS).stream()
                        .anyMatch(row -> row.getText().contains(email));
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
    }

    public String notificationText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(NOTIFICATION)).getText();
    }

    private void open() {
        clickTab("tab-couriers");
    }
}
