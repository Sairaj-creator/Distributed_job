package com.taskflow.persistence;

import com.taskflow.core.JobId;
import com.taskflow.core.JobRun;
import com.taskflow.core.JobStatus;
import com.taskflow.core.WorkflowId;
import com.taskflow.exception.PersistenceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of job run persistence with dynamic filtering and pagination.
 */
public final class JdbcJobRunRepository implements JobRunRepository {
    private final ConnectionManager connectionManager;

    public JdbcJobRunRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<JobRun> findById(Long id) {
        String sql = "SELECT * FROM job_runs WHERE run_id = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new PersistenceException("failed to find job run " + id, ex);
        }
    }

    @Override
    public JobRun save(JobRun entity) {
        return entity.runId() == null ? insert(entity) : update(entity);
    }

    private JobRun insert(JobRun entity) {
        String sql = """
                INSERT INTO job_runs(job_id, workflow_id, workflow_run_id, attempt_number, status,
                                     started_at, finished_at, duration_ms, error_message, output_summary)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindRun(statement, entity);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return entity.withRunId(keys.getLong(1));
                }
                return entity;
            }
        } catch (SQLException ex) {
            throw new PersistenceException("failed to insert job run", ex);
        }
    }

    private JobRun update(JobRun entity) {
        String sql = """
                UPDATE job_runs
                SET job_id = ?, workflow_id = ?, workflow_run_id = ?, attempt_number = ?, status = ?,
                    started_at = ?, finished_at = ?, duration_ms = ?, error_message = ?, output_summary = ?
                WHERE run_id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindRun(statement, entity);
            statement.setLong(11, entity.runId());
            statement.executeUpdate();
            return entity;
        } catch (SQLException ex) {
            throw new PersistenceException("failed to update job run " + entity.runId(), ex);
        }
    }

    @Override
    public List<JobRun> findAll() {
        return findRuns(RunQuery.builder().build(), Page.first(1000)).items();
    }

    @Override
    public PageResult<JobRun> findRuns(RunQuery query, Page page) {
        QueryParts parts = buildWhere(query);
        String sql = "SELECT * FROM job_runs " + parts.where()
                + " ORDER BY started_at DESC, run_id DESC LIMIT ? OFFSET ?";
        String countSql = "SELECT COUNT(*) FROM job_runs " + parts.where();
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             PreparedStatement count = connection.prepareStatement(countSql)) {
            bindParameters(statement, parts.values());
            statement.setInt(parts.values().size() + 1, page.limit());
            statement.setInt(parts.values().size() + 2, page.offset());
            bindParameters(count, parts.values());
            List<JobRun> runs = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    runs.add(map(rs));
                }
            }
            long total;
            try (ResultSet rs = count.executeQuery()) {
                rs.next();
                total = rs.getLong(1);
            }
            return new PageResult<>(runs, page.offset(), page.limit(), total);
        } catch (SQLException ex) {
            throw new PersistenceException("failed to query job runs", ex);
        }
    }

    private QueryParts buildWhere(RunQuery query) {
        List<String> clauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        query.workflowId().ifPresent(workflowId -> {
            clauses.add("workflow_id = ?");
            values.add(workflowId.value());
        });
        query.jobId().ifPresent(jobId -> {
            clauses.add("job_id = ?");
            values.add(jobId.value());
        });
        query.status().ifPresent(status -> {
            clauses.add("status = ?");
            values.add(status.name());
        });
        query.startedFrom().ifPresent(from -> {
            clauses.add("started_at >= ?");
            values.add(Timestamp.from(from));
        });
        query.startedTo().ifPresent(to -> {
            clauses.add("started_at <= ?");
            values.add(Timestamp.from(to));
        });
        return new QueryParts(clauses.isEmpty() ? "" : "WHERE " + String.join(" AND ", clauses), values);
    }

    private void bindRun(PreparedStatement statement, JobRun run) throws SQLException {
        statement.setString(1, run.jobId().value());
        statement.setString(2, run.workflowId().value());
        statement.setLong(3, run.workflowRunId());
        statement.setInt(4, run.attemptNumber());
        statement.setString(5, run.status().name());
        statement.setTimestamp(6, toTimestamp(run.startedAt()));
        statement.setTimestamp(7, toTimestamp(run.finishedAt()));
        statement.setLong(8, run.duration().toMillis());
        statement.setString(9, run.errorMessage());
        statement.setString(10, run.outputSummary());
    }

    private void bindParameters(PreparedStatement statement, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            statement.setObject(i + 1, values.get(i));
        }
    }

    private Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private JobRun map(ResultSet rs) throws SQLException {
        return JobRun.builder(JobId.of(rs.getString("job_id")), WorkflowId.of(rs.getString("workflow_id")),
                        rs.getLong("workflow_run_id"))
                .runId(rs.getLong("run_id"))
                .attemptNumber(rs.getInt("attempt_number"))
                .status(JobStatus.valueOf(rs.getString("status")))
                .startedAt(toInstant(rs.getTimestamp("started_at")))
                .finishedAt(toInstant(rs.getTimestamp("finished_at")))
                .errorMessage(rs.getString("error_message"))
                .outputSummary(rs.getString("output_summary"))
                .build();
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private record QueryParts(String where, List<Object> values) {
    }
}
