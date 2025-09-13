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
        String jobId = context.getJobDetail().getJobDataMap().getString("jobId");

        System.out.println("Executing job: " + context.getJobDetail().getKey().getName() 
                           + " (jobId=" + jobId + ")");

        jobService.doSomething(jobId);
    }
}
