package com.rezolve.sdk_sample.utils;

import java.util.Calendar;

public final class DateUtils {

    public static long getCurrentTimestampInSeconds() {
        Calendar date = Calendar.getInstance();
        return date.getTimeInMillis() / 1000;
    }

    public static long addMinutesToTimestamp(long timestamp, int minutes) {
        return timestamp + minutes * 60;
    }
}
