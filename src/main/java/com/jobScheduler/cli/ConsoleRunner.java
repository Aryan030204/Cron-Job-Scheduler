package com.jobScheduler.cli;

import com.jobScheduler.model.Job;
import com.jobScheduler.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConsoleRunner implements CommandLineRunner {

    private final JobService jobService;
    
    public ConsoleRunner(JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Run console in a separate thread so Spring still finishes starting
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                printHelp();
                while ((line = reader.readLine()) != null) {
                    handle(line.trim());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void printHelp() {
        System.out.println("Console CLI: available commands:");
        System.out.println("  list                      -> list all jobs");
        System.out.println("  schedule <name> <cron>    -> schedule job with name and cron");
        System.out.println("  pause <jobId>             -> pause job");
        System.out.println("  resume <jobId>            -> resume job");
        System.out.println("  delete <jobId>            -> delete job");
        System.out.println("  logs <jobId>              -> show last logs for job");
        System.out.println("  help                      -> show this help");
    }

    private void handle(String input) {
        try {
            if (input.isBlank()) return;
            String[] parts = input.split("\\s+");
            String cmd = parts[0];

            switch (cmd.toLowerCase()) {
                case "list":
                    List<Job> jobs = jobService.listAllJobs();
                    jobs.forEach(j -> System.out.println(j.getId() + " | " + j.getName() + " | " + j.getStatus() + " | " + j.getCronExpression()));
                    break;
                case "schedule":
                    if (parts.length < 3) {
                        System.out.println("Usage: schedule <name> <cronExpression>");
                        break;
                    }
                    String name = parts[1];
                    // fix: join all remaining tokens to support multi-part cron expressions
                    String cron = String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length));
                    
                    Job job = new Job();
                    job.setName(name);
                    job.setCronExpression(cron);
                    job = jobService.saveJob(job);
                    jobService.scheduleJob(job);
                    System.out.println("Scheduled job id=" + job.getId());
                    break;
                case "pause":
                    if (parts.length < 2) { System.out.println("Usage: pause <jobId>"); break; }
                    System.out.println(jobService.pauseJob(parts[1]) ? "Paused." : "Failed to pause.");
                    break;
                case "resume":
                    if (parts.length < 2) { System.out.println("Usage: resume <jobId>"); break; }
                    System.out.println(jobService.resumeJob(parts[1]) ? "Resumed." : "Failed to resume.");
                    break;
                case "delete":
                    if (parts.length < 2) { System.out.println("Usage: delete <jobId>"); break; }
                    System.out.println(jobService.deleteJob(parts[1]) ? "Deleted." : "Failed to delete.");
                    break;
                case "logs":
                    if (parts.length < 2) { System.out.println("Usage: logs <jobId>"); break; }
                    jobService.getJobLogs(parts[1]).forEach(System.out::println);
                    break;
                case "help":
                    printHelp();
                    break;
                default:
                    System.out.println("Unknown command: " + cmd + ". Type 'help'");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
