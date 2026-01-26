package com.seplag.music.domain.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumDTO {

    private Long id;
    private String title;
    private Integer releaseYear;
    private LocalDateTime createdAt;
    private Set<ArtistDTO> artists;
    private Set<AlbumCoverDTO> covers;
}