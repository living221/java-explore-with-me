package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStat;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    @Query(value = "SELECT new ru.practicum.model.ViewStat(e.uri, e.app, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND e.uri IN :uris " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStat> getViewStatsUnique(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT NEW ru.practicum.model.ViewStat(e.uri, e.app, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.uri IN :uris " +
            "AND e.timestamp BETWEEN :start AND :end " +
            "GROUP BY e.app, e.uri, e.ip " +
            "ORDER BY COUNT(DISTINCT e.ip) DESC")
    List<ViewStat> getViewStatsByUrisAndUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.model.ViewStat(e.uri, e.app, COUNT(e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStat> getViewStats(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.model.ViewStat(e.uri, e.app, COUNT(e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND e.uri IN :uris " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStat> getViewStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}
