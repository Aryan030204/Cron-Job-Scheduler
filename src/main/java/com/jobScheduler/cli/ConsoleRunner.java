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

    @Override
    public void run(String... args) throws Exception {
        // Run console in a separate thread so Spring still finishes starting
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                printHelp();
                while ((line = reader.readLine()) != null) {
                    handle(line.trim(), reader);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void printHelp() {
        System.out.println("Console CLI: available commands:");
        System.out.println("  list                      -> list all jobs");
        System.out.println("  schedule <name> <cron>    -> schedule job with name and cron (then choose action)");
        System.out.println("  pause <jobId>             -> pause job");
        System.out.println("  resume <jobId>            -> resume job");
        System.out.println("  delete <jobId>            -> delete job");
        System.out.println("  logs <jobId>              -> show last logs for job");
        System.out.println("  help                      -> show this help");
    }

    private void handle(String input, BufferedReader reader) {
        try {
            if (input.isBlank())
                return;
            String[] parts = input.split("\\s+");
            String cmd = parts[0];

            switch (cmd.toLowerCase()) {
                case "list":
                    List<Job> jobs = jobService.listAllJobs();
                    jobs.forEach(j -> System.out.println(j.getId() + " | " + j.getName() + " | " + j.getStatus() + " | "
                            + j.getCronExpression() + " | action=" + j.getAction()));
                    break;

                case "schedule":
                    if (parts.length < 3) {
                        System.out.println("Usage: schedule <name> <cronExpression>");
                        break;
                    }
                    String name = parts[1];
                    String cron = String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length));

                    Job job = new Job();
                    job.setName(name);
                    job.setCronExpression(cron);

                    // Save preliminary job
                    job = jobService.saveJob(job);

                    // Ask user which action the job should perform
                    System.out.println("Select an action for this job:");
                    System.out.println("  1. Print a custom message");
                    System.out.println("  2. Run a countdown timer");
                    System.out.println("  3. Call an external API and log the response");
                    System.out.println("  4. Write a message to a file");
                    System.out.println("  5. Insert a log entry into MongoDB");
                    System.out.println("  6. Send a fake 'email' notification");

                    String actionChoice = reader.readLine().trim();
                    switch (actionChoice) {
                        case "1":
                            job.setAction("PRINT_MESSAGE");
                            System.out.print("Enter message: ");
                            job.setPayload(reader.readLine().trim());
                            break;
                        case "2":
                            job.setAction("COUNTDOWN");
                            System.out.print("Enter countdown start number: ");
                            job.setPayload(reader.readLine().trim());
                            break;
                        case "3":
                            job.setAction("CALL_API");
                            System.out.print("Enter API URL: ");
                            job.setPayload(reader.readLine().trim());
                            break;
                        case "4":
                            job.setAction("WRITE_FILE");
                            System.out.print("Enter file path: ");
                            job.setPayload(reader.readLine().trim());
                            break;
                        case "5":
                            job.setAction("MONGO_LOG");
                            job.setPayload("Insert log for job " + job.getName());
                            break;
                        case "6":
                            job.setAction("SEND_EMAIL");
                            System.out.print("Enter fake recipient email: ");
                            job.setPayload(reader.readLine().trim());
                            break;
                        default:
                            System.out.println("Invalid choice. Defaulting to PRINT_MESSAGE.");
                            job.setAction("PRINT_MESSAGE");
                            job.setPayload("Hello from " + job.getName());
                    }

                    // Save again with action+payload
                    job = jobService.saveJob(job);

                    // Schedule job
                    jobService.scheduleJob(job);
                    System.out.println("Scheduled job id=" + job.getId() + " with action=" + job.getAction());
                    break;

                case "pause":
                    if (parts.length < 2) {
                        System.out.println("Usage: pause <jobId>");
                        break;
                    }
                    System.out.println(jobService.pauseJob(parts[1]) ? "Paused." : "Failed to pause.");
                    break;

                case "resume":
                    if (parts.length < 2) {
                        System.out.println("Usage: resume <jobId>");
                        break;
                    }
                    System.out.println(jobService.resumeJob(parts[1]) ? "Resumed." : "Failed to resume.");
                    break;

                case "delete":
                    if (parts.length < 2) {
                        System.out.println("Usage: delete <jobId>");
                        break;
                    }
                    System.out.println(jobService.deleteJob(parts[1]) ? "Deleted." : "Failed to delete.");
                    break;

                case "logs":
                    if (parts.length < 2) {
                        System.out.println("Usage: logs <jobId>");
                        break;
                    }
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
