package com.gana.repository;

import com.gana.entity.Playlist;
import com.gana.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUser(User user);
}