package com.kompozith.komflow.features.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactImportResultDto {
    private int importedCount;
    private int updatedCount;
    private int skippedCount;
    private int failedCount;
    private List<String> errors = new ArrayList<>();
}
