package com.taskflow.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener that writes lifecycle changes through SLF4J/Logback.
 */
public final class FileEventListener extends AbstractEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileEventListener.class);

    @Override
    protected void handle(JobStatusEvent event) {
        if (event.error() == null) {
            LOGGER.info("job={} workflow={} attempt={} {} -> {} {}",
                    event.jobId(), event.workflowId(), event.attemptNumber(),
                    event.oldStatus(), event.newStatus(), event.message());
        } else {
            LOGGER.warn("job={} workflow={} attempt={} {} -> {} {}",
                    event.jobId(), event.workflowId(), event.attemptNumber(),
                    event.oldStatus(), event.newStatus(), event.message(), event.error());
        }
    }
}
