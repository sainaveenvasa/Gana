package com.gana.controller;

import com.gana.dto.AddSongToPlaylistRequest;
import com.gana.dto.PlaylistRequest;
import com.gana.entity.Playlist;
import com.gana.entity.PlaylistSong;
import com.gana.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    public Playlist createPlaylist(@Valid @RequestBody PlaylistRequest request) {
        return playlistService.createPlaylist(request);
    }

    @GetMapping("/user/{userId}")
    public List<Playlist> getPlaylistsByUser(@PathVariable Long userId) {
        return playlistService.getPlaylistsByUser(userId);
    }

    @PostMapping("/add-song")
    public PlaylistSong addSongToPlaylist(@Valid @RequestBody AddSongToPlaylistRequest request) {
        return playlistService.addSongToPlaylist(request);
    }

    @GetMapping("/{playlistId}/songs")
    public List<PlaylistSong> getSongsByPlaylist(@PathVariable Long playlistId) {
        return playlistService.getSongsByPlaylist(playlistId);
    }
}