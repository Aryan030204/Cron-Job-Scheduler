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
    private JobService jobService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // jobId is stored in JobDataMap when scheduling
        String jobId = context.getJobDetail().getJobDataMap().getString("jobId");
        String jobName = context.getJobDetail().getKey().getName();

        System.out.println("Quartz fired job: " + jobName + " (jobId=" + jobId + ") at " + java.time.LocalDateTime.now());
        try {
            jobService.doSomething(jobId);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
