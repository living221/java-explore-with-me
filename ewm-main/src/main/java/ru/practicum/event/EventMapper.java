package ru.practicum.event;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.location.model.Location;
import ru.practicum.user.RatingScore;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@UtilityClass
public class EventMapper {
    public static EventShortDto toEventShortDto(Event event, CategoryDto categoryDto, UserShortDto initiator) {
        EventShortDto eventShortDto = EventShortDto.builder()
                .id(event.getId())
                .category(categoryDto)
                .annotation(event.getAnnotation())
                .confirmedRequests(0)
                .eventDate(event.getEventDate())
                .initiator(initiator)
                .paid(event.getPaid())
                .title(event.getTitle())
                .build();

        if (Objects.isNull(event.getRatings())) {
            eventShortDto.setRating(0);
        } else {
            eventShortDto.setRating(RatingScore.calculate(event.getRatings()));
        }
        return eventShortDto;
    }

    public static EventFullDto toEventFullDto(Event event,
                                              CategoryDto categoryDto,
                                              UserShortDto initiator,
                                              LocationDto locationDto) {
        return EventFullDto.builder()
                .id(event.getId())
                .category(categoryDto)
                .annotation(event.getAnnotation())
                .confirmedRequests(Optional.ofNullable(event.getParticipants()).orElse(Set.of()).size())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(initiator)
                .location(locationDto)
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .build();
    }

    public static Event toEvent(NewEventDto newEventDto,
                                Category category,
                                Location location,
                                User initiator) {
        return Event.builder()
                .category(category)
                .annotation(newEventDto.getAnnotation())
                .createdOn(LocalDateTime.now())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .initiator(initiator)
                .location(location)
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .state(EventState.PENDING)
                .title(newEventDto.getTitle())
                .build();
    }
}
