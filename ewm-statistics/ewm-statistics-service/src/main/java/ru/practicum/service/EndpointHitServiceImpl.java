package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ViewStatMapper;
import ru.practicum.dao.EndpointHitRepository;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatDto;
import ru.practicum.exceptions.TimestampException;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStat;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.EndpointHitMapper.toEndPointHit;
import static ru.practicum.EndpointHitMapper.toEndpointHitDto;

@Service
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EndpointHitRepository endpointHitRepository;

    @Override
    @Transactional
    public EndPointHitDto create(EndPointHitDto endPointHitDto) {
        EndpointHit endpointHit = toEndPointHit(endPointHitDto);

        return toEndpointHitDto(endpointHitRepository.save(endpointHit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        LocalDateTime startDate = LocalDateTime.parse(URLDecoder.decode(start, StandardCharsets.UTF_8), FORMATTER);
        LocalDateTime endDate = LocalDateTime.parse(URLDecoder.decode(end, StandardCharsets.UTF_8), FORMATTER);

        checkDate(startDate, endDate);



        List<ViewStat> result;
        if (unique) {
            if (uris.isEmpty()) {
                result = endpointHitRepository.getViewStatsUnique(startDate, endDate);
            } else {
                result = endpointHitRepository.getViewStatsByUrisAndUnique(startDate, endDate, uris);
            }
        } else {
            if (uris.isEmpty()) {
                result = endpointHitRepository.getViewStats(startDate, endDate);
            } else {
                result = endpointHitRepository.getViewStatsByUris(startDate, endDate, uris);
            }
        }

        return result.stream()
                .map(ViewStatMapper::toViewStatDto)
                .collect(Collectors.toList());
    }

    private void checkDate(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new TimestampException("Start date or end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new TimestampException("End time cannot be before start time.");
        }
    }
}
