package com.kompozith.komflow.features.messaging.controller;

import com.kompozith.komflow.features.messaging.dto.CreateCampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDto;
import com.kompozith.komflow.features.messaging.service.CampaignService;
import com.kompozith.komflow.util.SimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Campaign Management", description = "APIs for managing email campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    @PreAuthorize("hasAuthority('CAMPAIGN_CREATE')")
    @PostMapping
    @Operation(summary = "Create a new campaign", description = "Create a new email campaign in the system")
    public ResponseEntity<CampaignDto> create(@Valid @RequestBody CreateCampaignDto createCampaignDto) {
        CampaignDto campaignDto = campaignService.create(createCampaignDto);
        return ResponseEntity.ok(campaignDto);
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_LIST')")
    @GetMapping
    @Operation(summary = "Get all campaigns", description = "Retrieve a paginated list of all campaigns")
    public ResponseEntity<Page<CampaignDto>> findAll(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<CampaignDto> campaigns = campaignService.findAll(pageable);
        return ResponseEntity.ok(campaigns);
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_SHOW')")
    @GetMapping("/{id}")
    @Operation(summary = "Get campaign by ID", description = "Retrieve a specific campaign by its ID")
    public ResponseEntity<CampaignDto> findById(@PathVariable Long id) {
        CampaignDto campaignDto = campaignService.findById(id);
        return ResponseEntity.ok(campaignDto);
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_UPDATE')")
    @PutMapping("/{id}")
    @Operation(summary = "Update campaign", description = "Update an existing campaign by its ID")
    public ResponseEntity<CampaignDto> update(@PathVariable Long id, @Valid @RequestBody CreateCampaignDto createCampaignDto) {
        CampaignDto campaignDto = campaignService.update(id, createCampaignDto);
        return ResponseEntity.ok(campaignDto);
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_DELETE')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete campaign", description = "Delete a campaign by its ID")
    public ResponseEntity<SimpleResponse> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return ResponseEntity.ok(new SimpleResponse<>("Campaign deleted successfully", null));
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_SEND')")
    @PostMapping("/{id}/send")
    @Operation(summary = "Send campaign", description = "Send an email campaign to all associated contacts")
    public ResponseEntity<SimpleResponse> sendCampaign(@PathVariable Long id) {
        campaignService.sendCampaign(id);
        return ResponseEntity.ok(new SimpleResponse<>("Campaign sent successfully", null));
    }
}