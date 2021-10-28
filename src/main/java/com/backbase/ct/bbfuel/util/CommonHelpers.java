package com.backbase.ct.bbfuel.util;

import static java.util.Arrays.asList;

import com.github.javafaker.CreditCardType;
import com.github.javafaker.Faker;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonHelpers {

    private static final List<String> VALID_BRANCH_CODES = asList(
        "122105155", "082000549", "121122676", "091300023", "121201694", "123123123", "307070115", "091000022"
    );

    private static final List<String> VALID_PROVIDERS = asList(
            "mastercard", "visa-white", "union-pay", "diners-club"
    );

    public static int generateRandomNumberInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        return ThreadLocalRandom.current().nextInt((max - min) + 1) + min;
    }

    public static BigDecimal generateRandomAmountInRange(long min, long max) {
        long clamp = max * 10 - min * 10;
        long value = Math.abs((ThreadLocalRandom.current().nextLong() % clamp));
        return new BigDecimal("" + ((value / 10D) + min)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static String generateRandomBranchCode() {
        return VALID_BRANCH_CODES.get(generateRandomNumberInRange(0, VALID_BRANCH_CODES.size() - 1));
    }

    public static LocalDate generateRandomDateInRange(LocalDate min, LocalDate max) {
        long days = min.until(max, ChronoUnit.DAYS);
        long randomDays = ThreadLocalRandom.current().nextLong(days + 1);
        return min.plusDays(randomDays);
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

    public static String generateRandomCardProvider() {
        return CreditCardType.values()[new Faker().random().nextInt(CreditCardType.values().length)]
            .name().replace("_", " ");
    }

    public static String generateRandomCardProviderFromList() {
        return VALID_PROVIDERS.get(generateRandomNumberInRange(0, VALID_PROVIDERS.size() - 1));
    }
}
