package com.backbase.ct.dataloader.util;

import java.math.BigDecimal;
import java.util.Random;
import org.apache.commons.lang.StringUtils;

public class CommonHelpers {

    public static int generateRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static BigDecimal generateRandomAmountInRange(long min, long max) {
        Random random = new Random();

        long clamp = max * 10 - min * 10;
        long value = Math.abs((random.nextLong() % clamp));
        return new BigDecimal("" + ((value / 10D) + min)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static long convertMinutesToMillis(long minutes) {
        return (minutes * 60L * 1000L);
    }


    /**
     * Split a string to lowercase, capitalize each word and divide them by a space.
     */
    public static String splitDelimitedWordToSingleCapatilizedWords(String value, String separatorChars) {
        String[] strings = StringUtils.split(value.toLowerCase(), separatorChars);
        for (int i = 0; i < strings.length; i++) {
            strings[i] = StringUtils.capitalize(strings[i]);
        }
        return StringUtils.join(strings, " ");
    }
}
