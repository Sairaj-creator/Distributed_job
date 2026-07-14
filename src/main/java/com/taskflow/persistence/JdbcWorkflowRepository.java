package com.taskflow.persistence;

import com.taskflow.api.JobResult;
import com.taskflow.core.JobDefinition;
import com.taskflow.core.JobId;
import com.taskflow.core.OverlapPolicy;
import com.taskflow.core.ScheduleType;
import com.taskflow.core.Workflow;
import com.taskflow.core.WorkflowId;
import com.taskflow.exception.PersistenceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation for workflow definitions.
 */
public final class JdbcWorkflowRepository implements WorkflowRepository {
    private final ConnectionManager connectionManager;

    public JdbcWorkflowRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<Workflow> findById(WorkflowId id) {
        try (Connection connection = connectionManager.getConnection()) {
            Workflow.Builder builder;
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM workflows WHERE workflow_id = ?")) {
                statement.setString(1, id.value());
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    builder = Workflow.builder(id, rs.getString("name"))
                            .description(rs.getString("description"))
                            .schedule(ScheduleType.valueOf(rs.getString("schedule_type")), rs.getString("schedule_spec"))
                            .overlapPolicy(OverlapPolicy.valueOf(rs.getString("overlap_policy")))
                            .paused(rs.getBoolean("is_paused"));
                }
            }
            loadJobs(connection, id, builder);
            loadDependencies(connection, id, builder);
            return Optional.of(builder.build());
        } catch (SQLException ex) {
            throw new PersistenceException("failed to find workflow " + id, ex);
        }
    }

    private void loadJobs(Connection connection, WorkflowId id, Workflow.Builder builder) throws SQLException {
        String sql = "SELECT * FROM jobs WHERE workflow_id = ? ORDER BY job_id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.value());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    JobId jobId = JobId.of(rs.getString("job_id"));
                    builder.addJob(JobDefinition.builder(jobId, rs.getString("name"), ctx -> JobResult.success("metadata-only job"))
                            .jobClassName(rs.getString("job_class"))
                            .timeout(Duration.ofSeconds(rs.getInt("timeout_seconds")))
                            .build());
                }
            }
        }
    }

    private void loadDependencies(Connection connection, WorkflowId id, Workflow.Builder builder) throws SQLException {
        String sql = """
                SELECT d.job_id, d.depends_on_job_id
                FROM job_dependencies d
                JOIN jobs j ON j.job_id = d.job_id
                WHERE j.workflow_id = ?
                ORDER BY d.job_id, d.depends_on_job_id
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.value());
            try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                builder.dependsOn(JobId.of(rs.getString("job_id")), JobId.of(rs.getString("depends_on_job_id")));
            }
            }
        }
    }

    @Override
    public Workflow save(Workflow entity) {
        try (Connection connection = connectionManager.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                upsertWorkflow(connection, entity);
                deleteChildren(connection, entity.id());
                insertJobs(connection, entity);
                insertDependencies(connection, entity);
                connection.commit();
                return entity;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException ex) {
            throw new PersistenceException("failed to save workflow " + entity.id(), ex);
        }
    }

    private void upsertWorkflow(Connection connection, Workflow workflow) throws SQLException {
        String sql = """
                INSERT INTO workflows(workflow_id, name, description, schedule_type, schedule_spec, overlap_policy, is_paused)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (workflow_id) DO UPDATE SET
                    name = EXCLUDED.name,
                    description = EXCLUDED.description,
                    schedule_type = EXCLUDED.schedule_type,
                    schedule_spec = EXCLUDED.schedule_spec,
                    overlap_policy = EXCLUDED.overlap_policy,
                    is_paused = EXCLUDED.is_paused
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, workflow.id().value());
            statement.setString(2, workflow.name());
            statement.setString(3, workflow.description());
            statement.setString(4, workflow.scheduleType().name());
            statement.setString(5, workflow.scheduleSpec());
            statement.setString(6, workflow.overlapPolicy().name());
            statement.setBoolean(7, workflow.isPaused());
            statement.executeUpdate();
        }
    }

    private void deleteChildren(Connection connection, WorkflowId id) throws SQLException {
        try (PreparedStatement dependencies = connection.prepareStatement(
                "DELETE FROM job_dependencies WHERE job_id IN (SELECT job_id FROM jobs WHERE workflow_id = ?)");
             PreparedStatement jobs = connection.prepareStatement("DELETE FROM jobs WHERE workflow_id = ?")) {
            dependencies.setString(1, id.value());
            dependencies.executeUpdate();
            jobs.setString(1, id.value());
            jobs.executeUpdate();
        }
    }

    private void insertJobs(Connection connection, Workflow workflow) throws SQLException {
        String sql = """
                INSERT INTO jobs(job_id, workflow_id, name, job_class, retry_policy, max_attempts, timeout_seconds)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (JobDefinition job : workflow.jobs()) {
                statement.setString(1, job.id().value());
                statement.setString(2, workflow.id().value());
                statement.setString(3, job.name());
                statement.setString(4, job.jobClassName());
                statement.setString(5, job.retryPolicy().getClass().getSimpleName());
                statement.setInt(6, job.retryPolicy().maxAttempts());
                statement.setLong(7, job.timeout().toSeconds());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertDependencies(Connection connection, Workflow workflow) throws SQLException {
        String sql = "INSERT INTO job_dependencies(job_id, depends_on_job_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (JobDefinition job : workflow.jobs()) {
                for (JobId dependency : workflow.graph().dependenciesOf(job.id())) {
                    statement.setString(1, job.id().value());
                    statement.setString(2, dependency.value());
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
    }

    @Override
    public List<Workflow> findAll() {
        List<Workflow> workflows = new ArrayList<>();
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT workflow_id FROM workflows ORDER BY workflow_id");
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                findById(WorkflowId.of(rs.getString("workflow_id"))).ifPresent(workflows::add);
            }
            return workflows;
        } catch (SQLException ex) {
            throw new PersistenceException("failed to list workflows", ex);
        }
    }
}
