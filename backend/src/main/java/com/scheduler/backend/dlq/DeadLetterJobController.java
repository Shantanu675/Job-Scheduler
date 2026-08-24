package com.scheduler.backend.dlq;

import com.scheduler.backend.dlq.dto.DeadLetterJobResponse;
import com.scheduler.backend.job.Job;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dlq")
public class DeadLetterJobController {

    private final DeadLetterJobService deadLetterJobService;

    public DeadLetterJobController(
            DeadLetterJobService deadLetterJobService
    ) {
        this.deadLetterJobService = deadLetterJobService;
    }

    @GetMapping
    public List<DeadLetterJobResponse> getAll() {
        return deadLetterJobService.getAll();
    }

    @GetMapping("/{id}")
    public DeadLetterJobResponse getById(
            @PathVariable Long id
    ) {
        return deadLetterJobService.getById(id);
    }

    @PostMapping("/{id}/requeue")
    public Job requeue(
            @PathVariable Long id
    ) {
        return deadLetterJobService.requeue(id);
    }
}