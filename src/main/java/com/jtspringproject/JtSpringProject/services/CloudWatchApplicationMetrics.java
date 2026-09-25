package com.jtspringproject.JtSpringProject.services;

import java.time.Instant;
import java.util.Collections;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

@Service
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true")
public class CloudWatchApplicationMetrics implements ApplicationMetrics {
	private static final String METRIC_NAMESPACE = System.getenv().getOrDefault("AWS_METRICS_NAMESPACE",
			"jt-spring-commerce");
	private final CloudWatchClient cloudWatchClient;

	public CloudWatchApplicationMetrics(CloudWatchClient cloudWatchClient) {
		this.cloudWatchClient = cloudWatchClient;
	}

	@Override
	public void increment(String metricName) {
		MetricDatum datum = MetricDatum.builder().metricName(metricName).unit(StandardUnit.COUNT).value(1D)
				.timestamp(Instant.now()).dimensions(Collections.singletonList(Dimension.builder().name("Application")
					.value(METRIC_NAMESPACE).build())).build();
		cloudWatchClient.putMetricData(PutMetricDataRequest.builder().namespace(METRIC_NAMESPACE)
				.metricData(datum).build());
	}
}
