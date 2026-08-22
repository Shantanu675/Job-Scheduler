package com.scheduler.backend.queue;

import com.scheduler.backend.queue.dto.CreateQueueRequest;
import com.scheduler.backend.queue.dto.QueueResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QueueResponse createQueue(
            @Valid @RequestBody CreateQueueRequest request
    ) {
        return queueService.createQueue(request);
    }

    @GetMapping
    public List<QueueResponse> getQueues(
            @RequestParam Long projectId
    ) {
        return queueService.getQueues(projectId);
    }

    @GetMapping("/{id}")
    public QueueResponse getQueue(
            @PathVariable Long id
    ) {
        return queueService.getQueue(id);
    }
}