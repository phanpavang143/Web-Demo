package com.jtspringproject.JtSpringProject.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.jtspringproject.JtSpringProject.configuration.AwsConfiguration.AwsSettings;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true")
public class SqsOrderEventPublisher implements OrderEventPublisher {
	private final SqsClient sqsClient;
	private final AwsSettings settings;

	public SqsOrderEventPublisher(SqsClient sqsClient, AwsSettings settings) {
		this.sqsClient = sqsClient;
		this.settings = settings;
	}

	@Override
	public void publish(String event) {
		if (!settings.queueUrl().isEmpty()) {
			sqsClient.sendMessage(SendMessageRequest.builder().queueUrl(settings.queueUrl()).messageBody(event).build());
		}
	}
}