package com.novaerp.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StructuredLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(StructuredLoggingConfig.class);

    public StructuredLoggingConfig() {
        if (logger.isInfoEnabled()) {
            logger.info("Nova ERP v2 — Structured logging initialized");
        }
    }

}
