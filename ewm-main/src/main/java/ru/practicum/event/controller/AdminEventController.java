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
    public List<EventFullDto> getEventsAdmin(@RequestParam(required = false)
                                             List<Long> users,
                                             @RequestParam(required = false)
                                             List<EventState> states,
                                             @RequestParam(required = false)
                                             List<Long> categories,
                                             @RequestParam(value = "rangeStart", required = false)
                                             String rangeStartString,
                                             @RequestParam(value = "rangeEnd", required = false)
                                             String rangeEndString,
                                             @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                             @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        LocalDateTime rangeStart = null;
        if (rangeEndString != null) {
            String decodedRangeStart = URLDecoder.decode(rangeStartString, StandardCharsets.UTF_8);
            rangeStart = LocalDateTime.parse(decodedRangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        LocalDateTime rangeEnd = null;
        if (rangeEndString != null) {
            String decodedRangeEnd = URLDecoder.decode(rangeEndString, StandardCharsets.UTF_8);
            rangeEnd = LocalDateTime.parse(decodedRangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        return eventService.getEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(
            @PathVariable("eventId") Long eventId,
            @RequestBody UpdateEventRequest updateEventRequest
    ) {
        return eventService.updateEventAdmin(eventId, updateEventRequest);
    }
}
