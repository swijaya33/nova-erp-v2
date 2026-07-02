package com.novaerp.common.tracing;

import io.opentelemetry.api.OpenTelemetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryAutoConfig {

    @Bean("novaTracer")
    public io.opentelemetry.api.trace.Tracer novaTracer() {
        return OpenTelemetry.noop().getTracer("nova-erp");
    }

}
