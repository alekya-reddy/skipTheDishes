package com.delivery.shifts.model;

import java.time.Instant;

public record CourierResponse(
        Long id,
        String name,
        String email,
        String phone,
        String vehicleType,
        boolean active,
        Instant createdAt
) {
}
