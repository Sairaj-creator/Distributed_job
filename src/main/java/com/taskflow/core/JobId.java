package com.taskflow.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value object for job identifiers.
 */
public final class JobId implements Comparable<JobId>, Serializable {
    private static final long serialVersionUID = 1L;
    private final String value;

    private JobId(String value) {
        String trimmed = Objects.requireNonNull(value, "value").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("job id cannot be blank");
        }
        this.value = trimmed;
    }

    public static JobId of(String value) {
        return new JobId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public int compareTo(JobId other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JobId other && value.equals(other.value);
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
