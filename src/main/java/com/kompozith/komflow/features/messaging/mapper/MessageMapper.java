package com.kompozith.komflow.features.messaging.mapper;

import com.kompozith.komflow.features.core.dto.FileDto;
import com.kompozith.komflow.features.core.entity.File;
import com.kompozith.komflow.features.core.entity.FileMediaType;
import com.kompozith.komflow.features.messaging.dto.CreateMessageDto;
import com.kompozith.komflow.features.messaging.dto.EventDto;
import com.kompozith.komflow.features.messaging.dto.MessageDto;
import com.kompozith.komflow.features.messaging.entity.Event;
import com.kompozith.komflow.features.messaging.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    ZoneId DEFAULT_EVENT_ZONE = ZoneId.of("GMT");

    @Mapping(target = "attachments", expression = "java(mapAttachments(message.getAttachments()))")
    @Mapping(target = "attachmentCount", expression = "java(message.getAttachments() != null ? message.getAttachments().size() : 0)")
    @Mapping(target = "event", expression = "java(toEventDto(message.getEvent()))")
    MessageDto messageToMessageDto(Message message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "attachments", expression = "java(mapAttachmentDtos(createMessageDto.getAttachments()))")
    Message createMessageDtoToMessage(CreateMessageDto createMessageDto);

    default List<FileDto> mapAttachments(List<File> attachments) {
        if (attachments == null) {
            return null;
        }
        return attachments.stream().map(this::toFileDto).collect(Collectors.toList());
    }

    default List<File> mapAttachmentDtos(List<FileDto> attachments) {
        if (attachments == null) {
            return null;
        }
        return attachments.stream().map(this::toFile).collect(Collectors.toList());
    }

    default FileDto toFileDto(File file) {
        if (file == null) {
            return null;
        }
        return new FileDto(
                file.getId(),
                file.getName(),
                file.getUrl(),
                FileMediaType.fromFileName(file.getName()),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }

    default File toFile(FileDto fileDto) {
        if (fileDto == null) {
            return null;
        }
        File file = new File();
        file.setId(fileDto.getId());
        file.setName(fileDto.getName());
        file.setUrl(fileDto.getUrl());
        return file;
    }

    default EventDto toEventDto(Event event) {
        if (event == null) {
            return null;
        }
        LocalDate startDate = null;
        LocalTime startTime = null;
        LocalDate endDate = null;
        LocalTime endTime = null;

        ZoneId zoneId = resolveZoneId(event.getTimezone());
        if (event.getStartAt() != null) {
            ZonedDateTime start = event.getStartAt().atZone(zoneId);
            startDate = start.toLocalDate();
            startTime = start.toLocalTime().withSecond(0).withNano(0);
        }
        if (event.getEndAt() != null) {
            ZonedDateTime end = event.getEndAt().atZone(zoneId);
            endDate = end.toLocalDate();
            endTime = end.toLocalTime().withSecond(0).withNano(0);
        }

        return new EventDto(
                event.getId(),
                event.getTitle(),
                event.getSlug(),
                event.getDescription(),
                event.getLocation(),
                event.getSubtitle(),
                event.getAddress(),
                event.getMode(),
                event.getMeetingUrl(),
                List.of(),
                List.of(),
                startDate,
                event.getStartAt(),
                event.getEndAt(),
                startDate,
                startTime,
                endDate,
                endTime,
                normalizeTimezone(event.getTimezone()),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }

    default String normalizeTimezone(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_EVENT_ZONE.getId();
        }
        return timezone.trim();
    }

    default ZoneId resolveZoneId(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return DEFAULT_EVENT_ZONE;
        }
        try {
            return ZoneId.of(timezone.trim());
        } catch (Exception e) {
            return DEFAULT_EVENT_ZONE;
        }
    }
}
