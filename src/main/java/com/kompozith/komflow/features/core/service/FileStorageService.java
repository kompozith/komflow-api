package com.kompozith.komflow.features.core.service;

import com.kompozith.komflow.features.core.dto.FileDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileDto upload(MultipartFile multipartFile, String baseUrl);

    Resource loadAsResource(Long fileId);
}

