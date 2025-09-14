package com.jobScheduler.service;

import com.jobScheduler.model.Job;
import com.jobScheduler.model.JobLog;
import com.jobScheduler.repository.JobRepository;
import com.jobScheduler.repository.JobLogRepository;
import com.jobScheduler.utils.CronUtils;
import com.jobScheduler.jobs.PrintMessageJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final Scheduler scheduler;
    private final JobRepository jobRepository;
    private final JobLogRepository jobLogRepository;

    // core schedule method
    public void scheduleJob(Job job) throws SchedulerException {
        if (!CronUtils.isValidCron(job.getCronExpression())) {
            throw new IllegalArgumentException("Invalid cron expression: " + job.getCronExpression());
        }

        JobDetail jobDetail = JobBuilder.newJob(PrintMessageJob.class)
                .withIdentity(job.getName(), "group1")
                .usingJobData("jobId", job.getId())
                .usingJobData("jobName", job.getName())
                .storeDurably(false)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(job.getName() + "Trigger", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                .usingJobData("jobId", job.getId())
                .usingJobData("jobName", job.getName())
                .build();

        if (!scheduler.checkExists(jobDetail.getKey())) {
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            scheduler.rescheduleJob(trigger.getKey(), trigger);
        }

        job.setStatus("SCHEDULED");
        jobRepository.save(job);
    }

    public List<Job> listAllJobs() {
        return jobRepository.findAll();
    }

    public boolean pauseJob(String jobId) throws SchedulerException {
        Optional<Job> opt = jobRepository.findById(jobId);
        if (opt.isEmpty()) return false;
        Job job = opt.get();
        JobKey key = new JobKey(job.getName(), "group1");
        if (scheduler.checkExists(key)) {
            scheduler.pauseJob(key);
            job.setStatus("PAUSED");
            jobRepository.save(job);
            return true;
        }
        return false;
    }

    public boolean resumeJob(String jobId) throws SchedulerException {
        Optional<Job> opt = jobRepository.findById(jobId);
        if (opt.isEmpty()) return false;
        Job job = opt.get();
        JobKey key = new JobKey(job.getName(), "group1");
        if (scheduler.checkExists(key)) {
            scheduler.resumeJob(key);
            job.setStatus("SCHEDULED");
            jobRepository.save(job);
            return true;
        }
        return false;
    }

    public boolean deleteJob(String jobId) throws SchedulerException {
        Optional<Job> opt = jobRepository.findById(jobId);
        if (opt.isEmpty()) return false;
        Job job = opt.get();
        JobKey key = new JobKey(job.getName(), "group1");
        boolean deleted = scheduler.deleteJob(key);
        if (deleted) {
            job.setStatus("DELETED");
            jobRepository.save(job);
        }
        return deleted;
    }

    public List<?> getJobLogs(String jobId) {
        return jobLogRepository.findByJobIdOrderByTimestampDesc(jobId)
                .stream().collect(Collectors.toList());
    }

    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

    // execute logic based on action
    public void doSomething(String jobId) {
        Optional<Job> opt = jobRepository.findById(jobId);
        if (opt.isEmpty()) return;

        Job job = opt.get();
        String action = job.getAction();
        String payload = job.getPayload();
        String logMessage = "Executed action: " + action;

        try {
            switch (action) {
                case "PRINT_MESSAGE":
                    System.out.println("[Job " + job.getName() + "] " + payload);
                    logMessage = payload;
                    break;

                case "COUNTDOWN":
                    try {
                        int start = Integer.parseInt(payload);
                        if (start > 0) {
                            System.out.println("[Job " + job.getName() + "] Countdown: " + start);
                            job.setPayload(String.valueOf(start - 1));
                            jobRepository.save(job);
                        } else {
                            System.out.println("[Job " + job.getName() + "] Countdown finished!");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid countdown number for job " + job.getName());
                    }
                    logMessage = "Countdown tick";
                    break;

                case "CALL_API":
                    try {
                        URL url = new URL(payload);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.connect();
                        int responseCode = conn.getResponseCode();
                        StringBuilder response = new StringBuilder();
                        try (Scanner sc = new Scanner(conn.getInputStream())) {
                            while (sc.hasNext()) {
                                response.append(sc.nextLine());
                            }
                        }
                        System.out.println("[Job " + job.getName() + "] API Response (" + responseCode + "): " + response);
                        logMessage = "API call successful";
                    } catch (Exception e) {
                        System.out.println("API call failed: " + e.getMessage());
                        logMessage = "API call failed";
                    }
                    break;

                case "WRITE_FILE":
                    try (FileWriter writer = new FileWriter(payload, true)) {
                        writer.write("Log from job " + job.getName() + " at " + new Date() + "\n");
                        System.out.println("[Job " + job.getName() + "] Wrote to file " + payload);
                        logMessage = "File write successful";
                    } catch (Exception e) {
                        System.out.println("File write failed: " + e.getMessage());
                        logMessage = "File write failed";
                    }
                    break;

                case "MONGO_LOG":
                    System.out.println("[Job " + job.getName() + "] Insert log into MongoDB: " + payload);
                    logMessage = "Inserted MongoDB log: " + payload;
                    break;

                case "SEND_EMAIL":
                    System.out.println("[Job " + job.getName() + "] Sending fake email to " + payload);
                    logMessage = "Fake email sent to " + payload;
                    break;

                default:
                    System.out.println("[Job " + job.getName() + "] Unknown action.");
                    logMessage = "Unknown action";
            }
        } finally {
            JobLog log = new JobLog();
            log.setJobId(jobId);
            log.setMessage(logMessage);
            log.setTimestamp(LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault()));
            jobLogRepository.save(log);
        }
    }
}
