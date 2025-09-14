# Distributed Cron Job Scheduler

A Spring Boot application for scheduling, managing, and logging jobs using Quartz and MongoDB. Supports REST API and interactive CLI for job management.

## Features

- Schedule jobs with cron expressions
- Pause, resume, and delete jobs
- Multiple job actions: print message, countdown, call API, write file, log to MongoDB, send email
- Job execution logs stored in MongoDB
- REST API and interactive console CLI

## Technologies

- Spring Boot
- Quartz Scheduler
- MongoDB (Spring Data)
- Lombok

## Getting Started

### Prerequisites

- Java 17+
- Maven
- MongoDB instance (see `src/main/resources/application.properties` for URI)

### Build & Run

#### Using Maven

```sh
./mvnw clean package
java -jar target/job-scheduler-1.0-SNAPSHOT.jar
```

#### Using Docker

```sh
docker build -t job-scheduler .
docker run -p 8080:8080 job-scheduler
```

### REST API Endpoints

- `POST /jobs/schedule` — Schedule a new job
- `GET /jobs/list` — List all jobs
- `POST /jobs/pause/{jobId}` — Pause a job
- `POST /jobs/resume/{jobId}` — Resume a job
- `DELETE /jobs/delete/{jobId}` — Delete a job
- `GET /jobs/logs/{jobId}` — Get job logs

### Console CLI

On startup, interact via console:

- `list` — List jobs
- `schedule <name> <cron>` — Schedule a job
- `pause <jobId>` — Pause job
- `resume <jobId>` — Resume job
- `delete <jobId>` — Delete job
- `logs <jobId>` — Show job logs
- `help` — Show help
