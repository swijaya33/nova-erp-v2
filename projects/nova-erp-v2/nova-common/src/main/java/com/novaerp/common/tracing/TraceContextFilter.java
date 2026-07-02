package com.novaerp.common.tracing;

import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class TraceContextFilter implements Filter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(TraceContextFilter.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_TRACE_ID = "traceId";

    private final io.opentelemetry.api.trace.Tracer tracer;

    public TraceContextFilter(io.opentelemetry.api.trace.Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        try {
            MDC.put(MDC_CORRELATION_ID, correlationId);
            var span = tracer.spanBuilder("HTTP " + httpRequest.getMethod() + " " + httpRequest.getRequestURI()).startSpan();
            try (var scope = span.makeCurrent()) {
                var spanContext = Span.current().getSpanContext();
                if (spanContext.isValid()) {
                    MDC.put(MDC_TRACE_ID, spanContext.getTraceId());
                    logger.info("Request: {} | correlationId={} | traceId={}", httpRequest.getRequestURI(), correlationId, spanContext.getTraceId());
                } else {
                    logger.info("Request: {} | correlationId={}", httpRequest.getRequestURI(), correlationId);
                }
                chain.doFilter(request, response);
            } finally {
                span.end();
            }
        } catch (Exception e) {
            logger.error("Trace filter error", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

}
