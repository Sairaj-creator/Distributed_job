package com.taskflow.scheduler;

import com.taskflow.exception.InvalidCronExpressionException;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Computes the next fire time for parsed cron expressions with a bounded minute-by-minute search.
 */
public final class CronScheduleCalculator {
    private static final int MAX_MINUTES_LOOKAHEAD = 4 * 366 * 24 * 60;

    /**
     * Computes the next matching time after {@code now}.
     *
     * @param expression parsed cron expression
     * @param now current time
     * @return next matching zoned date-time
     */
    public ZonedDateTime nextFireTime(ParsedCronExpression expression, ZonedDateTime now) {
        ZonedDateTime candidate = now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
        for (int i = 0; i < MAX_MINUTES_LOOKAHEAD; i++) {
            if (matches(expression, candidate)) {
                return candidate;
            }
            candidate = candidate.plusMinutes(1);
        }
        throw new InvalidCronExpressionException("no cron fire time found within four years");
    }

    private boolean matches(ParsedCronExpression expression, ZonedDateTime candidate) {
        int dayOfWeek = candidate.getDayOfWeek().getValue() % 7;
        return expression.minutes().contains(candidate.getMinute())
                && expression.hours().contains(candidate.getHour())
                && expression.daysOfMonth().contains(candidate.getDayOfMonth())
                && expression.months().contains(candidate.getMonthValue())
                && expression.daysOfWeek().contains(dayOfWeek);
    }
}
