package ru.practicum.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterId(Long userId);

    Request findOneByEventIdAndRequesterId(Long eventId, Long userId);

    List<Request> findByEventInitiatorIdAndEventId(Long initiatorId, Long eventId);
}
