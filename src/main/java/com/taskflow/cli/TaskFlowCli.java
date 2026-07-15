package com.taskflow.cli;

import com.taskflow.core.JobId;
import com.taskflow.core.JobRun;
import com.taskflow.core.WorkflowId;
import com.taskflow.persistence.Page;
import com.taskflow.persistence.JobRunRepository;
import com.taskflow.persistence.RunQuery;
import com.taskflow.service.ReportService;
import com.taskflow.service.SchedulingService;
import com.taskflow.service.WorkflowService;

import java.time.Duration;

/**
 * Thin CLI controller for TaskFlow operations.
 */
public final class TaskFlowCli {
    private final WorkflowService workflowService;
    private final SchedulingService schedulingService;
    private final ReportService reportService;
    private final JobRunRepository jobRunRepository;
    private final CommandParser parser = new CommandParser();

    public TaskFlowCli(WorkflowService workflowService, SchedulingService schedulingService, ReportService reportService) {
        this(workflowService, schedulingService, reportService, null);
    }

    public TaskFlowCli(
            WorkflowService workflowService,
            SchedulingService schedulingService,
            ReportService reportService,
            JobRunRepository jobRunRepository) {
        this.workflowService = workflowService;
        this.schedulingService = schedulingService;
        this.reportService = reportService;
        this.jobRunRepository = jobRunRepository;
    }

    public String run(String[] args) {
        CommandParser.ParsedCommand command = parser.parse(args);
        return switch (command.name()) {
            case "list-workflows" -> workflowService.listStatuses().toString();
            case "show-workflow" -> workflowService.status(WorkflowId.of(required(command, 0))).map(Object::toString).orElse("not found");
            case "trigger" -> schedulingService.triggerNowAsync(WorkflowId.of(required(command, 0))).join().toString();
            case "pause" -> workflowService.pause(WorkflowId.of(required(command, 0))).id() + " paused";
            case "resume" -> workflowService.resume(WorkflowId.of(required(command, 0))).id() + " resumed";
            case "history" -> history(command);
            case "stats" -> reportService.renderText(reportService.generateReport(Duration.ofDays(7)));
            default -> help();
        };
    }

    private String history(CommandParser.ParsedCommand command) {
        if (jobRunRepository == null) {
            return "history repository is not configured";
        }
        JobId jobId = JobId.of(required(command, 0));
        int limit = parseLimit(command);
        StringBuilder builder = new StringBuilder("runId,jobId,status,attempt,startedAt,finishedAt").append(System.lineSeparator());
        for (JobRun run : jobRunRepository.findRuns(RunQuery.builder().jobId(jobId).build(), Page.first(limit)).items()) {
            builder.append(run.runId()).append(',')
                    .append(run.jobId()).append(',')
                    .append(run.status()).append(',')
                    .append(run.attemptNumber()).append(',')
                    .append(run.startedAt()).append(',')
                    .append(run.finishedAt()).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private int parseLimit(CommandParser.ParsedCommand command) {
        for (int i = 0; i < command.arguments().size() - 1; i++) {
            if ("--limit".equals(command.arguments().get(i))) {
                return Integer.parseInt(command.arguments().get(i + 1));
            }
        }
        return 20;
    }

    private String required(CommandParser.ParsedCommand command, int index) {
        if (command.arguments().size() <= index) {
            throw new IllegalArgumentException("missing argument " + index + " for " + command.name());
        }
        return command.arguments().get(index);
    }

    private String help() {
        return "TaskFlow commands: list-workflows, show-workflow <id>, trigger <id>, pause <id>, resume <id>, "
                + "history <jobId> [--limit N], stats";
    }
}
