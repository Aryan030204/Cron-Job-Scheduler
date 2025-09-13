package com.jobScheduler.repository;

import com.jobScheduler.model.JobLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface JobLogRepository extends MongoRepository<JobLog, String> {
    List<JobLog> findByJobIdOrderByTimestampDesc(String jobId);
}
