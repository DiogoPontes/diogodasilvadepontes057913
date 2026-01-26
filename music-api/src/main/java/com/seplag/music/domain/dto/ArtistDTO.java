package com.seplag.music.domain.dto;

import com.seplag.music.domain.model.ArtistType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistDTO {

    private Long id;
    private String name;
    private ArtistType type;
    private LocalDateTime createdAt;
}