package com.delivery.shifts.api.utils;

import io.restassured.response.Response;

public final class ApiResponseUtils {
    private ApiResponseUtils() {
    }

    public static String message(Response response) {
        String message = response.jsonPath().getString("message");
        return message == null || message.isBlank() ? "(no response message)" : message;
    }

    public static String stringField(Response response, String fieldName) {
        return response.jsonPath().getString(fieldName);
    }

    public static <T> T body(Response response, Class<T> responseType) {
        return response.as(responseType);
    }
}
