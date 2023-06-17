package ru.practicum.service;

import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitService {
    EndPointHitDto create(EndPointHitDto endPointHitDto);

    List<ViewStatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
