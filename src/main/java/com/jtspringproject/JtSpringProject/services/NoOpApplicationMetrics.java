package com.jtspringproject.JtSpringProject.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "aws.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpApplicationMetrics implements ApplicationMetrics {
	@Override
	public void increment(String metricName) {
	}
}