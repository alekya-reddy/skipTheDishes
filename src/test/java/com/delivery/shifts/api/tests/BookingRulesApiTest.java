package com.delivery.shifts.api.tests;

import com.delivery.shifts.api.pages.ApiServicePage;
import com.delivery.shifts.api.utils.ApiAssertions;
import com.delivery.shifts.api.utils.ApiResponseUtils;

import com.delivery.shifts.data.TestData;
import com.delivery.shifts.model.CourierRequest;
import com.delivery.shifts.model.CourierResponse;
import com.delivery.shifts.model.ShiftRequest;
import com.delivery.shifts.model.ShiftResponse;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("api")
@Feature("Booking business rules")
class BookingRulesApiTest {
    private final ApiServicePage api = new ApiServicePage();

    @Test
    @Story("Enforce capacity, cancel a booking, and reuse the freed slot")
    void capacityIsEnforcedAndCancellationFreesTheSlot() {
        CourierResponse firstCourier = api.createCourier(TestData.courier(TestData.uniqueToken()));
        CourierResponse secondCourier = api.createCourier(TestData.courier(TestData.uniqueToken()));
        ShiftResponse shift = api.createShift(TestData.shift("EAST"));

        var firstBooking = api.createBooking(firstCourier.id(), shift.id());
        assertEquals("CONFIRMED", firstBooking.status());

        Response duplicateResponse = api.createBookingResponse(firstCourier.id(), shift.id());
        ApiAssertions.assertStatus(duplicateResponse, 409);

        Response fullShiftResponse = api.createBookingResponse(secondCourier.id(), shift.id());
        ApiAssertions.assertError(fullShiftResponse, 409, "full");

        Response cancelResponse = api.cancelBookingResponse(firstBooking.id());
        ApiAssertions.assertStatus(cancelResponse, 200);
        assertEquals("CANCELLED", ApiResponseUtils.stringField(cancelResponse, "status"));

        var replacementBooking = api.createBooking(secondCourier.id(), shift.id());
        assertEquals("CONFIRMED", replacementBooking.status());
        assertEquals(secondCourier.id(), replacementBooking.courierId());

        Response secondCancellation = api.cancelBookingResponse(firstBooking.id());
        ApiAssertions.assertStatus(secondCancellation, 409);
    }

    @Test
    @Story("Reject overlapping confirmed bookings")
    void overlappingBookingsAreRejected() {
        CourierResponse courier = api.createCourier(TestData.courier(TestData.uniqueToken()));
        Instant start = Instant.now().plus(10, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        ShiftResponse firstShift = api.createShift(new ShiftRequest("WEST", start, start.plus(4, ChronoUnit.HOURS), 5));
        ShiftResponse overlappingShift = api.createShift(new ShiftRequest("WEST",
                start.plus(2, ChronoUnit.HOURS), start.plus(6, ChronoUnit.HOURS), 5));

        api.createBooking(courier.id(), firstShift.id());
        Response overlapResponse = api.createBookingResponse(courier.id(), overlappingShift.id());

        ApiAssertions.assertError(overlapResponse, 409, "overlap");
    }

    @Test
    @Story("Reject bookings for inactive couriers")
    void inactiveCourierCannotBookAShift() {
        CourierResponse inactiveCourier = api.createCourier(TestData.courier(TestData.uniqueToken(), false));
        ShiftResponse shift = api.createShift(TestData.shift("SOUTH"));

        Response response = api.createBookingResponse(inactiveCourier.id(), shift.id());

        ApiAssertions.assertError(response, 409, "inactive");
    }

    @Test
    @Story("Reject duplicate courier emails")
    void duplicateCourierEmailIsRejected() {
        String token = TestData.uniqueToken();
        CourierRequest first = TestData.courier(token);
        api.createCourier(first);
        CourierRequest duplicate = new CourierRequest("Another Courier", first.email(), "+1-555-0101", "CAR", true);

        Response response = api.createCourierResponse(duplicate);

        ApiAssertions.assertError(response, 409, "email");
    }

    @Test
    @Story("Reject invalid shift capacity")
    void shiftCapacityMustBeWithinAllowedRange() {
        Instant start = Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        Response response = api.createShiftResponse(new ShiftRequest("NORTH", start, start.plus(2, ChronoUnit.HOURS), 51));

        ApiAssertions.assertStatus(response, 400);
    }

    @Test
    @Story("Reject shifts with an invalid time range")
    void shiftEndMustBeAfterShiftStart() {
        Instant start = Instant.now().plus(20, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        Response response = api.createShiftResponse(new ShiftRequest("WEST", start, start.minus(1, ChronoUnit.HOURS), 10));

        ApiAssertions.assertStatus(response, 400);
    }

    @Test
    @Story("Reject shifts in the past")
    void pastShiftCannotBeCreated() {
        Instant start = Instant.now().minus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        Response response = api.createShiftResponse(new ShiftRequest("SOUTH", start, start.plus(4, ChronoUnit.HOURS), 10));

        ApiAssertions.assertStatus(response, 400);
    }

    @Test
    @Story("Reject a booking with a missing shift")
    void bookingForUnknownShiftIsRejected() {
        CourierResponse courier = api.createCourier(TestData.courier(TestData.uniqueToken()));
        Response response = api.createBookingResponse(courier.id(), 9_999_999L);

        ApiAssertions.assertStatus(response, 404);
    }
}
