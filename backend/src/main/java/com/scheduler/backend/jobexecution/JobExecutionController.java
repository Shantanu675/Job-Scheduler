package com.scheduler.backend.jobexecution;

import com.scheduler.backend.job.Job;
import com.scheduler.backend.job.JobService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class JobExecutionController {

    private final JobService jobService;
    private final JobExecutionRepository jobExecutionRepository;

    public JobExecutionController(
            JobService jobService,
            JobExecutionRepository jobExecutionRepository
    ) {
        this.jobService = jobService;
        this.jobExecutionRepository = jobExecutionRepository;
    }

    @PostMapping("/workers/{workerId}/jobs/{jobId}/execute")
    public Job executeJob(
            @PathVariable String workerId,
            @PathVariable Long jobId
    ) {

        Long numericWorkerId = Long.parseLong(
                workerId.replace("worker-", "")
        );

        return jobService.executeJob(
                jobId,
                numericWorkerId
        );
    }

    @GetMapping("/jobs/{jobId}/executions")
    public List<JobExecution> getExecutions(
            @PathVariable Long jobId
    ) {

        jobService.verifyJobOwnership(jobId);

        return jobExecutionRepository
                .findByJobIdOrderByAttemptNumberAsc(jobId);
    }
}