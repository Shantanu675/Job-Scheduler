package com.scheduler.backend.job;

import com.scheduler.backend.job.dto.CreateJobRequest;
import com.scheduler.backend.job.dto.JobResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(
            @Valid @RequestBody CreateJobRequest request
    ) {
        return jobService.createJob(request);
    }

    @GetMapping("/{id}")
    public JobResponse getJob(
            @PathVariable Long id
    ) {
        return jobService.getJob(id);
    }

    @GetMapping
    public List<JobResponse> getJobs(
            @RequestParam Long queueId
    ) {
        return jobService.getJobs(queueId);
    }
}