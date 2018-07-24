package com.doubleh.lumidiet.utils;

import java.util.Calendar;

/**
 * Created by user-pc on 2016-10-14.
 */

public class MyCalendarUtil {
    public static long[] getMonthInMillis(Calendar calendar, int monthAgo) {
        Calendar c = (Calendar) calendar.clone();

        long[] times = new long[2];

        if (monthAgo != 0) {
            c.add(Calendar.MONTH, -monthAgo);
        }
        c.add(Calendar.DAY_OF_MONTH, -c.get(Calendar.DAY_OF_MONTH) + 1);
        int lastDay = c.getActualMaximum(Calendar.DATE);

        // 해당 날짜의 지난 시간을 millisecond 단위로 환산
        long time = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000
                + c.get(Calendar.MINUTE) * 60 * 1000
                + c.get(Calendar.SECOND) * 1000;

        // 해당 날짜의 자정 시간과 한 달 이후의 시간
        times[0] = c.getTimeInMillis() - time;
        times[1] = times[0] + ((long)lastDay * 24 * 60 * 60 * 1000);

        return times;
    }

    /**
     * 원하는 주 일요일의(시작 요일) 12시 정각 시간을 구하는 함수
     * @param weeksAgo    몇 주 전인지(0~)
     * @return  millisecond 단위
     */
    public static long getWeekOfFirstDayInMillis(int weeksAgo) {
        Calendar c = Calendar.getInstance();
        if (weeksAgo != 0) {
            // 몇 주 전으로 setting
            c.add(Calendar.WEEK_OF_YEAR, -weeksAgo);
        }
        // 일요일로 setting
        c.add(Calendar.DAY_OF_MONTH, -(c.get(Calendar.DAY_OF_WEEK) - 1));

        // 해당 날짜의 지난 시간을 millisecond 단위로 환산
        long time = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000
                + c.get(Calendar.MINUTE) * 60 * 1000
                + c.get(Calendar.SECOND) * 1000;
        // 자정 값 return
        return c.getTimeInMillis() - time;
    }

    public static long[] getWeekOfFirstDayInMillis(Calendar calendar, int weeksAgo) {
        long[] times = new long[2];

        Calendar c = (Calendar) calendar.clone();
        if (weeksAgo != 0) {
            // 몇 주 전으로 setting
            c.add(Calendar.WEEK_OF_YEAR, -weeksAgo);
        }
        // 일요일로 setting
        c.add(Calendar.DAY_OF_MONTH, -(c.get(Calendar.DAY_OF_WEEK) - 1));

        // 해당 날짜의 지난 시간을 millisecond 단위로 환산
        long time = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000
                + c.get(Calendar.MINUTE) * 60 * 1000
                + c.get(Calendar.SECOND) * 1000;

        // 자정의 시간과 일주일 뒤의 시간 return
        times[0] = c.getTimeInMillis() - time;
        times[1] = times[0] + (7 * 24 * 60 * 60 * 1000);

        return times;
    }

    public static long[] getDayInMillis(int daysAgo) {
        long[] times = new long[2];

        Calendar c = Calendar.getInstance();
        if (daysAgo != 0) {
            // 며칠 전으로 setting
            c.add(Calendar.DAY_OF_MONTH, -daysAgo);
        }

        // 해당 날짜의 지난 시간을 millisecond 단위로 환산
        long time = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000
                + c.get(Calendar.MINUTE) * 60 * 1000
                + c.get(Calendar.SECOND) * 1000;

        // 해당 날짜의 자정 시간과 24시간이 지난 시간을 설정
        times[0] = c.getTimeInMillis() - time;
        times[1] = times[0] + 24 * 60 * 60 * 1000;

        // 해당 날짜 자정부터 24시간 후의 시간까지 return
        return times;
    }

    public static long[] getDayInMillis(Calendar calendar, int daysAgo) {

        Calendar c = (Calendar) calendar.clone();

        long[] times = new long[2];

        if (daysAgo != 0) {
            // 며칠 전으로 setting
            c.add(Calendar.DAY_OF_MONTH, -daysAgo);
        }

        // 해당 날짜의 지난 시간을 millisecond 단위로 환산
        long time = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000
                + c.get(Calendar.MINUTE) * 60 * 1000
                + c.get(Calendar.SECOND) * 1000;

        // 해당 날짜의 자정 시간과 24시간이 지난 시간을 설정
        times[0] = c.getTimeInMillis() - time;
        times[1] = times[0] + 24 * 60 * 60 * 1000;

        // 해당 날짜 자정부터 24시간 후의 시간까지 return
        return times;
    }
}
