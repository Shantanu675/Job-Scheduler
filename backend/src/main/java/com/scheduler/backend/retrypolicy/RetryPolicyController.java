package com.scheduler.backend.retrypolicy;

import com.scheduler.backend.retrypolicy.dto.CreateRetryPolicyRequest;
import com.scheduler.backend.retrypolicy.dto.RetryPolicyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/retry-policies")
public class RetryPolicyController {

    private final RetryPolicyService retryPolicyService;

    public RetryPolicyController(
            RetryPolicyService retryPolicyService
    ) {
        this.retryPolicyService = retryPolicyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RetryPolicyResponse createPolicy(
            @Valid @RequestBody CreateRetryPolicyRequest request
    ) {
        return retryPolicyService.createPolicy(request);
    }

    @GetMapping
    public List<RetryPolicyResponse> getPolicies(
            @RequestParam Long projectId
    ) {
        return retryPolicyService.getPolicies(projectId);
    }

    @GetMapping("/{id}")
    public RetryPolicyResponse getPolicy(
            @PathVariable Long id
    ) {
        return retryPolicyService.getPolicy(id);
    }
}