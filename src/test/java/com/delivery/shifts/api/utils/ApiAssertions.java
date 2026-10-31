package com.delivery.shifts.api.utils;

import io.restassured.response.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ApiAssertions {
    private ApiAssertions() {
    }

    public static void assertStatus(Response response, int expectedStatus) {
        assertEquals(expectedStatus, response.statusCode(),
                "Unexpected HTTP status. Response message: " + ApiResponseUtils.message(response));
    }

    public static void assertError(Response response, int expectedStatus, String messagePart) {
        assertStatus(response, expectedStatus);
        assertTrue(ApiResponseUtils.message(response).toLowerCase().contains(messagePart.toLowerCase()),
                "Expected message to contain '<" + messagePart + ">' but was: " + ApiResponseUtils.message(response));
    }

    public static void assertCompletesWithin(Response response, long maxMillis) {
        assertTrue(response.time() < maxMillis,
                "Request took " + response.time() + " ms; maximum allowed is " + maxMillis + " ms");
    }
}
