package com.gana.service.impl;

import com.gana.dto.JamendoTrackDto;
import com.gana.dto.JamendoTracksResponse;
import com.gana.service.JamendoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JamendoServiceImpl implements JamendoService {

    @Value("${jamendo.client-id}")
    private String clientId;

    @Value("${jamendo.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<JamendoTrackDto> getPopularTracks(int limit) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/tracks")
                .queryParam("client_id", clientId)
                .queryParam("format", "json")
                .queryParam("limit", limit)
                .queryParam("order", "popularity_total")
                .toUriString();

        JamendoTracksResponse response =
                restTemplate.getForObject(url, JamendoTracksResponse.class);

        return response != null && response.getResults() != null
                ? response.getResults()
                : Collections.emptyList();
    }

    @Override
    public List<JamendoTrackDto> searchTracks(String keyword, int limit) {
        String url = UriComponentsBuilder.fromUriString(baseUrl + "/tracks")
                .queryParam("client_id", clientId)
                .queryParam("format", "json")
                .queryParam("limit", limit)
                .queryParam("search", keyword)
                .toUriString();

        JamendoTracksResponse response =
                restTemplate.getForObject(url, JamendoTracksResponse.class);

        return response != null && response.getResults() != null
                ? response.getResults()
                : Collections.emptyList();
    }
}