package com.kompozith.komflow.features.messaging.controller;

import com.kompozith.komflow.features.messaging.dto.CampaignContactResultDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDetailsDto;
import com.kompozith.komflow.features.messaging.dto.CampaignEditabilityDto;
import com.kompozith.komflow.features.messaging.dto.CampaignResultsSummaryDto;
import com.kompozith.komflow.features.messaging.dto.CreateCampaignDto;
import com.kompozith.komflow.features.messaging.dto.CampaignDto;
import com.kompozith.komflow.features.messaging.dto.ScheduleCampaignDto;
import com.kompozith.komflow.features.messaging.entity.CampaignSendStatus;
import com.kompozith.komflow.features.messaging.service.CampaignEventStreamService;
import com.kompozith.komflow.features.messaging.service.CampaignService;
import com.kompozith.komflow.util.SimpleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Campaign Management", description = "APIs for managing email campaigns")
public class CampaignController {

    private final CampaignService campaignService;
    private final CampaignEventStreamService campaignEventStreamService;

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
    @Operation(summary = "Get campaign details by ID", description = "Retrieve a specific campaign with all its constituent elements (contacts, tags, CC, CCI)")
    public ResponseEntity<CampaignDetailsDto> getCampaignById(@PathVariable Long id) {
        CampaignDetailsDto campaignDetailsDto = campaignService.findById(id);
        return ResponseEntity.ok(campaignDetailsDto);
    }

    @PreAuthorize("hasAnyAuthority('CAMPAIGN_SHOW', 'CAMPAIGN_UPDATE')")
    @GetMapping("/{id}/editability")
    @Operation(summary = "Check campaign editability", description = "Check whether a campaign can currently be edited based on its status and schedule")
    public ResponseEntity<CampaignEditabilityDto> getEditability(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getEditability(id));
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

    @PreAuthorize("hasAuthority('CAMPAIGN_SUBMIT')")
    @PutMapping("/{id}/submit")
    @Operation(summary = "Submit campaign", description = "Submit campaign to all associated contacts. If scheduled date is set, campaign will be scheduled for that time.")
    public ResponseEntity<SimpleResponse> sendCampaign(@PathVariable Long id) {
        campaignService.sendCampaign(id);
        return ResponseEntity.ok(new SimpleResponse<>("Campaign submitted successfully", null));
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_SUBMIT')")
    @PutMapping("/{id}/resubmit")
    @Operation(
            summary = "Resubmit failed campaign",
            description = "Resubmit a FAILED or PARTIAL_SUCCESS campaign. Only contacts whose previous send " +
                    "attempt failed are retried; contacts already reached successfully are skipped."
    )
    public ResponseEntity<SimpleResponse> resubmitCampaign(@PathVariable Long id) {
        campaignService.resubmitCampaign(id);
        return ResponseEntity.ok(new SimpleResponse<>("Campaign resubmission started", null));
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_SUBMIT')")
    @PutMapping("/{id}/schedule")
    @Operation(summary = "Schedule campaign", description = "Schedule a campaign to be sent at a specific date and time")
    public ResponseEntity<CampaignDto> scheduleCampaign(@PathVariable Long id, @Valid @RequestBody ScheduleCampaignDto scheduleDto) {
        CampaignDto campaignDto = campaignService.scheduleCampaign(id, scheduleDto.getScheduledAt());
        return ResponseEntity.ok(campaignDto);
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_SUBMIT')")
    @PutMapping("/{id}/cancel-schedule")
    @Operation(summary = "Cancel campaign schedule", description = "Cancel a scheduled campaign and return it to DRAFT status. Only applicable if the scheduled date has not yet been reached.")
    public ResponseEntity<SimpleResponse<CampaignDto>> cancelSchedule(@PathVariable Long id) {
        CampaignDto campaignDto = campaignService.cancelSchedule(id);
        return ResponseEntity.ok(new SimpleResponse<>("Campaign schedule cancelled successfully", campaignDto));
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_SHOW')")
    @GetMapping("/{id}/results")
    @Operation(
            summary = "Campaign send results",
            description = "Paginated list of all contacts processed by the campaign, " +
                    "with their send status. Filter by ?status=SUCCESS or ?status=FAILED. " +
                    "Optionally filter by contact name/email with ?search=term."
    )
    public ResponseEntity<Page<CampaignContactResultDto>> getCampaignResults(
            @PathVariable Long id,
            @RequestParam(required = false) CampaignSendStatus status,
            @RequestParam(required = false) String search,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return ResponseEntity.ok(campaignService.getCampaignResults(id, status, search, pageable));
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_SHOW')")
    @GetMapping("/{id}/results/summary")
    @Operation(
            summary = "Campaign send results summary",
            description = "Returns success / failed / total counts for a campaign execution."
    )
    public ResponseEntity<CampaignResultsSummaryDto> getCampaignResultsSummary(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignResultsSummary(id));
    }

    @PreAuthorize("hasAuthority('CAMPAIGN_SHOW')")
    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Campaign events", description = "Stream campaign execution events in real time")
    public SseEmitter streamCampaignEvents(@PathVariable Long id) {
        return campaignEventStreamService.subscribe(id);
    }
}
