package com.example.UserManagement.controller;


import com.example.UserManagement.model.AdditionalModel.IotData;
import com.example.UserManagement.response.RecordResponse;
import com.example.UserManagement.service.IotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/iot-data")
@RequiredArgsConstructor
public class IotController {
    private final IotService iotService;

    @GetMapping("/patient/{patientId}/recent")
    public ResponseEntity<?> getRecentDataForPatient(@PathVariable Long patientId){
        List<RecordResponse> data = iotService.getRecentDataForPatient(patientId);
        return ResponseEntity.ok(data);
    }



}
