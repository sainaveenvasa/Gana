package com.gana.dto;

import lombok.Data;

@Data
public class JamendoTrackDto {
    private String id;
    private String name;
    private String duration;
    private String artist_name;
    private String album_name;
    private String image;
    private String audio;
    private String audiodownload;
    private Boolean audiodownload_allowed;
}