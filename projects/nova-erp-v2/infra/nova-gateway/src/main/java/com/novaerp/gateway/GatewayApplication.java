package com.novaerp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway — Spring Cloud Gateway for Nova ERP v2 microservices.
 * 
 * Responsibilities:
 * - Dynamic service routing via Eureka (no hardcoded URLs)
 * - JWT validation middleware on all requests
 * - Rate limiting per IP/client using Redis-backed sliding window counter
 * - CORS policy enforcement
 * - Circuit breaker aggregation monitoring downstream services' health status
 * - W3C TraceContext header propagation for distributed tracing
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
