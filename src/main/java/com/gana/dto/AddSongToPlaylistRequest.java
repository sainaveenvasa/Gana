package com.gana.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddSongToPlaylistRequest {

    @NotNull
    private Long playlistId;

    @NotNull
    private Long songId;
}