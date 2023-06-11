package ru.practicum.ewmstatisticsclient;

import ru.practicum.dto.ViewStatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsClient {
    void save(String app, String uri, String ip, LocalDateTime localDateTime);

    List<ViewStatDto> get(String start, String end, List<String> uris, Boolean unique);
}