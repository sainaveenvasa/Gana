package com.gana.repository;

import com.gana.entity.Playlist;
import com.gana.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    List<PlaylistSong> findByPlaylist(Playlist playlist);
}