package com.taskflow.events;

import com.taskflow.api.EventListener;

import java.util.Objects;

/**
 * Template listener that validates an event before delegating to subclasses.
 */
public abstract class AbstractEventListener implements EventListener<JobStatusEvent> {
    @Override
    public final void onEvent(JobStatusEvent event) {
        handle(Objects.requireNonNull(event, "event"));
    }

    protected abstract void handle(JobStatusEvent event);
}
