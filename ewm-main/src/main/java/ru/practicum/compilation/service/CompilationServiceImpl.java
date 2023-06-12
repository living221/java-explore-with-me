package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dao.CompilationRepository;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.exceptions.ObjectNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.category.CategoryMapper.toCategoryDto;
import static ru.practicum.compilation.CompilationMapper.toCompilation;
import static ru.practicum.compilation.CompilationMapper.toCompilationDto;
import static ru.practicum.event.EventMapper.toEventShortDto;
import static ru.practicum.user.UserMapper.toUserShortDto;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);

        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);

        return compilations.stream()
                .map(c -> toCompilationDto(c, c.getEvents().stream()
                        .map(e -> toEventShortDto(e, toCategoryDto(e.getCategory()), toUserShortDto(e.getInitiator())))
                        .collect(Collectors.toList()))).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format("Compilation with id=%s was not found", compId));
        });

        return toCompilationDto(compilation, compilation.getEvents().stream()
                .map(e -> toEventShortDto(e,
                        toCategoryDto(e.getCategory()),
                        toUserShortDto(e.getInitiator())))
                .collect(Collectors.toList()));
    }

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Set<Event> events = Set.copyOf(eventRepository.findAllById(newCompilationDto.getEvents()));

        Compilation compilation = toCompilation(newCompilationDto, events);

        Compilation savedCompilation = compilationRepository.save(compilation);

        return toCompilationDto(savedCompilation, events.stream()
                .map(e -> toEventShortDto(e,
                        toCategoryDto(e.getCategory()),
                        toUserShortDto(e.getInitiator())))
                .collect(Collectors.toList()));
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationRepository.findById(compId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format("Compilation with id=%s was not found", compId));
        });

        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new ObjectNotFoundException(String.format("Compilation with id=%s was not found", compId));
        });

        Set<Event> events = Set.copyOf(eventRepository.findAllById(updateCompilationRequest.getEvents()));
        compilation.setEvents(events);

        if (!Objects.isNull(updateCompilationRequest.getTitle())) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        if (!Objects.isNull(updateCompilationRequest.getPinned())) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        return toCompilationDto(savedCompilation, events.stream()
                .map(e -> toEventShortDto(e,
                        toCategoryDto(e.getCategory()),
                        toUserShortDto(e.getInitiator())))
                .collect(Collectors.toList()));
    }
}
