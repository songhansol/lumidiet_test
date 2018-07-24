package com.doubleh.lumidiet.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public abstract class NumberFormatUtil {

    public static String commaedNumber(long number) {
        return NumberFormat.getNumberInstance(Locale.US).format(number);
    }

    public static long integerFromCommaedNumber(String number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        long result = 0;
        try {
            result = decimalFormat.parse(number).longValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }
}
