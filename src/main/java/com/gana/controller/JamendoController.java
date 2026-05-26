package com.gana.controller;

import com.gana.dto.JamendoTrackDto;
import com.gana.service.JamendoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jamendo")
@RequiredArgsConstructor
public class JamendoController {

    private final JamendoService jamendoService;

    @GetMapping("/popular")
    public List<JamendoTrackDto> getPopularTracks(
            @RequestParam(defaultValue = "30") int limit) {
        return jamendoService.getPopularTracks(limit);
    }

    @GetMapping("/search")
    public List<JamendoTrackDto> searchTracks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "30") int limit) {
        return jamendoService.searchTracks(keyword, limit);
    }
}