package com.learning.journalApp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @NotEmpty(message = "User name cannot be empty")
    @Schema(description = "User name of the user", example = "john_doe")
    private String userName;
    @NotEmpty(message = "Password cannot be empty")
    private String password;
    private String email;
    private boolean sentimentAnalysis;
}