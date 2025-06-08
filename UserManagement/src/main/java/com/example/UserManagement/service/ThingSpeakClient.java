package com.example.UserManagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class ThingSpeakClient {
    private final WebClient webClient;

    public ThingSpeakClient(WebClient.Builder builder){
        this.webClient = builder.baseUrl("https://api.thingspeak.com").build();
    }

    public Mono<Map<String , Object>> fetchLatestIotData(String apiKey , long channelId , long noOfResults ){
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("channels/"+channelId +"/feeds.json")
                        .queryParam("api_key",apiKey)
                        .queryParam("results",noOfResults)
                        .build()
                ).retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
