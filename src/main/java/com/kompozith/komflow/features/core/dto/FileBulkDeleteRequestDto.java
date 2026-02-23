package com.kompozith.komflow.features.core.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileBulkDeleteRequestDto {

    @NotEmpty
    private List<Long> fileIds;

    private boolean orphanOnly = true;
}
