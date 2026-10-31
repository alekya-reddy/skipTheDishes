package com.delivery.shifts.model;

public record CourierRequest(String name, String email, String phone, String vehicleType, boolean active) {
}
