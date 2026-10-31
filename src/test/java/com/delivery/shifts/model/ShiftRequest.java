package com.delivery.shifts.model;

import java.time.Instant;

public record ShiftRequest(String zone, Instant startTime, Instant endTime, int capacity) {
}
