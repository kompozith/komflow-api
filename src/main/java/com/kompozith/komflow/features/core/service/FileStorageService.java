package com.kompozith.komflow.features.core.service;

import com.kompozith.komflow.features.core.dto.FileDto;
import com.kompozith.komflow.features.core.dto.FileDeleteResultDto;
import com.kompozith.komflow.features.core.dto.FileListResponseDto;
import com.kompozith.komflow.features.core.dto.OrphanFileCleanupResultDto;
import com.kompozith.komflow.features.core.entity.FileMediaType;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

    FileDto upload(MultipartFile multipartFile, String baseUrl);

    Resource loadAsResource(Long fileId);

    FileListResponseDto findAll(Pageable pageable, String search, FileMediaType mediaType, boolean orphanOnly);

    Page<FileDto> findOrphans(Pageable pageable, String search, FileMediaType mediaType);

    OrphanFileCleanupResultDto deleteOrphans(String search, FileMediaType mediaType);

    FileDeleteResultDto deleteFilesByIds(List<Long> fileIds, boolean orphanOnly);
}
