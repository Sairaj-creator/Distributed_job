package com.taskflow.api;

import java.util.Objects;

/**
 * Result returned by a completed job.
 */
public final class JobResult {
    private final boolean success;
    private final String outputSummary;

    private JobResult(boolean success, String outputSummary) {
        this.success = success;
        this.outputSummary = outputSummary == null ? "" : outputSummary;
    }

    /**
     * Creates a successful result.
     *
     * @param outputSummary human-readable output
     * @return success result
     */
    public static JobResult success(String outputSummary) {
        return new JobResult(true, outputSummary);
    }

    /**
     * Creates a failed result without throwing.
     *
     * @param outputSummary failure summary
     * @return failed result
     */
    public static JobResult failure(String outputSummary) {
        return new JobResult(false, outputSummary);
    }

    public boolean isSuccess() {
        return success;
    }

    public String outputSummary() {
        return outputSummary;
    }

    @Override
    public String toString() {
        return "JobResult{success=" + success + ", outputSummary='" + outputSummary + "'}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JobResult other)) {
            return false;
        }
        return success == other.success && Objects.equals(outputSummary, other.outputSummary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, outputSummary);
    }
}
