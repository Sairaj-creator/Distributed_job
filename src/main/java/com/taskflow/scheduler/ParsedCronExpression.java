package com.taskflow.scheduler;

import java.util.Set;

/**
 * Parsed five-field cron expression.
 */
public record ParsedCronExpression(
        Set<Integer> minutes,
        Set<Integer> hours,
        Set<Integer> daysOfMonth,
        Set<Integer> months,
        Set<Integer> daysOfWeek) {
}
