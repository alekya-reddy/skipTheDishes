package com.delivery.shifts.model;

import java.time.Instant;

public record ShiftResponse(
        Long id,
        String zone,
        Instant startTime,
        Instant endTime,
        int capacity,
        long bookedCount,
        long availableSlots,
        String status,
        Instant createdAt
) {
}
