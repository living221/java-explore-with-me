package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventFullDto getUserEventByEventId(Long userId, Long eventId);

    EventFullDto updateEventByEventId(Long userId, Long eventId, UpdateEventRequest updateEventRequest);

    List<EventShortDto> getEvents(String text,
                                  List<Long> categories,
                                  Boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Boolean onlyAvailable,
                                  EventSortType sort,
                                  Integer from,
                                  Integer size,
                                  HttpServletRequest request);

    EventFullDto getPublicEventById(Long eventId, HttpServletRequest request);

    List<EventFullDto> getEventsAdmin(List<Long> users,
                                      List<EventState> states,
                                      List<Long> categories,
                                      LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd,
                                      Integer from,
                                      Integer size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventRequest updateEventRequest);

    void addRating(Long userId, Long eventId, Boolean isPositive);

    void deleteRating(Long userId, Long eventId);
}
