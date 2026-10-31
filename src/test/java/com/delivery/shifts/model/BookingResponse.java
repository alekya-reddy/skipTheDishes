package com.delivery.shifts.model;

import java.time.Instant;

public record BookingResponse(
        Long id,
        Long courierId,
        String courierName,
        Long shiftId,
        String zone,
        Instant shiftStart,
        Instant shiftEnd,
        String status,
        Instant createdAt,
        Instant cancelledAt
) {
}
