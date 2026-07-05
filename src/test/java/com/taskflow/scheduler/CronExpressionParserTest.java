package com.taskflow.scheduler;

import com.taskflow.exception.InvalidCronExpressionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CronExpressionParserTest {
    private final CronExpressionParser parser = new CronExpressionParser();

    @ParameterizedTest
    @ValueSource(strings = {"0 2 * * *", "*/15 * * * *", "0 9 * * 1-5", "0,30 8-10 */2 1,2 0"})
    void parsesValidExpressions(String expression) {
        ParsedCronExpression parsed = parser.parse(expression);

        assertTrue(parsed.minutes().size() >= 1);
        assertTrue(parsed.hours().size() >= 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "bad", "61 * * * *", "*/0 * * * *", "0 24 * * *", "0 1 31-1 * *"})
    void rejectsInvalidExpressions(String expression) {
        assertThrows(InvalidCronExpressionException.class, () -> parser.parse(expression));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0 0 * * 7"})
    void normalizesSevenAsSunday(String expression) {
        assertEquals(0, parser.parse(expression).daysOfWeek().iterator().next());
    }
}
