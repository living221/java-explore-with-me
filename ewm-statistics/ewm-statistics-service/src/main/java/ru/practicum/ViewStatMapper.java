package ru.practicum;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.ViewStatDto;
import ru.practicum.model.ViewStat;

@UtilityClass
public class ViewStatMapper {
    public static ViewStatDto toViewStatDto(ViewStat viewStat) {
        return ViewStatDto.builder()
                .app(viewStat.getApp())
                .uri(viewStat.getUri())
                .hits(viewStat.getHits())
                .build();
    }
}
