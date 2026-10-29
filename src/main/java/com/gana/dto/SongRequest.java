package com.gana.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SongRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 200)
    private String artist;

    @Size(max = 200)
    private String album;

    @Size(max = 100)
    private String genre;

    @Min(0)
    @Max(86_400)
    private Integer duration;

    // A plain file name inside the music storage directory: no slashes or "..".
    @NotBlank
    @Pattern(
            regexp = "^(?!.*\\.\\.)[A-Za-z0-9 _().-]{1,200}\\.(mp3|MP3)$",
            message = "filePath must be a plain .mp3 file name"
    )
    private String filePath;

    @Size(max = 2048)
    @Pattern(regexp = "^(https?://.*)?$", message = "coverImageUrl must be an http(s) URL")
    private String coverImageUrl;
}
