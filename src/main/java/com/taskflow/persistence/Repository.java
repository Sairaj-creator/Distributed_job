package com.taskflow.persistence;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository abstraction.
 *
 * @param <T> entity type
 * @param <ID> identifier type
 */
public interface Repository<T, ID> {
    Optional<T> findById(ID id);

    T save(T entity);

    List<T> findAll();
}
