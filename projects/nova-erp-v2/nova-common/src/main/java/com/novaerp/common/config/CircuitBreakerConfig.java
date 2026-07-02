package com.novaerp.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

/**
 * Resilience4j configuration for circuit breakers, retries, bulkheads, and time limiters.
 */
@Configuration
public class CircuitBreakerConfig {

    @Bean("defaultCircuitBreakerRegistry")
    public io.github.resilience4j.circuitbreaker.CircuitBreakerConfig defaultCircuitBreaker() {
        return io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
    }

    @Bean("financialCircuitBreakerRegistry")
    public io.github.resilience4j.circuitbreaker.CircuitBreakerConfig financialCircuitBreaker() {
        return io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(30)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .slidingWindowSize(5)
                .minimumNumberOfCalls(2)
                .permittedNumberOfCallsInHalfOpenState(1)
                .build();
    }

    @Bean("defaultRetryConfig")
    public io.github.resilience4j.retry.RetryConfig defaultRetry() {
        return io.github.resilience4j.retry.RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .intervalFunction(io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                        Duration.ofMillis(500), 2.0, Duration.ofSeconds(5)))
                .retryExceptions(java.net.ConnectException.class, java.io.IOException.class)
                .build();
    }

    @Bean("defaultBulkheadConfig")
    public io.github.resilience4j.bulkhead.BulkheadConfig defaultBulkhead() {
        return io.github.resilience4j.bulkhead.BulkheadConfig.custom()
                .maxConcurrentCalls(20)
                .maxWaitDuration(Duration.ofMillis(100))
                .build();
    }

    @Bean("defaultTimeLimiterConfig")
    public io.github.resilience4j.timelimiter.TimeLimiterConfig defaultTimeLimiter() {
        return io.github.resilience4j.timelimiter.TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))
                .cancelRunningFuture(true)
                .build();
    }

}
