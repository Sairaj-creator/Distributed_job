package com.taskflow.exception;

/**
 * Thrown when a cron expression cannot be parsed or has no possible next fire time.
 */
public class InvalidCronExpressionException extends TaskFlowException {
    public InvalidCronExpressionException(String message) {
        super(message);
    }
}
