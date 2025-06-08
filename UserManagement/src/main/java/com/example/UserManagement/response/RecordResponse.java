package com.example.UserManagement.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordResponse {
    private String field1; // Room Temp
    private String field2; // Humidity
    private String field3; // Body Temp
    private String field4; // SpO2
    private String field5; // ECG
    private LocalDateTime createdAt;
}
