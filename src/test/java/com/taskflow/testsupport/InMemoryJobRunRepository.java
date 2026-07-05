package com.taskflow.testsupport;

import com.taskflow.core.JobRun;
import com.taskflow.persistence.JobRunRepository;
import com.taskflow.persistence.Page;
import com.taskflow.persistence.PageResult;
import com.taskflow.persistence.RunQuery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryJobRunRepository implements JobRunRepository {
    private final AtomicLong ids = new AtomicLong(1);
    private final List<JobRun> runs = new CopyOnWriteArrayList<>();

    @Override
    public Optional<JobRun> findById(Long id) {
        return runs.stream().filter(run -> id.equals(run.runId())).findFirst();
    }

    @Override
    public JobRun save(JobRun entity) {
        JobRun withId = entity.runId() == null ? entity.withRunId(ids.getAndIncrement()) : entity;
        runs.removeIf(run -> run.runId().equals(withId.runId()));
        runs.add(withId);
        return withId;
    }

    @Override
    public List<JobRun> findAll() {
        return new ArrayList<>(runs);
    }

    @Override
    public PageResult<JobRun> findRuns(RunQuery query, Page page) {
        List<JobRun> filtered = runs.stream()
                .filter(run -> query.jobId().map(id -> id.equals(run.jobId())).orElse(true))
                .filter(run -> query.status().map(status -> status == run.status()).orElse(true))
                .filter(run -> query.startedFrom().map(from -> run.startedAt() != null && !run.startedAt().isBefore(from)).orElse(true))
                .filter(run -> query.startedTo().map(to -> run.startedAt() != null && !run.startedAt().isAfter(to)).orElse(true))
                .sorted(Comparator.comparing(JobRun::startedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        int to = Math.min(filtered.size(), page.offset() + page.limit());
        List<JobRun> items = page.offset() >= filtered.size() ? List.of() : filtered.subList(page.offset(), to);
        return new PageResult<>(items, page.offset(), page.limit(), filtered.size());
    }
}
