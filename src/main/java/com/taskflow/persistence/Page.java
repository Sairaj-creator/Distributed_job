package com.taskflow.persistence;

/**
 * Pagination request.
 */
public record Page(int offset, int limit) {
    public Page {
        if (offset < 0) {
            throw new IllegalArgumentException("offset cannot be negative");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
    }

    public static Page first(int limit) {
        return new Page(0, limit);
    }
}
