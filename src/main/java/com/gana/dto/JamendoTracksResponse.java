package com.gana.dto;

import lombok.Data;
import java.util.List;

@Data
public class JamendoTracksResponse {
    private List<JamendoTrackDto> results;
}