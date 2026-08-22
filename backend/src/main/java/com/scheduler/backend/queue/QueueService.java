package com.scheduler.backend.queue;

import com.scheduler.backend.queue.dto.CreateQueueRequest;
import com.scheduler.backend.queue.dto.QueueResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QueueService {

    private final QueueRepository queueRepository;

    public QueueService(QueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    @Transactional
    public QueueResponse createQueue(CreateQueueRequest request) {

        if (queueRepository.existsByProjectIdAndName(
                request.getProjectId(),
                request.getName()
        )) {
            throw new IllegalArgumentException(
                    "Queue already exists for this project"
            );
        }

        Queue queue = new Queue();

        queue.setProjectId(request.getProjectId());
        queue.setName(request.getName());
        queue.setPriority(request.getPriority());
        queue.setMaxConcurrency(request.getMaxConcurrency());

        Queue savedQueue = queueRepository.save(queue);

        return QueueResponse.from(savedQueue);
    }

    @Transactional(readOnly = true)
    public List<QueueResponse> getQueues(Long projectId) {

        return queueRepository
                .findByProjectId(projectId)
                .stream()
                .map(QueueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QueueResponse getQueue(Long id) {

        Queue queue = queueRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Queue not found"
                        )
                );

        return QueueResponse.from(queue);
    }
}