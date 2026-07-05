package com.taskflow.cli;

import com.taskflow.core.JobId;
import com.taskflow.core.WorkflowId;
import com.taskflow.persistence.Page;
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
    private final CommandParser parser = new CommandParser();

    public TaskFlowCli(WorkflowService workflowService, SchedulingService schedulingService, ReportService reportService) {
        this.workflowService = workflowService;
        this.schedulingService = schedulingService;
        this.reportService = reportService;
    }

    public String run(String[] args) {
        CommandParser.ParsedCommand command = parser.parse(args);
        return switch (command.name()) {
            case "list-workflows" -> workflowService.listStatuses().toString();
            case "show-workflow" -> workflowService.status(WorkflowId.of(required(command, 0))).map(Object::toString).orElse("not found");
            case "trigger" -> schedulingService.triggerNow(WorkflowId.of(required(command, 0))).toString();
            case "pause" -> workflowService.pause(WorkflowId.of(required(command, 0))).id() + " paused";
            case "resume" -> workflowService.resume(WorkflowId.of(required(command, 0))).id() + " resumed";
            case "history" -> "history for " + JobId.of(required(command, 0)) + " is available through JobRunRepository";
            case "stats" -> reportService.renderText(reportService.generateReport(Duration.ofDays(7)));
            default -> help();
        };
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
