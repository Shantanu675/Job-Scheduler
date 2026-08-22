package com.scheduler.backend.scheduler;

import com.scheduler.backend.job.Job;
import com.scheduler.backend.job.JobRepository;
import com.scheduler.backend.job.JobStatus;
import com.scheduler.backend.worker.Worker;
import com.scheduler.backend.worker.WorkerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class WorkerHealthScheduler {

    private final WorkerRepository workerRepository;
    private final JobRepository jobRepository;

    @Value("${scheduler.worker.heartbeat-timeout-seconds:30}")
    private long heartbeatTimeoutSeconds;

    public WorkerHealthScheduler(
            WorkerRepository workerRepository,
            JobRepository jobRepository
    ) {
        this.workerRepository = workerRepository;
        this.jobRepository = jobRepository;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void checkWorkers() {

        Instant now = Instant.now();

        Instant threshold =
                now.minusSeconds(heartbeatTimeoutSeconds);

        List<Worker> workers = workerRepository.findAll();

        for (Worker worker : workers) {

            if (!"ONLINE".equals(worker.getStatus())) {
                continue;
            }

            Instant lastHeartbeat =
                    worker.getLastHeartbeatAt();

            if (lastHeartbeat == null) {
                continue;
            }

            if (lastHeartbeat.isBefore(threshold)) {

                worker.setStatus("OFFLINE");

                workerRepository.save(worker);

                recoverWorkerJobs(worker, now);

                System.out.println(
                        "Worker " +
                                worker.getWorkerId() +
                                " marked OFFLINE"
                );
            }
        }
    }

    private void recoverWorkerJobs(
            Worker worker,
            Instant now
    ) {

        List<Job> jobs =
                jobRepository.findByWorkerIdAndStatusIn(
                        worker.getId(),
                        List.of(
                                JobStatus.CLAIMED,
                                JobStatus.RUNNING
                        )
                );

        for (Job job : jobs) {

            job.setStatus(JobStatus.PENDING);
            job.setWorkerId(null);
            job.setClaimedAt(null);
            job.setStartedAt(null);
            job.setAvailableAt(now);

            jobRepository.save(job);

            System.out.println(
                    "Recovered job " +
                            job.getId() +
                            " from worker " +
                            worker.getWorkerId()
            );
        }
    }
}