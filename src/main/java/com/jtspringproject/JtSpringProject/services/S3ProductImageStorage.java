package com.jtspringproject.JtSpringProject.services;

import java.io.IOException;
import java.util.UUID;
import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jtspringproject.JtSpringProject.configuration.AwsConfiguration.AwsSettings;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@ConditionalOnProperty(name = "aws.enabled", havingValue = "true")
public class S3ProductImageStorage implements ProductImageStorage {
	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final AwsSettings settings;

	public S3ProductImageStorage(S3Client s3Client, S3Presigner s3Presigner, AwsSettings settings) {
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
		this.settings = settings;
	}

	@Override
	public String store(MultipartFile image) throws IOException {
		if (image == null || image.isEmpty()) {
			return null;
		}
		if (image.getSize() > 5 * 1024 * 1024 || image.getContentType() == null
				|| !image.getContentType().startsWith("image/")) {
			throw new IOException("Only image files up to 5 MB are supported");
		}
		String key = "products/" + UUID.randomUUID() + "-" + image.getOriginalFilename();
		PutObjectRequest request = PutObjectRequest.builder().bucket(settings.bucket()).key(key)
				.contentType(image.getContentType()).build();
		s3Client.putObject(request, RequestBody.fromBytes(image.getBytes()));
		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofHours(1))
				.getObjectRequest(GetObjectRequest.builder().bucket(settings.bucket()).key(key).build()).build();
		return s3Presigner.presignGetObject(presignRequest).url().toString();
	}
}