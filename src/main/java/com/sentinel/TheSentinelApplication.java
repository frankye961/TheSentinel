package com.sentinel;

import com.sentinel.config.AssessmentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AssessmentProperties.class)
public class TheSentinelApplication {
    static void main(String[] args) {
        SpringApplication.run(TheSentinelApplication.class, args);
    }
}
