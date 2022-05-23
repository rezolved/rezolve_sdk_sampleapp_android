package com.rezolve.sdk_sample.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtils {

    private final static ThreadLocal<SimpleDateFormat> ISO_8601_24H_FULL_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            return dateFormat;
        }
    };

    private final static ThreadLocal<SimpleDateFormat> ISO_8601_24H_DATE_AND_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'", Locale.ROOT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            return dateFormat;
        }
    };

    private final static ThreadLocal<SimpleDateFormat> DATE_FORMAT_SSP_ACT_SUBMISSION_WITH_TIME = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd MMM yyyy HH:mm a", Locale.ROOT);
        }
    };

    public static long getCurrentTimestampInSeconds() {
        Calendar date = Calendar.getInstance();
        return date.getTimeInMillis() / 1000;
    }

    public static long addMinutesToTimestamp(long timestamp, int minutes) {
        return timestamp + minutes * 60;
    }

    // TODO: fix the issue and + handle all states in SspActBlockAdapter
    public static String getHumanReadableDateSspDefaultFormatWithTime(String timestamp) {
        String dateAndTime = getDateAndTime(
                timestamp,
                ISO_8601_24H_FULL_FORMAT,
                DATE_FORMAT_SSP_ACT_SUBMISSION_WITH_TIME
        );

        if(!dateAndTime.trim().isEmpty())
            return dateAndTime;
        else
            return getDateAndTime(timestamp, ISO_8601_24H_DATE_AND_TIME_FORMAT,
                DATE_FORMAT_SSP_ACT_SUBMISSION_WITH_TIME);
    }

    public static String getDateAndTime(String timestamp, ThreadLocal<SimpleDateFormat> sourceFormat,
                                        ThreadLocal<SimpleDateFormat> destFormat) {
        try {
            SimpleDateFormat simpleDateFormat = sourceFormat.get();

            if (simpleDateFormat != null) {
                Date date = simpleDateFormat.parse(timestamp);

                if (date != null) {
                    SimpleDateFormat simpleDestFormat = destFormat.get();

                    if (simpleDestFormat != null) {
                        return simpleDestFormat.format(date);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
