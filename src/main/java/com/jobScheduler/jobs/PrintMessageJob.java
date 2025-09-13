//package com.jobScheduler.jobs;
//
//import com.jobScheduler.model.JobLog;
//import com.jobScheduler.repository.JobLogRepository;
//import org.quartz.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.stereotype.Component;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.Random;
//
//@Component
//public class PrintMessageJob implements Job {
//
//    // We cannot use constructor injection in Quartz Job; use ApplicationContext lookup
//    @Override
//    public void execute(JobExecutionContext context) throws JobExecutionException {
//        JobDataMap data = context.getMergedJobDataMap();
//
//        String jobId = data.getString("jobId");
//        String jobName = data.getString("jobName");
//        int attempt = data.containsKey("attempt") ? data.getInt("attempt") : 1;
//        int maxAttempts = data.containsKey("maxAttempts") ? data.getInt("maxAttempts") : 3;
//
//        // get Spring beans from SchedulerContext (populated in QuartzConfig)
//        SchedulerContext schedCtx;
//        try {
//            schedCtx = context.getScheduler().getContext();
//        } catch (SchedulerException e) {
//            throw new JobExecutionException(e);
//        }
//
//        ApplicationContext appCtx = (ApplicationContext) schedCtx.get("applicationContext");
//        JobLogRepository logRepo = appCtx.getBean(JobLogRepository.class);
//
//        LocalDateTime start = LocalDateTime.now();
//        JobLog log = new JobLog();
//        log.setJobId(jobId);
//        log.setJobName(jobName);
//        log.setTimestamp(start);
//        log.setAttempt(attempt);
//        log.setStatus("RUNNING");
//        log.setMessage("Started");
//        logRepo.save(log);
//
//        long durationMs = 0L;
//        try {
//            // === JOB BUSINESS LOGIC START ===
//            // For demonstration: print message and sometimes throw to simulate failure
//            System.out.println("Executing job: " + jobName + " (id=" + jobId + ") attempt=" + attempt +
//                    " at " + start);
//
//            // Simulate work and random failure for demo:
//            Thread.sleep(500); // simulate time taken by job
//            if (new Random().nextInt(5) == 0) { // ~20% chance fail
//                throw new RuntimeException("Simulated failure");
//            }
//            // === JOB BUSINESS LOGIC END ===
//
//            durationMs = Duration.between(start, LocalDateTime.now()).toMillis();
//            log.setDurationMs(durationMs);
//            log.setStatus("SUCCESS");
//            log.setMessage("Completed successfully");
//            logRepo.save(log);
//
//        } catch (Exception ex) {
//            durationMs = Duration.between(start, LocalDateTime.now()).toMillis();
//            log.setDurationMs(durationMs);
//            log.setStatus("FAILED");
//            log.setMessage(ex.getMessage());
//            logRepo.save(log);
//
//            // Failure handling: retry with exponential backoff if attempts left
//            if (attempt < maxAttempts) {
//                int nextAttempt = attempt + 1;
//                long backoffSeconds = (long) Math.pow(2, attempt); // 2^attempt seconds
//
//                // Build a new trigger that fires after backoffSeconds
//                Trigger newTrigger = TriggerBuilder.newTrigger()
//                        .forJob(context.getJobDetail())
//                        .usingJobData("attempt", nextAttempt)
//                        .usingJobData("maxAttempts", maxAttempts)
//                        .usingJobData("jobId", jobId)
//                        .usingJobData("jobName", jobName)
//                        .startAt(DateBuilder.futureDate((int) backoffSeconds, DateBuilder.IntervalUnit.SECOND))
//                        .withIdentity(context.getJobDetail().getKey().getName() + "_retry_" + nextAttempt,
//                                      context.getJobDetail().getKey().getGroup())
//                        .build();
//
//                try {
//                    context.getScheduler().scheduleJob(newTrigger);
//                    System.out.println("Scheduled retry #" + nextAttempt + " for job " + jobName +
//                            " after " + backoffSeconds + "s");
//                } catch (SchedulerException se) {
//                    throw new JobExecutionException("Failed to schedule retry", se, false);
//                }
//            } else {
//                System.out.println("Job " + jobName + " exhausted retries (" + attempt + ")");
//            }
//
//            // rethrow if you want Quartz to mark job as failed — here we swallow since we handled retries
//        }
//    }
//}


package com.jobScheduler.jobs;

import com.jobScheduler.service.JobService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrintMessageJob implements Job {

    @Autowired
    private JobService jobService; // Quartz will now inject this

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("Executing job: " + context.getJobDetail().getKey().getName());
        jobService.doSomething(); // replace with your actual logic
    }
}
