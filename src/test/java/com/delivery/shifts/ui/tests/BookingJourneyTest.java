package com.delivery.shifts.ui.tests;

import com.delivery.shifts.data.TestData;
import com.delivery.shifts.ui.pages.CourierShiftsPage;
import io.qameta.allure.Allure;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("ui")
@Feature("Booking user journey")
public class BookingJourneyTest {
    private CourierShiftsPage page;

    @BeforeEach
    void openApplication() {
        page = new CourierShiftsPage();
    }

    @AfterEach
    void closeBrowser(TestInfo testInfo) {
        if (page != null) {
            try {
                page.attachScreenshot(testInfo.getDisplayName() + " - final screenshot");
            } finally {
                page.close();
            }
        }
    }

    @Test
    @Story("Create courier, create shift, book the shift, and cancel it")
    void courierCanBookAndCancelANewShift() {
        String token = TestData.uniqueToken();
        String courierName = "QA Courier " + token;
        String courierEmail = "qa-" + token + "@example.test";

        Allure.step("Create a new active courier", () ->
                page.couriers().create(courierName, courierEmail, "+1-555-0188", "BIKE"));

        LocalDateTime start = LocalDateTime.now().plusDays(90)
                .withHour(12).withMinute(0).withSecond(0).withNano(0);
        long shiftId = Allure.step("Create an upcoming shift", () ->
                page.shifts().create("NORTH", start, 4, 1));

        Allure.step("Book the shift for the courier", () ->
                page.shifts().book(shiftId, courierName, "BIKE"));
        page.bookings().open();
        page.bookings().waitForStatus(courierName, "CONFIRMED");
        assertEquals("CONFIRMED", page.bookings().status(courierName));

        Allure.step("Cancel the booking", () -> page.bookings().cancel(courierName));
        page.bookings().waitForStatus(courierName, "CANCELLED");
        assertEquals("CANCELLED", page.bookings().status(courierName));
    }

    @Test
    @Story("Filter shifts by delivery zone")
    void userCanFilterShiftsByZone() {
        LocalDateTime northStart = LocalDateTime.now().plusDays(45)
                .withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime eastStart = LocalDateTime.now().plusDays(46)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);

        page.shifts().create("NORTH", northStart, 3, 5);
        page.shifts().create("EAST", eastStart, 3, 5);

        page.shifts().filterByZone("NORTH");
        assertTrue(page.shifts().visibleCount() > 0, "The NORTH zone should show at least one shift");
    }

    @Test
    @Story("Reject duplicate courier email in the UI")
    void uiRejectsDuplicateCourierEmail() {
        String token = TestData.uniqueToken();
        String email = "qa-duplicate-" + token + "@example.test";
        page.couriers().create("QA Original " + token, email, "+1-555-0180", "BIKE");

        page.couriers().attemptDuplicate("QA Duplicate " + token, email);
        assertTrue(page.couriers().notificationText().toLowerCase().contains("email"));
    }

    @Test
    @Story("Create and display a courier in the couriers table")
    void userCanCreateAndSeeCourier() {
        String token = TestData.uniqueToken();
        String email = "qa-created-" + token + "@example.test";

        page.couriers().create("QA Visible " + token, email, "+1-555-0177", "CAR");

        assertTrue(page.couriers().isVisible(email), "The created courier should appear in the couriers table");
    }

    @Test
    @Story("Disable booking when the shift capacity is reached")
    void fullShiftCannotAcceptAnotherBooking() {
        String token = TestData.uniqueToken();
        String firstCourier = "QA Full One " + token;
        String secondCourier = "QA Full Two " + token;
        page.couriers().create(firstCourier, "qa-full-one-" + token + "@example.test", "+1-555-0166", "BIKE");
        page.couriers().create(secondCourier, "qa-full-two-" + token + "@example.test", "+1-555-0165", "BIKE");

        LocalDateTime start = LocalDateTime.now().plusDays(75)
                .withHour(13).withMinute(0).withSecond(0).withNano(0);
        long shiftId = page.shifts().create("SOUTH", start, 3, 1);

        page.shifts().book(shiftId, firstCourier, "BIKE");

        assertTrue(page.shifts().bookingDisabled(shiftId), "A full shift should disable or remove its book action");
    }

    @Test
    @Story("Reset the shift zone filter to all zones")
    void userCanResetZoneFilter() {
        LocalDateTime northStart = LocalDateTime.now().plusDays(55)
                .withHour(8).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime eastStart = LocalDateTime.now().plusDays(56)
                .withHour(9).withMinute(0).withSecond(0).withNano(0);
        page.shifts().create("NORTH", northStart, 2, 5);
        page.shifts().create("EAST", eastStart, 2, 5);

        page.shifts().filterByZone("NORTH");
        assertTrue(page.shifts().visibleCount() > 0, "The NORTH filter should show at least one shift");

        page.shifts().filterByZone("");
        assertTrue(page.shifts().visibleCount() >= 2, "Resetting the zone filter should restore shifts from all zones");
    }

    @Test
    @Story("Deactivate an existing courier")
    void userCanDeactivateCourier() {
        String token = TestData.uniqueToken();
        String email = "qa-deactivate-" + token + "@example.test";
        String courierName = "QA Deactivate " + token;
        page.couriers().create(courierName, email, "+1-555-0155", "CAR");

        page.couriers().openCourierDialog(email);
        page.couriers().setActive(false);

        assertTrue(page.couriers().notificationText().toLowerCase().contains("updated"));
    }

    @Test
    @Story("Reject an invalid courier email format")
    void uiRejectsInvalidCourierEmailFormat() {
        String validationMessage = page.couriers().attemptInvalidEmail(
                "QA Invalid Email", "not-a-valid-email");
        assertTrue(validationMessage.toLowerCase().contains("email"),
                "The browser should validate the courier email format");
    }

    @Test
    @Story("Reject a shift capacity outside the allowed range")
    void uiRejectsInvalidShiftCapacity() {
        LocalDateTime start = LocalDateTime.now().plusDays(65)
                .withHour(11).withMinute(0).withSecond(0).withNano(0);
        String validationMessage = page.shifts().attemptInvalidCapacity(start, 0);
        assertFalse(validationMessage.isBlank(),
                "The browser should reject capacity less than 1");
    }

    @Test
    @Story("Reject a shift whose end time is before its start time")
    void uiRejectsShiftEndingBeforeItStarts() {
        LocalDateTime start = LocalDateTime.now().plusDays(66)
                .withHour(15).withMinute(0).withSecond(0).withNano(0);
        assertTrue(page.shifts().attemptShiftBeforeStart(start, 2, 3),
                "The shift dialog should remain open and report an invalid time range");
    }
}
