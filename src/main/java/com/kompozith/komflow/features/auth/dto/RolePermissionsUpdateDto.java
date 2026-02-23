package com.kompozith.komflow.features.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionsUpdateDto {
    @NotNull(message = "permissionCodeList cannot be null")
    private List<String> permissionCodeList;
}
