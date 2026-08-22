package com.scheduler.backend.worker;

import com.scheduler.backend.job.Job;
import com.scheduler.backend.worker.dto.RegisterWorkerRequest;
import com.scheduler.backend.worker.dto.WorkerResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkerResponse registerWorker(
            @Valid @RequestBody RegisterWorkerRequest request
    ) {
        return workerService.registerWorker(request);
    }

    @PostMapping("/{workerId}/heartbeat")
    public WorkerResponse heartbeat(
            @PathVariable String workerId
    ) {
        return workerService.heartbeat(workerId);
    }

    @PostMapping("/{workerId}/claim")
    public ResponseEntity<Job> claimJob(
            @PathVariable String workerId,
            @RequestParam Long queueId
    ) {
        Job job = workerService.claimJob(workerId, queueId);

        if (job == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(job);
    }

    @GetMapping
    public List<WorkerResponse> getWorkers() {
        return workerService.getWorkers();
    }
}