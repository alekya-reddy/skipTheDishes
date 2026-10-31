package com.delivery.shifts.api.pages;

import com.delivery.shifts.config.ApiSpecifications;
import com.delivery.shifts.model.BookingRequest;
import com.delivery.shifts.model.BookingResponse;
import com.delivery.shifts.model.CourierRequest;
import com.delivery.shifts.model.CourierResponse;
import com.delivery.shifts.model.ShiftRequest;
import com.delivery.shifts.model.ShiftResponse;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

import java.util.List;

import static io.restassured.RestAssured.given;

public class ApiServicePage {
    public Response createCourierResponse(CourierRequest courier) {
        return given()
                .spec(ApiSpecifications.request())
                .body(courier)
                .when()
                .post("/couriers");
    }

    public CourierResponse createCourier(CourierRequest courier) {
        return createCourierResponse(courier)
                .then()
                .spec(ApiSpecifications.jsonResponse())
                .statusCode(201)
                .extract()
                .as(CourierResponse.class);
    }

    public Response createShiftResponse(ShiftRequest shift) {
        return given()
                .spec(ApiSpecifications.request())
                .body(shift)
                .when()
                .post("/shifts");
    }

    public ShiftResponse createShift(ShiftRequest shift) {
        return given()
                .spec(ApiSpecifications.request())
                .body(shift)
                .when()
                .post("/shifts")
                .then()
                .spec(ApiSpecifications.jsonResponse())
                .statusCode(201)
                .extract()
                .as(ShiftResponse.class);
    }

    public Response createBookingResponse(long courierId, long shiftId) {
        return given()
                .spec(ApiSpecifications.request())
                .body(new BookingRequest(courierId, shiftId))
                .when()
                .post("/bookings");
    }

    public BookingResponse createBooking(long courierId, long shiftId) {
        return createBookingResponse(courierId, shiftId)
                .then()
                .spec(ApiSpecifications.jsonResponse())
                .statusCode(201)
                .extract()
                .as(BookingResponse.class);
    }

    public BookingResponse getBooking(long bookingId) {
        return given()
                .spec(ApiSpecifications.request())
                .when()
                .get("/bookings/{id}", bookingId)
                .then()
                .spec(ApiSpecifications.jsonResponse())
                .statusCode(200)
                .extract()
                .as(BookingResponse.class);
    }

    public List<BookingResponse> getBookingsForCourier(long courierId) {
        return given()
                .spec(ApiSpecifications.request())
                .queryParam("courierId", courierId)
                .when()
                .get("/bookings")
                .then()
                .spec(ApiSpecifications.jsonResponse())
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public Response cancelBookingResponse(long bookingId) {
        return given()
                .spec(ApiSpecifications.request())
                .when()
                .post("/bookings/{id}/cancel", bookingId);
    }
}
