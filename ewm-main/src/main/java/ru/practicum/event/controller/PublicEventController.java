package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.EventSortType;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(value = "text", required = false)
                                         String text,
                                         @RequestParam(value = "categories", required = false)
                                         List<Long> categories,
                                         @RequestParam(value = "paid", required = false)
                                         Boolean paid,
                                         @RequestParam(value = "rangeStart", required = false)
                                         String rangeStartString,
                                         @RequestParam(value = "rangeEnd", required = false)
                                         String rangeEndString,
                                         @RequestParam(value = "onlyAvailable", defaultValue = "false")
                                         Boolean onlyAvailable,
                                         @RequestParam(value = "sort", required = false) EventSortType sort,
                                         @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                         @RequestParam(value = "size", defaultValue = "10") @Min(1) Integer size,
                                         HttpServletRequest request) {

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

        return eventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getPublicEventById(@PathVariable("eventId") Long eventId, HttpServletRequest request) {
        return eventService.getPublicEventById(eventId, request);
    }
}
