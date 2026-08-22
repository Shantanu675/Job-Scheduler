package com.scheduler.backend.worker;

import com.scheduler.backend.job.Job;
import com.scheduler.backend.job.JobRepository;
import com.scheduler.backend.job.JobStatus;
import com.scheduler.backend.queue.Queue;
import com.scheduler.backend.queue.QueueRepository;
import com.scheduler.backend.worker.dto.RegisterWorkerRequest;
import com.scheduler.backend.worker.dto.WorkerResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final JobRepository jobRepository;
    private final QueueRepository queueRepository;

    public WorkerService(
            WorkerRepository workerRepository,
            JobRepository jobRepository,
            QueueRepository queueRepository
    ) {
        this.workerRepository = workerRepository;
        this.jobRepository = jobRepository;
        this.queueRepository = queueRepository;
    }

    @Transactional
    public WorkerResponse registerWorker(RegisterWorkerRequest request) {

        Worker worker = workerRepository
                .findByWorkerId(request.getWorkerId())
                .orElseGet(Worker::new);

        worker.setWorkerId(request.getWorkerId());
        worker.setHostname(request.getHostname());
        worker.setStatus("ONLINE");
        worker.setLastHeartbeatAt(Instant.now());

        worker = workerRepository.save(worker);

        return toResponse(worker);
    }

    @Transactional
    public WorkerResponse heartbeat(String workerId) {

        Worker worker = workerRepository
                .findByWorkerId(workerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Worker not found: " + workerId
                        )
                );

        worker.setStatus("ONLINE");
        worker.setLastHeartbeatAt(Instant.now());

        worker = workerRepository.save(worker);

        return toResponse(worker);
    }

    @Transactional
    public Job claimJob(String workerId, Long queueId) {

        Worker worker = workerRepository
                .findByWorkerId(workerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Worker not found: " + workerId
                        )
                );

        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Queue not found: " + queueId
                        )
                );

        /*
         * Count jobs that currently occupy a concurrency slot.
         *
         * CLAIMED  -> worker has claimed the job
         * RUNNING  -> worker is executing the job
         */
        long activeJobs = jobRepository.countActiveJobs(
                queueId,
                List.of(
                        JobStatus.CLAIMED,
                        JobStatus.RUNNING
                )
        );

        /*
         * Queue concurrency limit reached.
         */
        if (activeJobs >= queue.getMaxConcurrency()) {
            return null;
        }

        /*
         * Find the highest-priority available pending job.
         */
        Instant now = Instant.now();

        List<Job> jobs = jobRepository.findAvailableJobs(
                queueId,
                JobStatus.PENDING,
                now
        );

        /*
         * Try each candidate.
         *
         * claimJob() performs an atomic UPDATE:
         *
         * PENDING -> CLAIMED
         *
         * Therefore only one worker can successfully claim
         * the same job.
         */
        for (Job job : jobs) {

            Instant claimedAt = Instant.now();

            int updated = jobRepository.claimJob(
                    job.getId(),
                    worker.getId(),
                    JobStatus.PENDING,
                    claimedAt,
                    JobStatus.CLAIMED
            );

            if (updated == 1) {

                return jobRepository
                        .findById(job.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Job disappeared after claiming: "
                                                + job.getId()
                                )
                        );
            }
        }

        return null;
    }

    private WorkerResponse toResponse(Worker worker) {

        return new WorkerResponse(
                worker.getId(),
                worker.getWorkerId(),
                worker.getHostname(),
                worker.getStatus(),
                worker.getLastHeartbeatAt(),
                worker.getCreatedAt(),
                worker.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<WorkerResponse> getWorkers() {
        return workerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
}