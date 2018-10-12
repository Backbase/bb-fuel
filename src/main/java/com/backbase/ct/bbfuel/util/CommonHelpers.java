package com.backbase.ct.bbfuel.util;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang.StringUtils;

public class CommonHelpers {

    public static int generateRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        return ThreadLocalRandom.current().nextInt((max - min) + 1) + min;
    }

    public static BigDecimal generateRandomAmountInRange(long min, long max) {
        long clamp = max * 10 - min * 10;
        long value = Math.abs((ThreadLocalRandom.current().nextLong() % clamp));
        return new BigDecimal("" + ((value / 10D) + min)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static <T> T getRandomFromList(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
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

    public static <T extends Enum> T getRandomFromEnumValues(T[] values) {
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

}
