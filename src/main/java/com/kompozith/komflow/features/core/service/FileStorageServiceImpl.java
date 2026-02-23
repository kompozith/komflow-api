package com.kompozith.komflow.features.core.service;

import com.kompozith.komflow.exception.ObjectNotFoundException;
import com.kompozith.komflow.features.core.dto.FileDto;
import com.kompozith.komflow.features.core.entity.File;
import com.kompozith.komflow.features.core.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final FileRepository fileRepository;

    @Value("${app.file-storage.path:uploads}")
    private String storagePath;

    @Override
    public FileDto upload(MultipartFile multipartFile, String baseUrl) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String originalFilename = StringUtils.cleanPath(multipartFile.getOriginalFilename() != null ? multipartFile.getOriginalFilename() : "file");
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("File name is required");
        }
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        File file = new File();
        file.setName(originalFilename);
        file.setUrl("pending");
        File savedFile = fileRepository.save(file);

        try {
            Path uploadDir = resolveAndCreateUploadDir();
            String extension = extractExtension(originalFilename);
            String storedFileName = savedFile.getId() + extension;
            Path target = uploadDir.resolve(storedFileName).normalize();
            Files.copy(multipartFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            savedFile.setUrl(normalizedBaseUrl + "/files/" + savedFile.getId() + "/download");
            savedFile = fileRepository.save(savedFile);

            return new FileDto(
                    savedFile.getId(),
                    savedFile.getName(),
                    savedFile.getUrl(),
                    savedFile.getCreatedAt(),
                    savedFile.getUpdatedAt()
            );
        } catch (Exception e) {
            fileRepository.deleteById(savedFile.getId());
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public Resource loadAsResource(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ObjectNotFoundException(File.class.getSimpleName(), fileId));

        try {
            Path uploadDir = resolveAndCreateUploadDir();
            String extension = extractExtension(file.getName());
            Path target = uploadDir.resolve(fileId + extension).normalize();
            Resource resource = new UrlResource(target.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ObjectNotFoundException("UploadedFile", fileId);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to read stored file", e);
        }
    }

    private Path resolveAndCreateUploadDir() {
        try {
            Path uploadDir = Paths.get(storagePath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            return uploadDir;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize upload directory", e);
        }
    }

    private String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot);
    }
}
