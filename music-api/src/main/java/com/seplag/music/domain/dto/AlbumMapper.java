package com.seplag.music.domain.dto;

import com.seplag.music.domain.model.Album;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AlbumMapper {

    private final ArtistMapper artistMapper;

    public AlbumMapper(ArtistMapper artistMapper) {
        this.artistMapper = artistMapper;
    }

    public AlbumDTO toDTO(Album album) {
        if (album == null) {
            return null;
        }
        return AlbumDTO.builder()
                .id(album.getId())
                .title(album.getTitle())
                .releaseYear(album.getReleaseYear())
                .createdAt(album.getCreatedAt())
                .artists(album.getArtists() != null
                        ? album.getArtists().stream()
                        .map(artistMapper::toDTO)
                        .collect(Collectors.toSet())
                        : null)
                .build();
    }

    public Album toEntity(AlbumCreateUpdateDTO dto) {
        if (dto == null) {
            return null;
        }
        return Album.builder()
                .title(dto.getTitle())
                .releaseYear(dto.getReleaseYear())
                .build();
    }

    public void updateEntity(AlbumCreateUpdateDTO dto, Album album) {
        if (dto == null) {
            return;
        }
        album.setTitle(dto.getTitle());
        album.setReleaseYear(dto.getReleaseYear());
    }
}