package com.backbase.ct.bbfuel.dto.positivePay;

import org.apache.commons.lang.time.DateUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class IssueDateRange {

    public LocalDate generateRandomDate()
    {
        LocalDate from = LocalDate.of(2021, 02, 1);
        LocalDate to = LocalDate.now();
        long days = from.until(to.plusDays(10), ChronoUnit.DAYS);
        long randomDays = ThreadLocalRandom.current().nextLong(days + 1);
        LocalDate randomDate = from.plusDays(randomDays);;
        return randomDate;
    }
}
