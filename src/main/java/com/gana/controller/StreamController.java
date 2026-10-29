package com.gana.controller;

import com.gana.entity.Song;
import com.gana.exception.ResourceNotFoundException;
import com.gana.service.SongService;
import com.gana.service.SongStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
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
        if (!Boolean.TRUE.equals(song.getActive())) {
            throw new ResourceNotFoundException("Song not found");
        }
        Resource resource = songStreamService.loadSongAsResource(song.getFilePath());

        // Built via ContentDisposition so the filename is properly quoted/encoded
        // instead of concatenated into the header.
        ContentDisposition disposition = ContentDisposition.inline()
                .filename("song-" + song.getId() + ".mp3")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}
