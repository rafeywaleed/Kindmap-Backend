package com.exotech.kindmap.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Getter
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public String getFullBaseUrl() {
        String cleanBaseUrl = baseUrl.replaceAll("/$", "");
        String cleanContextPath = contextPath.replaceAll("^/", "");
        if (!cleanContextPath.isEmpty()) {
            return cleanBaseUrl + "/" + cleanContextPath;
        }
        return cleanBaseUrl;
    }
}