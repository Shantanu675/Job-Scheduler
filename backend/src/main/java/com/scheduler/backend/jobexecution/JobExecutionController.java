package com.scheduler.backend.jobexecution;

import com.scheduler.backend.job.Job;
import com.scheduler.backend.job.JobService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workers")
public class JobExecutionController {

    private final JobService jobService;

    public JobExecutionController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/{workerId}/jobs/{jobId}/execute")
    public Job executeJob(
            @PathVariable String workerId,
            @PathVariable Long jobId
    ) {
        // Current worker IDs are represented as worker-1,
        // while jobs store the numeric worker database ID.
        Long numericWorkerId = Long.parseLong(
                workerId.replace("worker-", "")
        );

        return jobService.executeJob(jobId, numericWorkerId);
    }
}