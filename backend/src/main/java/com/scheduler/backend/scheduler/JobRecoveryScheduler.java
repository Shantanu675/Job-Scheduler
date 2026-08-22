package com.scheduler.backend.scheduler;

import com.scheduler.backend.job.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class JobRecoveryScheduler {

    private final JobRepository jobRepository;

    @Value("${scheduler.job.lease-timeout-seconds:30}")
    private long leaseTimeoutSeconds;

    public JobRecoveryScheduler(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void recoverStaleJobs() {

        Instant threshold =
                Instant.now().minusSeconds(leaseTimeoutSeconds);

        int recovered =
                jobRepository.recoverStaleJobs(threshold);

        if (recovered > 0) {
            System.out.println(
                    "Recovered " + recovered + " stale job(s)"
            );
        }
    }
}