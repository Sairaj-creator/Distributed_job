package com.taskflow.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value object for workflow identifiers.
 */
public final class WorkflowId implements Comparable<WorkflowId>, Serializable {
    private static final long serialVersionUID = 1L;
    private final String value;

    private WorkflowId(String value) {
        String trimmed = Objects.requireNonNull(value, "value").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("workflow id cannot be blank");
        }
        this.value = trimmed;
    }

    public static WorkflowId of(String value) {
        return new WorkflowId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public int compareTo(WorkflowId other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WorkflowId other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
