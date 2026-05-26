package com.gana.service;

import com.gana.dto.JamendoTrackDto;
import java.util.List;

public interface JamendoService {
    List<JamendoTrackDto> getPopularTracks(int limit);
    List<JamendoTrackDto> searchTracks(String keyword, int limit);
}