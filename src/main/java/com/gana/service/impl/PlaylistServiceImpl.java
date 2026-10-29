package com.gana.service.impl;

import com.gana.dto.AddSongToPlaylistRequest;
import com.gana.dto.PlaylistRequest;
import com.gana.entity.Playlist;
import com.gana.entity.PlaylistSong;
import com.gana.entity.Song;
import com.gana.entity.User;
import com.gana.exception.ResourceNotFoundException;
import com.gana.exception.UnauthorizedActionException;
import com.gana.repository.PlaylistRepository;
import com.gana.repository.PlaylistSongRepository;
import com.gana.repository.SongRepository;
import com.gana.repository.UserRepository;
import com.gana.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        User user = currentUser();

        Playlist playlist = Playlist.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .user(user)
                .build();

        return playlistRepository.save(playlist);
    }

    @Override
    public List<Playlist> getMyPlaylists() {
        return playlistRepository.findByUser(currentUser());
    }

    @Override
    public PlaylistSong addSongToPlaylist(AddSongToPlaylistRequest request) {
        Playlist playlist = findOwnedPlaylist(request.getPlaylistId());

        Song song = songRepository.findById(request.getSongId())
                .filter(Song::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found"));

        PlaylistSong playlistSong = PlaylistSong.builder()
                .playlist(playlist)
                .song(song)
                .build();

        return playlistSongRepository.save(playlistSong);
    }

    @Override
    public List<PlaylistSong> getSongsByPlaylist(Long playlistId) {
        return playlistSongRepository.findByPlaylist(findOwnedPlaylist(playlistId));
    }

    private Playlist findOwnedPlaylist(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        if (!playlist.getUser().getId().equals(currentUser().getId())) {
            throw new UnauthorizedActionException("You do not have access to this playlist");
        }
        return playlist;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedActionException("Authentication required");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedActionException("Authentication required"));
    }
}
