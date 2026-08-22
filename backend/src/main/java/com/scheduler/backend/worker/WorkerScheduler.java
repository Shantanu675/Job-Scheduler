package com.scheduler.backend.worker;

import com.scheduler.backend.job.Job;
import com.scheduler.backend.job.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkerScheduler {

    private final WorkerService workerService;
    private final JobService jobService;

    /*
     * These workers represent worker processes running
     * inside this scheduler application.
     *
     * Later, these can be moved to environment variables
     * when running real distributed workers.
     */
    private final List<String> workerIds = List.of(
            "worker-1",
            "worker-2"
    );

    /*
     * Queues that workers should poll.
     */
    private final List<Long> queueIds = List.of(
            1L,
            2L
    );

    public WorkerScheduler(
            WorkerService workerService,
            JobService jobService
    ) {
        this.workerService = workerService;
        this.jobService = jobService;
    }

    /**
     * Automatically sends heartbeats.
     *
     * Runs every 5 seconds.
     */
    @Scheduled(fixedRate = 5000)
    public void heartbeatWorkers() {

        for (String workerId : workerIds) {

            try {
                workerService.heartbeat(workerId);

            } catch (Exception e) {
                System.err.println(
                        "Heartbeat failed for "
                                + workerId
                                + ": "
                                + e.getMessage()
                );
            }
        }
    }

    /**
     * Automatically polls queues for jobs.
     *
     * Runs every 1 second.
     */
    @Scheduled(fixedRate = 1000)
    public void pollJobs() {

        for (String workerId : workerIds) {

            for (Long queueId : queueIds) {

                try {

                    Job job = workerService.claimJob(
                            workerId,
                            queueId
                    );

                    if (job == null) {
                        continue;
                    }

                    System.out.println(
                            "Worker "
                                    + workerId
                                    + " claimed job "
                                    + job.getId()
                                    + " from queue "
                                    + queueId
                    );

                    executeJob(workerId, job);

                } catch (Exception e) {

                    System.err.println(
                            "Worker "
                                    + workerId
                                    + " failed while polling queue "
                                    + queueId
                                    + ": "
                                    + e.getMessage()
                    );
                }
            }
        }
    }

    private void executeJob(
            String workerId,
            Job job
    ) {

        try {

            /*
             * Current WorkerController uses worker-1 -> 1,
             * worker-2 -> 2.
             */
            Long numericWorkerId = Long.parseLong(
                    workerId.replace("worker-", "")
            );

            Job executedJob = jobService.executeJob(
                    job.getId(),
                    numericWorkerId
            );

            System.out.println(
                    "Worker "
                            + workerId
                            + " completed job "
                            + executedJob.getId()
                            + " with status "
                            + executedJob.getStatus()
            );

        } catch (Exception e) {

            System.err.println(
                    "Execution failed for job "
                            + job.getId()
                            + ": "
                            + e.getMessage()
            );
        }
    }
}