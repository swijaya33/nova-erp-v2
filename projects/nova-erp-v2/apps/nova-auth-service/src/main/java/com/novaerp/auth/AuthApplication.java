package com.novaerp.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Nova ERP v2 — Authentication & Authorization Microservice (Port: 4021) */
@SpringBootApplication(scanBasePackages = "com.novaerp")
public class AuthApplication {
    public static void main(String[] args) { SpringApplication.run(AuthApplication.class, args); }
}
