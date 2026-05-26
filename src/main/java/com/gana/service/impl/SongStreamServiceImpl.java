package com.gana.service.impl;

import com.gana.service.SongStreamService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SongStreamServiceImpl implements SongStreamService {

    @Value("${app.music.storage.path}")
    private String musicStoragePath;

    @Override
    public Resource loadSongAsResource(String fileName) {
        try {
            Path filePath = Paths.get(musicStoragePath).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + fileName, e);
        }
    }
}