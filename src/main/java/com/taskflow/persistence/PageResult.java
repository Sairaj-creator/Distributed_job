package com.taskflow.persistence;

import java.util.List;

/**
 * Pagination result.
 */
public record PageResult<T>(List<T> items, int offset, int limit, long total) {
}
