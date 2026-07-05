package com.taskflow.scheduler;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CronScheduleCalculatorTest {
    private final CronExpressionParser parser = new CronExpressionParser();
    private final CronScheduleCalculator calculator = new CronScheduleCalculator();

    @Test
    void computesDailyTwoAm() {
        ZonedDateTime now = ZonedDateTime.of(2026, 7, 5, 1, 30, 15, 0, ZoneId.of("UTC"));

        ZonedDateTime next = calculator.nextFireTime(parser.parse("0 2 * * *"), now);

        assertEquals(ZonedDateTime.of(2026, 7, 5, 2, 0, 0, 0, ZoneId.of("UTC")), next);
    }

    @Test
    void computesNextQuarterHour() {
        ZonedDateTime now = ZonedDateTime.of(2026, 7, 5, 10, 7, 0, 0, ZoneId.of("UTC"));

        ZonedDateTime next = calculator.nextFireTime(parser.parse("*/15 * * * *"), now);

        assertEquals(ZonedDateTime.of(2026, 7, 5, 10, 15, 0, 0, ZoneId.of("UTC")), next);
    }

    @Test
    void computesWeekdayMorning() {
        ZonedDateTime sunday = ZonedDateTime.of(2026, 7, 5, 10, 0, 0, 0, ZoneId.of("UTC"));

        ZonedDateTime next = calculator.nextFireTime(parser.parse("0 9 * * 1-5"), sunday);

        assertEquals(ZonedDateTime.of(2026, 7, 6, 9, 0, 0, 0, ZoneId.of("UTC")), next);
    }
}
