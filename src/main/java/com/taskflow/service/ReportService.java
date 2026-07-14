package com.taskflow.service;

import com.taskflow.core.JobId;
import com.taskflow.core.JobRun;
import com.taskflow.core.JobStatus;
import com.taskflow.dto.JobRunSummaryDto;
import com.taskflow.persistence.JobRunRepository;
import com.taskflow.persistence.Page;
import com.taskflow.persistence.RunQuery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Generates human-readable and CSV run reports using Streams aggregation.
 */
public final class ReportService {
    private final JobRunRepository jobRunRepository;

    public ReportService(JobRunRepository jobRunRepository) {
        this.jobRunRepository = jobRunRepository;
    }

    public List<JobRunSummaryDto> generateReport(Duration window) {
        return generateReport(Instant.now().minus(window), Instant.now());
    }

    public List<JobRunSummaryDto> generateReport(Instant from, Instant to) {
        return summarize(jobRunRepository.findRuns(RunQuery.builder().startedFrom(from).startedTo(to).build(), Page.first(10_000)).items());
    }

    public List<JobRunSummaryDto> generateReport(String jobId, Duration window) {
        return summarize(jobRunRepository.findRuns(
                RunQuery.builder().jobId(JobId.of(jobId)).startedFrom(Instant.now().minus(window)).build(),
                Page.first(10_000)).items());
    }

    public String renderText(List<JobRunSummaryDto> summaries) {
        StringBuilder builder = new StringBuilder("job,total,success,failed,successRate,avgMs,p95Ms%n".formatted());
        for (JobRunSummaryDto summary : summaries) {
            builder.append(summary.jobId()).append(',')
                    .append(summary.totalRuns()).append(',')
                    .append(summary.succeeded()).append(',')
                    .append(summary.failed()).append(',')
                    .append(String.format("%.2f", summary.successRate())).append(',')
                    .append(summary.averageDuration().toMillis()).append(',')
                    .append(summary.p95Duration().toMillis()).append(System.lineSeparator());
        }
        return builder.toString();
    }

    public void writeCsv(Path path, List<JobRunSummaryDto> summaries) throws IOException {
        Files.writeString(path, renderText(summaries), StandardCharsets.UTF_8);
    }

    private List<JobRunSummaryDto> summarize(List<JobRun> runs) {
        Map<JobId, List<JobRun>> grouped = runs.stream().collect(Collectors.groupingBy(JobRun::jobId));
        return grouped.entrySet().stream()
                .map(entry -> summarizeJob(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(JobRunSummaryDto::jobId))
                .toList();
    }

    private JobRunSummaryDto summarizeJob(JobId jobId, List<JobRun> runs) {
        long total = runs.size();
        long succeeded = runs.stream().filter(run -> run.status() == JobStatus.SUCCEEDED).count();
        long failed = runs.stream().filter(run -> run.status() != JobStatus.SUCCEEDED).count();
        DoubleSummaryStatistics stats = runs.stream()
                .collect(Collectors.summarizingDouble(run -> run.duration().toMillis()));
        List<Long> durations = runs.stream().map(run -> run.duration().toMillis()).sorted().collect(Collectors.toCollection(ArrayList::new));
        long p95 = durations.isEmpty() ? 0L : durations.get(Math.max(0, (int) Math.ceil(durations.size() * 0.95) - 1));
        return new JobRunSummaryDto(
                jobId,
                total,
                succeeded,
                failed,
                total == 0 ? 0.0 : (double) succeeded / total,
                Duration.ofMillis((long) stats.getAverage()),
                Duration.ofMillis(p95));
    }
    public long countByStatus(JobStatus status) {
        return jobRunRepository.findRuns(RunQuery.builder().status(status).build(), Page.first(10_000)).items().size();
    }

    public Optional<JobRun> latestRun(JobId jobId) {
        return jobRunRepository.findRuns(RunQuery.builder().jobId(jobId).build(), Page.first(1)).items().stream().findFirst();
    }
}
