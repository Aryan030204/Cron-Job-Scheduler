package com.jobScheduler.repository;

import com.jobScheduler.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
    // You get CRUD methods out of the box (save, findById, delete, etc.)
}
