package com.gana.service.impl;
import com.gana.dto.SongRequest;
import com.gana.entity.Song;
import com.gana.repository.SongRepository;
import com.gana.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;

    @Override
    public Song addSong(SongRequest request) {
        Song song = Song.builder()
                .title(request.getTitle())
                .artist(request.getArtist())
                .album(request.getAlbum())
                .genre(request.getGenre())
                .duration(request.getDuration())
                .filePath(request.getFilePath())
                .coverImageUrl(request.getCoverImageUrl())
                .active(true)
                .build();

        return songRepository.save(song);
    }

    @Override
    public List<Song> getAllSongs() {
        return songRepository.findByActiveTrue();
    }

    @Override
    public Song getSongById(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found with id: " + id));
    }

    @Override
    public List<Song> searchSongs(String keyword) {
        return songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(keyword, keyword);
    }
}