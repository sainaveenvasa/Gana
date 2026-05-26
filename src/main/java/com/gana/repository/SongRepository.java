package com.gana.repository;

import com.gana.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByActiveTrue();

    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(String title, String artist);
}