package ru.practicum.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.category.Create;
import ru.practicum.category.Update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCategoryDto {
    @NotBlank(groups = {Create.class, Update.class}, message = "name cannot be empty or null.")
    @Size(groups = {Create.class, Update.class}, min = 1, max = 50, message = "name cannot < 0 and > 50 symbols.")
    String name;
}
