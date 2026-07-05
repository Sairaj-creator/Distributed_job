package com.taskflow.events;

import com.taskflow.api.EventListener;
import com.taskflow.concurrency.NamedThreadFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Asynchronous event bus backed by one dispatch thread and a blocking queue.
 */
public final class EventBus implements AutoCloseable {
    private final LinkedBlockingQueue<JobStatusEvent> queue = new LinkedBlockingQueue<>();
    private final List<EventListener<JobStatusEvent>> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Thread dispatcher;

    public EventBus() {
        this.dispatcher = new NamedThreadFactory("taskflow-events").newThread(this::dispatchLoop);
        this.dispatcher.start();
    }

    public void register(EventListener<JobStatusEvent> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void publish(JobStatusEvent event) {
        if (running.get()) {
            queue.offer(Objects.requireNonNull(event, "event"));
        }
    }

    private void dispatchLoop() {
        while (running.get() || !queue.isEmpty()) {
            try {
                JobStatusEvent event = queue.poll(100, TimeUnit.MILLISECONDS);
                if (event != null) {
                    for (EventListener<JobStatusEvent> listener : listeners) {
                        listener.onEvent(event);
                    }
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void close() {
        running.set(false);
        dispatcher.interrupt();
    }
}
