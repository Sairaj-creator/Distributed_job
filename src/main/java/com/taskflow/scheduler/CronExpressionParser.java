package com.taskflow.scheduler;

import com.taskflow.exception.InvalidCronExpressionException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Parser for simplified 5-field cron expressions supporting *, ranges, steps, and comma lists.
 */
public final class CronExpressionParser {
    private static final int FIELD_COUNT = 5;

    /**
     * Parses a cron expression.
     *
     * @param expression expression such as {@code 0 2 * * *}
     * @return parsed expression
     */
    public ParsedCronExpression parse(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new InvalidCronExpressionException("cron expression cannot be blank");
        }
        String[] fields = expression.trim().split("\\s+");
        if (fields.length != FIELD_COUNT) {
            throw new InvalidCronExpressionException("cron expression must contain 5 fields: " + expression);
        }
        return new ParsedCronExpression(
                parseField(fields[0], 0, 59, false, "minute"),
                parseField(fields[1], 0, 23, false, "hour"),
                parseField(fields[2], 1, 31, false, "day-of-month"),
                parseField(fields[3], 1, 12, false, "month"),
                parseField(fields[4], 0, 7, true, "day-of-week"));
    }

    private Set<Integer> parseField(String field, int min, int max, boolean normalizeSunday, String name) {
        Set<Integer> values = new TreeSet<>();
        for (String part : field.split(",")) {
            parsePart(part.trim(), min, max, normalizeSunday, name, values);
        }
        if (values.isEmpty()) {
            throw new InvalidCronExpressionException("cron field '" + name + "' produced no values");
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(values));
    }

    private void parsePart(
            String part,
            int min,
            int max,
            boolean normalizeSunday,
            String name,
            Set<Integer> values) {
        if (part.isEmpty()) {
            throw new InvalidCronExpressionException("empty cron field component in " + name);
        }
        String rangePart = part;
        int step = 1;
        if (part.contains("/")) {
            String[] pieces = part.split("/");
            if (pieces.length != 2) {
                throw new InvalidCronExpressionException("invalid step syntax in " + name + ": " + part);
            }
            rangePart = pieces[0];
            step = parseNumber(pieces[1], min, max, name);
            if (step <= 0) {
                throw new InvalidCronExpressionException("step must be positive in " + name + ": " + part);
            }
        }

        int start;
        int end;
        if ("*".equals(rangePart)) {
            start = min;
            end = max;
        } else if (rangePart.contains("-")) {
            String[] bounds = rangePart.split("-");
            if (bounds.length != 2) {
                throw new InvalidCronExpressionException("invalid range in " + name + ": " + part);
            }
            start = parseNumber(bounds[0], min, max, name);
            end = parseNumber(bounds[1], min, max, name);
        } else {
            start = parseNumber(rangePart, min, max, name);
            end = start;
        }
        if (start > end) {
            throw new InvalidCronExpressionException("range start greater than end in " + name + ": " + part);
        }
        for (int value = start; value <= end; value += step) {
            values.add(normalizeSunday && value == 7 ? 0 : value);
        }
    }

    private int parseNumber(String raw, int min, int max, String name) {
        try {
            int value = Integer.parseInt(raw);
            if (value < min || value > max) {
                throw new InvalidCronExpressionException("value out of range for " + name + ": " + value);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new InvalidCronExpressionException("invalid number for " + name + ": " + raw);
        }
    }
}
