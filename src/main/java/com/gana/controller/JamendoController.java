package com.gana.controller;

import com.gana.dto.JamendoTrackDto;
import com.gana.exception.BadRequestException;
import com.gana.service.JamendoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jamendo")
@RequiredArgsConstructor
public class JamendoController {

    private static final int MAX_LIMIT = 50;
    private static final int MAX_KEYWORD_LENGTH = 100;

    private final JamendoService jamendoService;

    @GetMapping("/popular")
    public List<JamendoTrackDto> getPopularTracks(
            @RequestParam(defaultValue = "30") int limit) {
        return jamendoService.getPopularTracks(clampLimit(limit));
    }

    @GetMapping("/search")
    public List<JamendoTrackDto> searchTracks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "30") int limit) {
        if (keyword.isBlank() || keyword.length() > MAX_KEYWORD_LENGTH) {
            throw new BadRequestException("keyword must be 1-" + MAX_KEYWORD_LENGTH + " characters");
        }
        return jamendoService.searchTracks(keyword.trim(), clampLimit(limit));
    }

    private static int clampLimit(int limit) {
        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }
}