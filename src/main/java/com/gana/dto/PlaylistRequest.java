package com.gana.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * The owning user is taken from the authenticated JWT, never from the request
 * body, so one user cannot create playlists on another user's account.
 */
@Data
public class PlaylistRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;
}
