package com.gana.service;
import com.gana.dto.SongRequest;
import com.gana.entity.Song;

import java.util.List;

public interface SongService {
    Song addSong(SongRequest request);
    List<Song> getAllSongs();
    Song getSongById(Long id);
    List<Song> searchSongs(String keyword);
}
