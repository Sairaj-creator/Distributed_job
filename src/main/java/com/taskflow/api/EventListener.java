package com.taskflow.api;

/**
 * Generic observer contract for TaskFlow events.
 *
 * @param <T> event type
 */
@FunctionalInterface
public interface EventListener<T> {
    /**
     * Handles an event.
     *
     * @param event event payload
     */
    void onEvent(T event);
}
