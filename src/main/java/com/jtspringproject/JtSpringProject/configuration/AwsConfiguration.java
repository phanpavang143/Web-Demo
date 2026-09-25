package com.jtspringproject.JtSpringProject.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true")
public class AwsConfiguration {

	@Bean
	public S3Client s3Client(AwsSettings settings) {
		return S3Client.builder().region(settings.region()).credentialsProvider(DefaultCredentialsProvider.create()).build();
	}

	@Bean
	public S3Presigner s3Presigner(AwsSettings settings) {
		return S3Presigner.builder().region(settings.region()).credentialsProvider(DefaultCredentialsProvider.create()).build();
	}

	@Bean
	public SqsClient sqsClient(AwsSettings settings) {
		return SqsClient.builder().region(settings.region()).credentialsProvider(DefaultCredentialsProvider.create()).build();
	}

	@Bean
	public CloudWatchClient cloudWatchClient(AwsSettings settings) {
		return CloudWatchClient.builder().region(settings.region()).credentialsProvider(DefaultCredentialsProvider.create()).build();
	}

	@Bean
	public AwsSettings awsSettings() {
		return new AwsSettings();
	}

	public static class AwsSettings {
		private final Region region = Region.of(System.getenv().getOrDefault("AWS_REGION", "ap-southeast-1"));
		private final String bucket = System.getenv().getOrDefault("AWS_S3_PRODUCT_BUCKET", "");
		private final String queueUrl = System.getenv().getOrDefault("AWS_SQS_ORDER_QUEUE_URL", "");

		public Region region() {
			return region;
		}

		public String bucket() {
			return bucket;
		}

		public String queueUrl() {
			return queueUrl;
		}
	}
}