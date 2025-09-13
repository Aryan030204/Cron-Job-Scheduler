package com.jobScheduler.service;

import com.jobScheduler.model.Job;
import com.jobScheduler.repository.JobRepository;
import com.jobScheduler.repository.JobLogRepository;
import com.jobScheduler.utils.CronUtils;
import com.jobScheduler.jobs.PrintMessageJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final Scheduler scheduler; // injected by Spring Boot
    private final JobRepository jobRepository;
    private final JobLogRepository jobLogRepository;
    

    public JobService(Scheduler scheduler, JobRepository jobRepository, JobLogRepository jobLogRepository) {
		super();
		this.scheduler = scheduler;
		this.jobRepository = jobRepository;
		this.jobLogRepository = jobLogRepository;
	}

	// core schedule method (enhanced)
    public void scheduleJob(Job job) throws SchedulerException {
        if (!CronUtils.isValidCron(job.getCronExpression())) {
            throw new IllegalArgumentException("Invalid cron expression: " + job.getCronExpression());
        }

        // Build JobDetail
        JobDetail jobDetail = JobBuilder.newJob(PrintMessageJob.class)
                .withIdentity(job.getName(), "group1")
                .usingJobData("jobId", job.getId())
                .usingJobData("jobName", job.getName())
                .usingJobData("attempt", 1)
                .usingJobData("maxAttempts", 3) // default, can be made configurable
                .storeDurably(false)
                .build();

        // Create Cron trigger
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(job.getName() + "Trigger", "group1")
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                .usingJobData("jobId", job.getId())
                .usingJobData("jobName", job.getName())
                .build();

        // schedule job
        if (!scheduler.checkExists(jobDetail.getKey())) {
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            // update trigger if job exists
            scheduler.rescheduleJob(trigger.getKey(), trigger);
        }

        job.setStatus("SCHEDULED");
        jobRepository.save(job);
    }

    // list all jobs (from MongoDB)
    public List<Job> listAllJobs() {
        return jobRepository.findAll();
    }

    // pause job by job id
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

    // resume job
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

    // delete job
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

    // fetch job logs
    public List<?> getJobLogs(String jobId) {
        return jobLogRepository.findByJobIdOrderByTimestampDesc(jobId)
                .stream().collect(Collectors.toList());
    }
    
    // save a job
    public Job saveJob(Job job) {
        return jobRepository.save(job);
    }

	public void performJobLogic(String jobName) {
		// TODO Auto-generated method stub
		
	}

	public void doSomething() {
		// TODO Auto-generated method stub
		
	}
}
