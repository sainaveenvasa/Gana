package com.gana.service.impl;

import com.gana.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SongStreamServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsFileInsideStorageDirectory() throws Exception {
        Path storage = Files.createDirectory(tempDir.resolve("songs"));
        Files.writeString(storage.resolve("track.mp3"), "audio");

        SongStreamServiceImpl service = new SongStreamServiceImpl(storage.toString());

        assertTrue(service.loadSongAsResource("track.mp3").exists());
    }

    @Test
    void rejectsPathTraversal() throws Exception {
        Path storage = Files.createDirectory(tempDir.resolve("songs"));
        Files.writeString(tempDir.resolve("secret.txt"), "secret");

        SongStreamServiceImpl service = new SongStreamServiceImpl(storage.toString());

        assertThrows(ResourceNotFoundException.class, () -> service.loadSongAsResource("../secret.txt"));
        assertThrows(ResourceNotFoundException.class,
                () -> service.loadSongAsResource(tempDir.resolve("secret.txt").toString()));
    }

    @Test
    void rejectsSymlinkEscapingStorageDirectory() throws Exception {
        Path storage = Files.createDirectory(tempDir.resolve("songs"));
        Path secret = Files.writeString(tempDir.resolve("secret.txt"), "secret");
        Files.createSymbolicLink(storage.resolve("link.mp3"), secret);

        SongStreamServiceImpl service = new SongStreamServiceImpl(storage.toString());

        assertThrows(ResourceNotFoundException.class, () -> service.loadSongAsResource("link.mp3"));
    }
}
