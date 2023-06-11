package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Value
@Builder
@AllArgsConstructor
@Jacksonized
public class EndPointHitDto {
    Long id;

    @NotBlank(message = "App cannot be empty")
    String app;

    @NotBlank(message = "URI cannot be empty")
    String uri;

    @NotBlank(message = "Ip cannot be empty")
    String ip;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Timestamp cannot be empty")
    LocalDateTime timestamp;
}
