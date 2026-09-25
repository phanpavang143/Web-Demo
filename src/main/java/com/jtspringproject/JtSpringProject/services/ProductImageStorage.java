package com.jtspringproject.JtSpringProject.services;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface ProductImageStorage {
	String store(MultipartFile image) throws IOException;
}