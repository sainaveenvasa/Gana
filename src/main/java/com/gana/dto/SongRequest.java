package com.gana.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SongRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String artist;

    private String album;
    private String genre;
    private Integer duration;
    private String filePath;
    private String coverImageUrl;
}
