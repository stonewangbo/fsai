package com.cat.fsai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = false)
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfiguration {
	
}
