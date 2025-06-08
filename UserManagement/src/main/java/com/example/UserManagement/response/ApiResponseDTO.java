package com.example.UserManagement.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO<T> {
    private String message;
    private boolean success;
    private T data;

    // Static factory method for success
    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>(message, true, data);
    }

    // Static factory method for failure (with optional null data)
    public static <T> ApiResponseDTO<T> failure(String message) {
        return new ApiResponseDTO<>(message, false, null);
    }
}
