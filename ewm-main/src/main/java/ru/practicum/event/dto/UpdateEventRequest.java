package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.StateAction;
import ru.practicum.location.dto.LocationDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventRequest {
    @Size(min = 20, max = 2000, message = "annotation cannot be less than 20 or more than 2000.")
    private String annotation;

    @Positive(message = "category id cannot be negative or zero.")
    private Long category;

    @Size(min = 20, max = 7000, message = "description cannot be less than 20 or more than 7000.")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "participant limit cannot be negative.")
    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction;

    @Size(min = 3, max = 120, message = "title cannot be less than 3 or more than 120.")
    private String title;
}
