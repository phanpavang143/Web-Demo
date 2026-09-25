package com.jtspringproject.JtSpringProject.services;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "aws.enabled", havingValue = "false", matchIfMissing = true)
public class LocalProductImageStorage implements ProductImageStorage {
	@Override
	public String store(MultipartFile image) throws IOException {
		return image == null || image.isEmpty() ? null : image.getOriginalFilename();
	}
}