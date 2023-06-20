package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewUserRequest {
    @NotBlank(message = "name cannot be empty or null.")
    @Size(min = 2, max = 250, message = "name cannot be less than 2 or more than 250.")
    private String name;

    @Email(message = "email is not valid.")
    @NotBlank(message = "email cannot be empty or null.")
    @Size(min = 6, max = 254, message = "email cannot be less than 6 or more than 254.")
    private String email;
}
