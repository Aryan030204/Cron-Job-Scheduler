package com.jobScheduler.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "job_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobLog {
    @Id
    private String id;

    private String jobId;           // reference to Job.id
    private String jobName;         // convenience
    private LocalDateTime timestamp;
    private String status;          // RUNNING / SUCCESS / FAILED
    private String message;         // error message or info
    private int attempt;            // attempt number
    private long durationMs;        // execution time in ms
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getAttempt() {
		return attempt;
	}
	public void setAttempt(int attempt) {
		this.attempt = attempt;
	}
	public long getDurationMs() {
		return durationMs;
	}
	public void setDurationMs(long durationMs) {
		this.durationMs = durationMs;
	}
	public static Object builder() {
		// TODO Auto-generated method stub
		return null;
	}
}
