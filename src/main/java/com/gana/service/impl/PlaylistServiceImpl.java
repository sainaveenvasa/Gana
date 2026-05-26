package com.gana.service.impl;

import com.gana.dto.AddSongToPlaylistRequest;
import com.gana.dto.PlaylistRequest;
import com.gana.entity.Playlist;
import com.gana.entity.PlaylistSong;
import com.gana.entity.Song;
import com.gana.entity.User;
import com.gana.repository.PlaylistRepository;
import com.gana.repository.PlaylistSongRepository;
import com.gana.repository.SongRepository;
import com.gana.repository.UserRepository;
import com.gana.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    @Override
    public Playlist createPlaylist(PlaylistRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .build();

        return playlistRepository.save(playlist);
    }

    @Override
    public List<Playlist> getPlaylistsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return playlistRepository.findByUser(user);
    }

    @Override
    public PlaylistSong addSongToPlaylist(AddSongToPlaylistRequest request) {
        Playlist playlist = playlistRepository.findById(request.getPlaylistId())
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        Song song = songRepository.findById(request.getSongId())
                .orElseThrow(() -> new RuntimeException("Song not found"));

        PlaylistSong playlistSong = PlaylistSong.builder()
                .playlist(playlist)
                .song(song)
                .build();

        return playlistSongRepository.save(playlistSong);
    }

    @Override
    public List<PlaylistSong> getSongsByPlaylist(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        return playlistSongRepository.findByPlaylist(playlist);
    }
}