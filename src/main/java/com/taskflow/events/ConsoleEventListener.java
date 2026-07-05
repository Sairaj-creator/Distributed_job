package com.taskflow.events;

/**
 * Listener that prints concise lifecycle changes to stdout for CLI demos.
 */
public final class ConsoleEventListener extends AbstractEventListener {
    @Override
    protected void handle(JobStatusEvent event) {
        System.out.printf(
                "%s job=%s workflow=%s attempt=%d %s -> %s %s%n",
                event.timestamp(),
                event.jobId(),
                event.workflowId(),
                event.attemptNumber(),
                event.oldStatus(),
                event.newStatus(),
                event.message() == null ? "" : event.message());
    }
}
