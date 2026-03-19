package com.kompozith.komflow.features.messaging.mapper;

import com.kompozith.komflow.features.messaging.dto.CreateEventDto;
import com.kompozith.komflow.features.messaging.dto.EventDto;
import com.kompozith.komflow.features.messaging.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "highlights", ignore = true)
    @Mapping(target = "agenda", ignore = true)
    @Mapping(target = "eventDate", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "endTime", ignore = true)
    @Mapping(target = "registrationWorkflowSteps", ignore = true)
    EventDto eventToEventDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "highlights", ignore = true)
    @Mapping(target = "agenda", ignore = true)
    @Mapping(target = "startAt", ignore = true)
    @Mapping(target = "endAt", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "registrationWorkflowSteps", ignore = true)
    Event createEventDtoToEvent(CreateEventDto createEventDto);
}
