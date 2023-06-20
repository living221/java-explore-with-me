package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventRequest;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;

import javax.validation.constraints.Min;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsAdmin(@RequestParam(required = false) List<Long> users,
                                             @RequestParam(required = false) List<EventState> states,
                                             @RequestParam(required = false) List<Long> categories,
                                             @RequestParam(value = "rangeStart", required = false)
                                             String rangeStartString,
                                             @RequestParam(value = "rangeEnd", required = false) String rangeEndString,
                                             @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                             @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        LocalDateTime rangeStart = null;
        LocalDateTime rangeEnd = null;

        if (rangeStartString != null) {
            rangeStart = decodeDate(rangeStartString);
        }

        if (rangeEndString != null) {
            rangeEnd = decodeDate(rangeEndString);
        }

        return eventService.getEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    private LocalDateTime decodeDate(String dateString) {
        LocalDateTime rangeStart;
        String decodedRangeStart = URLDecoder.decode(dateString, StandardCharsets.UTF_8);
        rangeStart = LocalDateTime.parse(decodedRangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return rangeStart;
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(@PathVariable("eventId") Long eventId,
                                         @RequestBody UpdateEventRequest updateEventRequest) {
        return eventService.updateEventAdmin(eventId, updateEventRequest);
    }
}
