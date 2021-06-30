package com.auth0.jwk;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class Util {
    static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    static void checkArgument(boolean arg, String message) {
        if (!arg) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
    }

    private static Map<TimeUnit, ChronoUnit> chronoByTimes = new HashMap<>();
    static {
        chronoByTimes.put(TimeUnit.NANOSECONDS, ChronoUnit.NANOS);
        chronoByTimes.put(TimeUnit.MICROSECONDS, ChronoUnit.MICROS);
        chronoByTimes.put(TimeUnit.MILLISECONDS, ChronoUnit.MILLIS);
        chronoByTimes.put(TimeUnit.SECONDS, ChronoUnit.SECONDS);
        chronoByTimes.put(TimeUnit.MINUTES, ChronoUnit.MINUTES);
        chronoByTimes.put(TimeUnit.HOURS, ChronoUnit.HOURS);
        chronoByTimes.put(TimeUnit.DAYS, ChronoUnit.DAYS);
    }

    // This method replaces the JDK 9 implementation TimeUnit.toChronoUnit
    static ChronoUnit toChronoUnit(TimeUnit unit) {
        ChronoUnit chrono = chronoByTimes.get(unit);
        if (chrono == null) {
            throw new IllegalArgumentException(String.format("Invalid TimeUnit %s", unit.toString()));
        }
        return chrono;
    }
}
