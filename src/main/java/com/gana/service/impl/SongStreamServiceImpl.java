package com.gana.service.impl;

import com.gana.exception.ResourceNotFoundException;
import com.gana.service.SongStreamService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SongStreamServiceImpl implements SongStreamService {

    private final Path storageRoot;

    public SongStreamServiceImpl(@Value("${app.music.storage.path}") String musicStoragePath) {
        this.storageRoot = Paths.get(musicStoragePath).toAbsolutePath().normalize();
    }

    @Override
    public Resource loadSongAsResource(String fileName) {
        try {
            Path filePath = storageRoot.resolve(fileName).normalize();

            // Block path traversal (e.g. "../../etc/passwd") and symlinks that
            // escape the music directory.
            if (!filePath.startsWith(storageRoot)
                    || !Files.isRegularFile(filePath)
                    || !filePath.toRealPath().startsWith(storageRoot.toRealPath())) {
                throw new ResourceNotFoundException("Song file not found");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.isReadable()) {
                throw new ResourceNotFoundException("Song file not found");
            }
            return resource;
        } catch (InvalidPathException | IOException e) {
            throw new ResourceNotFoundException("Song file not found");
        }
    }
}
