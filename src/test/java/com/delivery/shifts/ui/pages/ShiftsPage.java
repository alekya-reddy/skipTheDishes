package com.delivery.shifts.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.qameta.allure.Allure;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShiftsPage extends BasePage {
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private static final By TABLE_ROWS = By.cssSelector("#shifts-table tbody tr");
    private static final By NOTIFICATION = By.id("notification");
    private static final By NEW_SHIFT_BUTTON = By.id("new-shift-btn");
    private static final By DIALOG = By.id("shift-dialog");
    private static final By ZONE = By.id("shift-zone");
    private static final By START = By.id("shift-start");
    private static final By END = By.id("shift-end");
    private static final By CAPACITY = By.id("shift-capacity");
    private static final By SAVE_BUTTON = By.id("shift-save-btn");
    private static final By ZONE_FILTER = By.id("shift-zone-filter");
    private static final By BOOK_DIALOG = By.id("book-dialog");
    private static final By BOOK_COURIER = By.id("book-courier");
    private static final By BOOK_CONFIRM = By.id("book-confirm-btn");

    public ShiftsPage(WebDriver driver, WebDriverWait wait) {
        super(driver, wait);
    }

    public long create(String zone, LocalDateTime start, int durationHours, int capacity) {
        Allure.step("Open the Shifts tab", this::open);
        Allure.step("Click New shift and open the shift form", () -> {
            find(NEW_SHIFT_BUTTON).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG));
        });
        Allure.step("Enter shift details: zone=" + zone + ", start=" + start
                + ", durationHours=" + durationHours + ", capacity=" + capacity, () -> {
            new Select(find(ZONE)).selectByValue(zone);
            setDateTime(START, start.format(INPUT_FORMAT));
            setDateTime(END, start.plusHours(durationHours).format(INPUT_FORMAT));
            WebElement capacityField = find(CAPACITY);
            capacityField.clear();
            capacityField.sendKeys(Integer.toString(capacity));
            attachScreenshot("Shift form - before save");
        });
        Allure.step("Save the shift", () -> find(SAVE_BUTTON).click());

        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(DIALOG));
        } catch (RuntimeException failure) {
            throw new IllegalStateException("Shift dialog did not close after save. Notification: "
                    + notificationText(), failure);
        }

        String displayedStart = (String) javascript(
                "return new Date(arguments[0]).toLocaleString([], {dateStyle: 'medium', timeStyle: 'short'})",
                start.format(INPUT_FORMAT));
        WebElement row = wait.until(webDriver -> {
            WebElement newestMatch = null;
            long newestId = -1;
            for (WebElement candidate : webDriver.findElements(TABLE_ROWS)) {
                try {
                    String rowZone = candidate.findElement(By.cssSelector("td:nth-child(2)")).getText();
                    String rowStart = candidate.findElement(By.cssSelector("td:nth-child(3)")).getText();
                    if (rowZone.equals(zone) && rowStart.equals(displayedStart)) {
                        long candidateId = Long.parseLong(candidate.getDomAttribute("data-id"));
                        if (candidateId > newestId) {
                            newestId = candidateId;
                            newestMatch = candidate;
                        }
                    }
                } catch (StaleElementReferenceException | NumberFormatException ignored) {
                    // The table can refresh while a matching row is being read.
                }
            }
            return newestMatch;
        });
        long shiftId = Long.parseLong(row.getDomAttribute("data-id"));
        Allure.step("Verify the created shift row", () -> attachScreenshot("Shift created - id " + shiftId));
        return shiftId;
    }

    public void book(long shiftId, String courierName, String vehicleType) {
        Allure.step("Prepare to book shift id " + shiftId + " for courier "
                + courierName + " using vehicle " + vehicleType, () -> attachScreenshot("Before booking - shift " + shiftId));
        javascript("document.getElementById('shift-zone-filter').value = ''; if (typeof loadShifts === 'function') { loadShifts(); }");
        try {
            wait.until(webDriver -> {
                try {
                    WebElement dialog = webDriver.findElement(BOOK_DIALOG);
                    if (dialog.isDisplayed()) {
                        return true;
                    }
                    WebElement row = webDriver.findElement(By.cssSelector("#shifts-table tbody tr[data-id='" + shiftId + "']"));
                    WebElement button = row.findElement(By.cssSelector("[data-action='book']"));
                    if (!button.isEnabled()) {
                        return false;
                    }
                    javascript("arguments[0].click()", button);
                    return false;
                } catch (StaleElementReferenceException ignored) {
                    return false;
                } catch (org.openqa.selenium.NoSuchElementException ignored) {
                    javascript("if (typeof loadShifts === 'function') { loadShifts(); }");
                    return false;
                }
            });
        } catch (RuntimeException failure) {
            throw new IllegalStateException("Booking dialog did not open for shift " + shiftId
                    + ". Notification: " + notificationText(), failure);
        }
        new Select(find(BOOK_COURIER)).selectByVisibleText(courierName + " (" + vehicleType + ")");
        find(BOOK_CONFIRM).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(BOOK_DIALOG));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(NOTIFICATION, "Shift booked"));
    }

    public void filterByZone(String zone) {
        open();
        new Select(find(ZONE_FILTER)).selectByValue(zone);
        wait.until(webDriver -> {
            try {
                List<WebElement> rows = webDriver.findElements(TABLE_ROWS);
                if (rows.isEmpty()) {
                    return false;
                }
                if (zone.isBlank()) {
                    return true;
                }
                boolean allMatch = rows.stream().allMatch(row -> row.findElement(By.cssSelector("td:nth-child(2)")).getText().trim().equals(zone));
                if (!allMatch) {
                    // The app does not order overlapping loadShifts responses, so a stale unfiltered response can overwrite the filtered table.
                    javascript("if (typeof loadShifts === 'function') { loadShifts(); }");
                }
                return allMatch;
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
    }

    public int visibleCount() {
        return driver.findElements(TABLE_ROWS).size();
    }

    public boolean attemptShiftBeforeStart(LocalDateTime start, int durationHours, int capacity) {
        open();
        find(NEW_SHIFT_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG));
        new Select(find(ZONE)).selectByValue("WEST");
        setDateTime(START, start.format(INPUT_FORMAT));
        setDateTime(END, start.minusHours(durationHours).format(INPUT_FORMAT));
        WebElement capacityField = find(CAPACITY);
        capacityField.clear();
        capacityField.sendKeys(Integer.toString(capacity));
        find(SAVE_BUTTON).click();
        return find(DIALOG).isDisplayed();
    }

    public String attemptInvalidCapacity(LocalDateTime start, int capacity) {
        open();
        find(NEW_SHIFT_BUTTON).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG));
        new Select(find(ZONE)).selectByValue("WEST");
        setDateTime(START, start.format(INPUT_FORMAT));
        setDateTime(END, start.plusHours(3).format(INPUT_FORMAT));
        WebElement capacityField = find(CAPACITY);
        capacityField.clear();
        capacityField.sendKeys(Integer.toString(capacity));
        find(SAVE_BUTTON).click();
        return (String) javascript(
                "return document.getElementById('shift-capacity').validationMessage || ''");
    }

    public boolean bookingDisabled(long shiftId) {
        open();
        WebElement row = shiftRow(shiftId);
        List<WebElement> buttons = row.findElements(By.cssSelector("[data-action='book']"));
        return buttons.isEmpty() || !buttons.get(0).isEnabled();
    }

    private void open() {
        clickTab("tab-shifts");
    }

    private void setDateTime(By locator, String value) {
        javascript("arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input', {bubbles:true})); arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                find(locator), value);
    }

    private WebElement shiftRow(long shiftId) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#shifts-table tbody tr[data-id='" + shiftId + "']")));
    }

    private String notificationText() {
        return driver.findElements(NOTIFICATION).stream()
                .map(WebElement::getText)
                .filter(text -> !text.isBlank())
                .findFirst()
                .orElse("(no notification text)");
    }
}
