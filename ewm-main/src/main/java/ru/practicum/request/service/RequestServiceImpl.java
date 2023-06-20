package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ObjectNotFoundException;
import ru.practicum.request.RequestMapper;
import ru.practicum.request.dao.RequestRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.NewRequestStatus;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.request.RequestMapper.toParticipationRequestDto;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsById(Long userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%s was not found", userId)));

        List<Request> requests = requestRepository.findByRequesterId(userId);

        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%s was not found", userId)));

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Event with id=%s was not found", eventId)));

        Request requestExist = requestRepository.findOneByEventIdAndRequesterId(eventId, userId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cant make request for unpublished request");
        }

        if (!Objects.isNull(requestExist)) {
            throw new ConflictException(String.format("Event with id=%s and requester with id=%s already exist",
                    eventId, userId));
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(String.format("Event initiator with id=%s cant make request for their event",
                    userId));
        }

        if (event.getParticipantLimit() != 0 &&
                event.getParticipants().size() >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit for request is exceeded");
        }

        Request request = Request.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        return toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%s was not found", userId)));

        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Request with id=%s was not found", requestId)));

        if (!user.getId().equals(request.getRequester().getId())) {
            throw new ConflictException(String.format("User with id=%s can't cancel request with id=%s",
                    userId, requestId));
        }

        request.setStatus(RequestStatus.CANCELED);

        return toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        List<Request> requests = requestRepository.findByEventInitiatorIdAndEventId(userId, eventId);

        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequest(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Event with id=%s was not found", eventId)));

        if (event.getParticipants().size() >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("You can't participate in an unpublished event");
        }

        List<Request> requests = requestRepository.findAllById(eventRequestStatusUpdateRequest.getRequestIds());

        requests.forEach(r -> {
            if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
                return;
            }
            if (NewRequestStatus.REJECTED == eventRequestStatusUpdateRequest.getStatus()) {
                r.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(toParticipationRequestDto(r));
            }
            if (NewRequestStatus.CONFIRMED == eventRequestStatusUpdateRequest.getStatus()) {
                r.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(toParticipationRequestDto(r));
            }
        });

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }
}
