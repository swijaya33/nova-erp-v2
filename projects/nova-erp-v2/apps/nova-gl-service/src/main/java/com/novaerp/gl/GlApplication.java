package com.novaerp.gl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Nova ERP v2 — General Ledger Microservice (Port: 4031) */
@SpringBootApplication(scanBasePackages = "com.novaerp")
public class GlApplication {
    public static void main(String[] args) { SpringApplication.run(GlApplication.class, args); }
}
