package com.gana.service;

import com.gana.dto.AddSongToPlaylistRequest;
import com.gana.dto.PlaylistRequest;
import com.gana.entity.Playlist;
import com.gana.entity.PlaylistSong;

import java.util.List;

public interface PlaylistService {
    Playlist createPlaylist(PlaylistRequest request);
    List<Playlist> getPlaylistsByUser(Long userId);
    PlaylistSong addSongToPlaylist(AddSongToPlaylistRequest request);
    List<PlaylistSong> getSongsByPlaylist(Long playlistId);
}