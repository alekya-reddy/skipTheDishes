package com.delivery.shifts.data;

import com.delivery.shifts.config.TimeUtils;
import com.delivery.shifts.model.CourierRequest;
import com.delivery.shifts.model.ShiftRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public final class TestData {
    private TestData() {
    }

    public static String uniqueToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static CourierRequest courier(String token) {
        return courier(token, true);
    }

    public static CourierRequest courier(String token, boolean active) {
        return new CourierRequest(
                "QA Courier " + token,
                "qa-" + token + "@example.test",
                "+1-555-0100",
                "BIKE",
                active
        );
    }

    public static CourierRequest courier(String token, boolean active, String vehicleType) {
        return new CourierRequest(
                "QA Courier " + token,
                email(token),
                "+1-555-0100",
                vehicleType,
                active
        );
    }

    public static String email(String token) {
        return "qa-" + token + "@example.test";
    }

    public static ShiftRequest shift(String zone) {
        return shift(zone, TimeUtils.futureInstant(1), 4, 1);
    }

    public static ShiftRequest shift(String zone, Instant start, int durationHours, int capacity) {
        return new ShiftRequest(zone, start, start.plus(durationHours, ChronoUnit.HOURS), capacity);
    }
}
