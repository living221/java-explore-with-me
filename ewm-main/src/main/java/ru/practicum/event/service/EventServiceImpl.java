package ru.practicum.event.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.dto.ViewStatDto;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.model.StateAction;
import ru.practicum.ewmstatisticsclient.StatsClient;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.ObjectNotFoundException;
import ru.practicum.location.dao.LocationRepository;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.location.model.Location;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.user.model.User;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static ru.practicum.category.CategoryMapper.toCategoryDto;
import static ru.practicum.event.EventMapper.*;
import static ru.practicum.location.LocationMapper.toLocation;
import static ru.practicum.location.LocationMapper.toLocationDto;
import static ru.practicum.user.UserMapper.toUserShortDto;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {

        validateEventDate(newEventDto.getEventDate());

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%s was not found", userId)));

        Location location = getLocationOrAddNew(newEventDto.getLocation());

        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format("Category with id=%s was not found", newEventDto.getCategory()));
        });

        Event event = toEvent(newEventDto, category, location, user);

        Event savedEvent = eventRepository.save(event);
        return toEventFullDto(savedEvent,
                toCategoryDto(savedEvent.getCategory()),
                toUserShortDto(savedEvent.getInitiator()),
                toLocationDto(savedEvent.getLocation()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%s was not found", userId)));

        Pageable pageable = PageRequest.of(from, size);

        List<Event> events = eventRepository.findByInitiatorIdOrderByEventDateDesc(userId, pageable);

        Map<Long, Integer> hits = getStatsFromEvents(events);

        return events.stream()
                .map(e -> toEventShortDto(e, toCategoryDto(e.getCategory()), toUserShortDto(e.getInitiator())))
                .peek(e -> e.setViews(hits.getOrDefault(e.getId(), 0)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEventByEventId(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%s was not found", userId)));

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Event with id=%s was not found", eventId)));

        Map<Long, Integer> hits = getStatsFromEvents(List.of(event));

        EventFullDto eventFullDto = toEventFullDto(event,
                toCategoryDto(event.getCategory()),
                toUserShortDto(event.getInitiator()),
                toLocationDto(event.getLocation()));

        eventFullDto.setViews(hits.getOrDefault(eventId, 0));

        return eventFullDto;
    }

    @Override
    public EventFullDto updateEventByEventId(Long userId, Long eventId, UpdateEventRequest updateEventRequest) {
        userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("User with id=%s was not found", userId)));

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Event with id=%s was not found", eventId)));

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException(
                    String.format("User with id=%s is not initiator of event with id=%s", userId, eventId));
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        updateEventParams(updateEventRequest, event);
        if (!Objects.isNull(updateEventRequest.getStateAction())) {
            StateAction state = updateEventRequest.getStateAction();
            switch (state) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                default:
                    throw new ConflictException("Only pending or canceled events can be changed");
            }
        }

        Event updatedEvent = eventRepository.save(event);

        Map<Long, Integer> hits = getStatsFromEvents(List.of(event));

        EventFullDto eventFullDto = toEventFullDto(updatedEvent,
                toCategoryDto(updatedEvent.getCategory()),
                toUserShortDto(updatedEvent.getInitiator()),
                toLocationDto(updatedEvent.getLocation()));

        eventFullDto.setViews(hits.getOrDefault(eventId, 0));

        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         EventSortType sort,
                                         Integer from,
                                         Integer size,
                                         HttpServletRequest request) {
        createHit(request);

        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart)) {
                throw new BadRequestException(String.format("Start date=%s cannot be before end date=%s",
                        rangeStart, rangeEnd));
            }
        }

        BooleanBuilder builder = new BooleanBuilder();

        if (!Objects.isNull(text)) {
            builder.and((QEvent.event.annotation.containsIgnoreCase(text)))
                    .or(QEvent.event.description.containsIgnoreCase(text));
        }

        if (!Objects.isNull(paid)) {
            builder.and((QEvent.event.paid.isTrue()));
        }

        if (!Objects.isNull(categories)) {
            builder.and((QEvent.event.category.id.in(categories)));
        }

        if (Objects.isNull(rangeStart) && Objects.isNull(rangeEnd)) {
            builder.and((QEvent.event.eventDate.after(LocalDateTime.now())));
        } else {
            builder.and(QEvent.event.eventDate.after(rangeStart))
                    .and(QEvent.event.eventDate.before(rangeEnd));
        }

        if (onlyAvailable) {
            builder.and(QEvent.event.participantLimit.goe(0));
        }

        Sort eventSort = Sort.by(Sort.Direction.ASC, "eventDate");
        if (Objects.equals(sort, EventSortType.VIEWS)) {
            eventSort = Sort.by(Sort.Direction.ASC, "views");
        }

        Pageable pageable = PageRequest.of(from, size, eventSort);

        Iterable<Event> events = eventRepository.findAll(builder, pageable);
        List<Event> result = StreamSupport.stream(events.spliterator(), false).collect(Collectors.toList());

        Map<Long, Integer> hits = getStatsFromEvents(result);

        return result.stream()
                .map(e -> toEventShortDto(e, toCategoryDto(e.getCategory()), toUserShortDto(e.getInitiator())))
                .peek(e -> e.setViews(hits.getOrDefault(e.getId(), 0)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEventById(Long eventId, HttpServletRequest request) {
        createHit(request);

        Event event = eventRepository.findByIdAndPublished(eventId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Event with id=%s was not found", eventId)));

        EventFullDto eventFullDto = toEventFullDto(event,
                toCategoryDto(event.getCategory()),
                toUserShortDto(event.getInitiator()),
                toLocationDto(event.getLocation()));

        Map<Long, Integer> hits = getStatsFromEvents(List.of(event));
        eventFullDto.setViews(hits.get(eventId));
        return eventFullDto;
    }

    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        BooleanBuilder builder = new BooleanBuilder();

        if (!Objects.isNull(users)) {
            builder.and(QEvent.event.initiator.id.in(users));
        }
        if (!Objects.isNull(states)) {
            builder.and(QEvent.event.state.in(states));
        }
        if (!Objects.isNull(categories)) {
            builder.and(QEvent.event.category.id.in(categories));
        }
        if (!Objects.isNull(rangeStart)) {
            builder.and(QEvent.event.eventDate.after(rangeStart));
        }
        if (!Objects.isNull(rangeEnd)) {
            builder.and(QEvent.event.eventDate.before(rangeEnd));
        }

        Pageable pageable = PageRequest.of(from, size);
        Iterable<Event> events = eventRepository.findAll(builder, pageable);
        List<Event> result = StreamSupport.stream(events.spliterator(), false).collect(Collectors.toList());

        return result.stream()
                .map(e -> toEventFullDto(e,
                        toCategoryDto(e.getCategory()),
                        toUserShortDto(e.getInitiator()),
                        toLocationDto(e.getLocation())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Event with id=%s was not found", eventId)));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        updateEventParams(updateEventRequest, event);

        if (!Objects.isNull(updateEventRequest.getStateAction())) {
            StateAction state = updateEventRequest.getStateAction();
            switch (state) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot publish the event because it's not in the right state: PUBLISHED");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new ConflictException("Only pending or canceled events can be changed");
            }
        }

        Event updatedEvent = eventRepository.save(event);

        return toEventFullDto(updatedEvent,
                toCategoryDto(updatedEvent.getCategory()),
                toUserShortDto(updatedEvent.getInitiator()),
                toLocationDto(updatedEvent.getLocation()));
    }

    private void createHit(HttpServletRequest request) {
        String app = "ewm-main-service";
        statsClient.save(app, request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private Map<Long, Integer> getStatsFromEvents(List<Event> events) {
        Map<Long, Integer> hits = new HashMap<>();

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        List<String> uris = eventIds.stream()
                .map(i -> "/events/" + i)
                .collect(Collectors.toList());

        String start = LocalDateTime.now().minusYears(50).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<ViewStatDto> viewStatDtos = statsClient.get(start, end, uris, true);

        for (ViewStatDto viewStatDto : viewStatDtos) {
            String uri = viewStatDto.getUri();
            hits.put(Long.parseLong(uri.substring(8)), Math.toIntExact(viewStatDto.getHits()));
        }
        return hits;
    }

    private void updateEventParams(UpdateEventRequest updateEventRequest, Event event) {
        if (!Objects.isNull(updateEventRequest.getAnnotation())) {
            if (updateEventRequest.getAnnotation().length() < 20 ||
                    updateEventRequest.getAnnotation().length() > 2000) {
                throw new BadRequestException("Annotation length cannot be less than 20 or more than 2000");
            }
            event.setAnnotation(updateEventRequest.getAnnotation());
        }

        if (!Objects.isNull(updateEventRequest.getCategory())) {
            Category category = categoryRepository.findById(updateEventRequest.getCategory()).orElseThrow(() -> {
                throw new ObjectNotFoundException(String.format("Category with id=%s was not found",
                        updateEventRequest.getCategory()));
            });
            event.setCategory(category);
        }

        if (!Objects.isNull(updateEventRequest.getDescription())) {
            if (updateEventRequest.getDescription().length() < 20 ||
                    updateEventRequest.getDescription().length() > 7000) {
                throw new BadRequestException("Description length cannot be less than 20 or more than 7000");
            }
            event.setDescription(updateEventRequest.getDescription());
        }

        if (!Objects.isNull(updateEventRequest.getEventDate())) {
            validateEventDate(updateEventRequest.getEventDate());
            event.setEventDate(updateEventRequest.getEventDate());
        }

        if (!Objects.isNull(updateEventRequest.getLocation())) {
            Location foundOrAddedLocation = getLocationOrAddNew(updateEventRequest.getLocation());
            event.setLocation(foundOrAddedLocation);
        }

        if (!Objects.isNull(updateEventRequest.getPaid())) {
            event.setPaid(updateEventRequest.getPaid());
        }

        if (!Objects.isNull(updateEventRequest.getParticipantLimit())) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }

        if (!Objects.isNull(updateEventRequest.getRequestModeration())) {
            event.setRequestModeration(updateEventRequest.getRequestModeration());
        }

        if (!Objects.isNull(updateEventRequest.getTitle())) {
            if (updateEventRequest.getTitle().length() < 3 ||
                    updateEventRequest.getTitle().length() > 120) {
                throw new BadRequestException("Title length cannot be less than 3 or more than 120");
            }
            event.setTitle(updateEventRequest.getTitle());
        }
    }

    private Location getLocationOrAddNew(LocationDto locationDto) {
        Location location = locationRepository.findByLatAndLon(
                locationDto.getLat(),
                locationDto.getLon());

        if (Objects.isNull(location)) {
            location = locationRepository.save(toLocation(locationDto));
        }
        return location;
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (LocalDateTime.now().plusHours(2).isAfter(eventDate)) {
            throw new BadRequestException(String.format("Event date=%s cannot be before now + 2 hours date.", eventDate));
        }
    }
}
