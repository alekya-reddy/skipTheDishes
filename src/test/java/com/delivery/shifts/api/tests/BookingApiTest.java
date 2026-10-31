package com.delivery.shifts.api.tests;

import com.delivery.shifts.api.pages.ApiServicePage;
import com.delivery.shifts.api.utils.ApiAssertions;
import com.delivery.shifts.api.utils.ApiResponseUtils;

import com.delivery.shifts.data.TestData;
import com.delivery.shifts.model.BookingResponse;
import com.delivery.shifts.model.CourierRequest;
import com.delivery.shifts.model.CourierResponse;
import com.delivery.shifts.model.ShiftRequest;
import com.delivery.shifts.model.ShiftResponse;
import io.qameta.allure.Allure;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("api")
@Feature("Bookings API")
class BookingApiTest {
    private static final Logger LOG = LoggerFactory.getLogger(BookingApiTest.class);
    private static final long MAX_RESPONSE_TIME_MILLIS = 3_000;
    private final ApiServicePage api = new ApiServicePage();

    @Test
    @Story("Create a booking and retrieve it")
    void createsBookingAndReturnsItInBookingList() {
        String token = TestData.uniqueToken();
        CourierRequest courierRequest = TestData.courier(token);
        ShiftRequest shiftRequest = TestData.shift("NORTH");

        CourierResponse courier = api.createCourier(courierRequest);
        ShiftResponse shift = api.createShift(shiftRequest);

        LOG.info("Creating booking for courier {} and shift {}", courier.id(), shift.id());
        Response response = Allure.step("Create a booking using the REST API", () ->
                api.createBookingResponse(courier.id(), shift.id()));

        ApiAssertions.assertStatus(response, 201);
        ApiAssertions.assertCompletesWithin(response, MAX_RESPONSE_TIME_MILLIS);

        BookingResponse booking = ApiResponseUtils.body(response, BookingResponse.class);
        assertNotNull(booking.id());
        assertEquals(courier.id(), booking.courierId());
        assertEquals(courier.name(), booking.courierName());
        assertEquals(shift.id(), booking.shiftId());
        assertEquals("NORTH", booking.zone());
        assertEquals("CONFIRMED", booking.status());
        assertEquals(shift.startTime(), booking.shiftStart());

        BookingResponse persistedBooking = api.getBooking(booking.id());
        assertEquals(booking.id(), persistedBooking.id());
        assertEquals(booking.courierId(), persistedBooking.courierId());
        assertEquals("CONFIRMED", persistedBooking.status());

        List<BookingResponse> bookings = api.getBookingsForCourier(courier.id());

        assertTrue(bookings.stream().anyMatch(found -> found.id().equals(booking.id())
                        && found.status().equals("CONFIRMED")),
                "The created booking should appear in the courier's booking list");
    }
}
