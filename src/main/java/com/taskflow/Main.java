package com.taskflow;

import com.taskflow.cli.TaskFlowCli;
import com.taskflow.config.AppContext;
import com.taskflow.config.ConfigService;
import com.taskflow.http.StatusHttpApi;
import com.taskflow.persistence.ConnectionManager;
import com.taskflow.persistence.JdbcJobRunRepository;
import com.taskflow.persistence.JdbcWorkflowRepository;
import com.taskflow.util.Constants;

/**
 * TaskFlow application entry point.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println(Constants.BANNER);
        System.out.println("Version: " + Constants.VERSION);

        ConfigService configService = new ConfigService();
        ConnectionManager connectionManager = new ConnectionManager(configService);
        JdbcWorkflowRepository workflowRepository = new JdbcWorkflowRepository(connectionManager);
        JdbcJobRunRepository jobRunRepository = new JdbcJobRunRepository(connectionManager);
        
        com.taskflow.config.DemoSeeder seeder = new com.taskflow.config.DemoSeeder(connectionManager, workflowRepository, jobRunRepository);
        seeder.seedIfEmpty();
        
        AppContext appContext = new AppContext(workflowRepository, jobRunRepository);
        TaskFlowCli cli = new TaskFlowCli(
                appContext.workflowService(),
                appContext.schedulingService(),
                appContext.reportService(),
                jobRunRepository
        );

        if (args.length > 0) {
            // Run CLI command
            String result = cli.run(args);
            System.out.println(result);
            
            // Clean up resources and exit
            appContext.close();
            connectionManager.close();
            System.exit(0);
        } else {
            // Start HTTP Server and engine
            int port = configService.getInt("taskflow.http.port", 8081);
            StatusHttpApi httpApi = new StatusHttpApi(appContext.workflowService(), appContext.reportService(), port);
            httpApi.start();
            
            System.out.println("TaskFlow engine started.");
            System.out.println("HTTP API listening on port " + port);
            System.out.println("Use Ctrl+C to stop.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down TaskFlow...");
                httpApi.close();
                appContext.close();
                connectionManager.close();
            }));

            // Keep the application running
            Thread.currentThread().join();
        }
    }
}
