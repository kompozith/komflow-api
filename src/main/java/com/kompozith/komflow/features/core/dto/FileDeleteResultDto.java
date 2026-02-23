package com.kompozith.komflow.features.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDeleteResultDto {
    private int deletedInDatabase;
    private int deletedInStorage;
    private List<Long> failedFileIds = new ArrayList<>();
    private List<Long> skippedReferencedFileIds = new ArrayList<>();
}
