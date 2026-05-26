package com.gana.controller;

import com.gana.entity.Song;
import com.gana.service.SongService;
import com.gana.service.SongStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamController {

    private final SongService songService;
    private final SongStreamService songStreamService;

    @GetMapping("/{songId}")
    public ResponseEntity<Resource> streamSong(@PathVariable Long songId) {
        Song song = songService.getSongById(songId);
        Resource resource = songStreamService.loadSongAsResource(song.getFilePath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + song.getFilePath() + "\"")
                .body(resource);
    }
}