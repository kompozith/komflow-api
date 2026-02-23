package com.kompozith.komflow.features.core.dto;

import com.kompozith.komflow.features.core.entity.FileMediaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileListResponseDto {
    private Page<FileDto> files;
    private Map<FileMediaType, Long> groupedByMediaType;
}
