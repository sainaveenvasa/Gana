package com.gana.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "songs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    private String album;

    private String genre;

    private Integer duration;

    // Internal storage detail; clients stream via /api/stream/{id}.
    @JsonIgnore
    @Column(nullable = false)
    private String filePath;

    private String coverImageUrl;

    @Column(nullable = false)
    private Boolean active = true;
}