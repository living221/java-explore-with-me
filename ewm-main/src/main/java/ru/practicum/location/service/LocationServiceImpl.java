package ru.practicum.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.location.LocationMapper;
import ru.practicum.location.dao.LocationRepository;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.location.model.Location;

import static ru.practicum.location.LocationMapper.toLocation;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public LocationDto createLocation(LocationDto locationDto) {
        Location location = toLocation(locationDto);

        return LocationMapper.toLocationDto(locationRepository.save(location));
    }
}
