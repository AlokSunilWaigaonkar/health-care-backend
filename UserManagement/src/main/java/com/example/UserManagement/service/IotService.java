package com.example.UserManagement.service;

import com.example.UserManagement.model.AdditionalModel.IotData;
import com.example.UserManagement.model.Users.Patient;
import com.example.UserManagement.repo.IotRepo;
import com.example.UserManagement.repo.PatientRepo;
import com.example.UserManagement.response.RecordResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IotService {
    private final IotRepo iotRepo;
    private final ThingSpeakClient thingSpeakClient;
    private final MailService mailService;
    private final PatientRepo patientRepo;
    @Value("${API_KEY}")
    public String Api_Key;

    @Transactional
    public void fetchAndSaveIotData(Long patientId ){
       Patient patient =  patientRepo.findById(patientId).orElseThrow(()->new RuntimeException("Patient Not Found"));
       long channelId = patient.getChannelId();
       long result = 7 ;
        Map<String, Object> dataMap = thingSpeakClient.fetchLatestIotData(Api_Key,channelId,result).block();


        if (dataMap == null || !dataMap.containsKey("feeds")){
            throw  new RuntimeException("Failed to fetch data from ThingSpeak");
        }
        List<?> rawFeeds = (List<?>) dataMap.get("feeds");

        List<IotData> iotDataList = new ArrayList<>();
        for (Object obj : rawFeeds) {
            if (obj instanceof Map<?, ?> feed) {
                IotData iotData = new IotData();
                iotData.setPatient(patient);
                iotData.setField1((String) feed.get("field1"));
                iotData.setField2((String) feed.get("field2"));
                iotData.setField3((String) feed.get("field3"));
                iotData.setField4((String) feed.get("field4"));
                iotData.setField5((String) feed.get("field5"));

                // Safely parse timestamp
                String timestamp = (String) feed.get("created_at");
                if (timestamp != null) {
                    iotData.setCreatedAt(LocalDateTime.parse(timestamp.replace("Z", "")));
                } else {
                    iotData.setCreatedAt(LocalDateTime.now());
                }

                iotDataList.add(iotData);
            }
        }
        List<IotData> saved = iotRepo.saveAll(iotDataList);



    }
    public List<RecordResponse> getRecentDataForPatient(Long patientId) {
        deleteLastSevenEntries(patientId);
        fetchAndSaveIotData(patientId);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<IotData> iotDataList =  iotRepo.findTop7ByPatientIdOrderByCreatedAtDesc(patientId);

        return iotDataList.stream()
                .map(data -> new RecordResponse(
                        data.getField1(),
                        data.getField2(),
                        data.getField3(),
                        data.getField4(),
                        data.getField5(),
                        data.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void deleteLastSevenEntries(Long patientId) {
        List<IotData> lastSevenEntries = iotRepo.findTop7ByPatientIdOrderByCreatedAtDesc(patientId);
        iotRepo.deleteAll(lastSevenEntries);
    }

    @Transactional
    public void refreshPatientIotData(Long patientId) {
        deleteLastSevenEntries(patientId);
        fetchAndSaveIotData(patientId);
    }

    @Transactional
    public void deletedOldEntries(){
        LocalDateTime cutOfDate = LocalDateTime.now().minusDays(7);
        iotRepo.deleteOldEntries(cutOfDate);
    }






}
