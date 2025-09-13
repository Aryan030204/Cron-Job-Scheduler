package com.jobScheduler.controller;

import com.jobScheduler.model.Job;
import com.jobScheduler.service.JobService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    

    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleJob(@RequestBody Job job) {
        try {
            // if job id is null, save to get an id (Mongo will assign)
            if (job.getId() == null) {
                job = jobService.saveJob(job);
            }
            jobService.scheduleJob(job);
            return ResponseEntity.ok("Job scheduled: " + job.getName());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(iae.getMessage());
        } catch (SchedulerException se) {
            return ResponseEntity.status(500).body("Scheduler error: " + se.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Job>> listJobs() {
        return ResponseEntity.ok(jobService.listAllJobs());
    }

    @PostMapping("/pause/{jobId}")
    public ResponseEntity<?> pause(@PathVariable String jobId) {
        try {
            boolean ok = jobService.pauseJob(jobId);
            return ok ? ResponseEntity.ok("Paused job " + jobId) :
                        ResponseEntity.badRequest().body("Job not found or cannot pause");
        } catch (SchedulerException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/resume/{jobId}")
    public ResponseEntity<?> resume(@PathVariable String jobId) {
        try {
            boolean ok = jobService.resumeJob(jobId);
            return ok ? ResponseEntity.ok("Resumed job " + jobId) :
                        ResponseEntity.badRequest().body("Job not found or cannot resume");
        } catch (SchedulerException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{jobId}")
    public ResponseEntity<?> delete(@PathVariable String jobId) {
        try {
            boolean ok = jobService.deleteJob(jobId);
            return ok ? ResponseEntity.ok("Deleted job " + jobId) :
                        ResponseEntity.badRequest().body("Job not found or cannot delete");
        } catch (SchedulerException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/logs/{jobId}")
    public ResponseEntity<?> logs(@PathVariable String jobId) {
        return ResponseEntity.ok(jobService.getJobLogs(jobId));
    }
}
