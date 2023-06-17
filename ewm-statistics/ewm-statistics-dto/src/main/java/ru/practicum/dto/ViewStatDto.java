package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Builder
@AllArgsConstructor
@Jacksonized
public class ViewStatDto {
    @NotBlank(message = "App cannot be empty")
    String app;

    @NotBlank(message = "URI cannot be empty")
    String uri;

    @NotNull(message = "Hits cannot be empty")
    Long hits;
}
