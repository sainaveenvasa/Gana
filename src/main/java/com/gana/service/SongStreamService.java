package com.gana.service;

import org.springframework.core.io.Resource;

public interface SongStreamService {
    Resource loadSongAsResource(String fileName);
}