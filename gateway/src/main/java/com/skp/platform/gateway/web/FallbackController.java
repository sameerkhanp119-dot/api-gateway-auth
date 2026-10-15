package com.skp.platform.gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** Target of circuit-breaker fallbacks: a fast, explicit 503 instead of a hanging request. */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/{service}")
    public ResponseEntity<ProblemDetail> fallback(@PathVariable String service) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                service + " is temporarily unavailable. Retry after a short delay.");
        problem.setTitle("Service unavailable");
        problem.setType(URI.create("https://sameerkhanpathan.com/problems/upstream-unavailable"));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "5")
                .body(problem);
    }
}
