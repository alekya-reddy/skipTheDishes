package com.delivery.shifts.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class TimeUtils {
    private TimeUtils() {
    }

    public static Instant futureInstant(int days) {
        return futureInstant(days, 0);
    }

    public static Instant futureInstant(int days, int hours) {
        return Instant.now()
                .plus(days, ChronoUnit.DAYS)
                .plus(hours, ChronoUnit.HOURS)
                .truncatedTo(ChronoUnit.SECONDS);
    }

    public static Instant pastInstant(int days) {
        return Instant.now()
                .minus(days, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.SECONDS);
    }
}
