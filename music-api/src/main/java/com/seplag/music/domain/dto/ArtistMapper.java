package com.seplag.music.domain.dto;

import com.seplag.music.domain.model.Artist;
import org.springframework.stereotype.Component;

@Component
public class ArtistMapper {

    public ArtistDTO toDTO(Artist artist) {
        if (artist == null) {
            return null;
        }
        return ArtistDTO.builder()
                .id(artist.getId())
                .name(artist.getName())
                .type(artist.getType())
                .createdAt(artist.getCreatedAt())
                .build();
    }

    public Artist toEntity(ArtistCreateUpdateDTO dto) {
        if (dto == null) {
            return null;
        }
        return Artist.builder()
                .name(dto.getName())
                .type(dto.getType())
                .build();
    }

    public void updateEntity(ArtistCreateUpdateDTO dto, Artist artist) {
        if (dto == null) {
            return;
        }
        artist.setName(dto.getName());
        artist.setType(dto.getType());
    }
}