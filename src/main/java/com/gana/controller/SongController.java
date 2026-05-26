package com.gana.controller;

import com.gana.dto.SongRequest;
import com.gana.entity.Song;
import com.gana.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    public Song addSong(@Valid @RequestBody SongRequest request) {
        return songService.addSong(request);
    }

    @GetMapping
    public List<Song> getAllSongs() {
        return songService.getAllSongs();
    }

    @GetMapping("/{id}")
    public Song getSongById(@PathVariable Long id) {
        return songService.getSongById(id);
    }

    @GetMapping("/search")
    public List<Song> searchSongs(@RequestParam String keyword) {
        return songService.searchSongs(keyword);
    }
}