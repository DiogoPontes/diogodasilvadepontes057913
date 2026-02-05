package com.seplag.music.domain.dto;

import com.seplag.music.domain.model.Album;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AlbumMapper {

    private final ArtistMapper artistMapper;
    private final AlbumCoverMapper albumCoverMapper; 

    public AlbumDTO toDTO(Album entity) {  
        if (entity == null) return null;  
  
        AlbumDTO dto = AlbumDTO.builder()  
                .id(entity.getId())  
                .title(entity.getTitle())  
                .releaseYear(entity.getReleaseYear())  
                .createdAt(entity.getCreatedAt())  
                .build();  
  
        if (entity.getArtists() != null) {  
            dto.setArtists(entity.getArtists().stream()  
                .map(artistMapper::toDTO)  
                .collect(Collectors.toSet()));  
        }  
  
        if (entity.getCovers() != null) {  
            dto.setCovers(entity.getCovers().stream()  
                .map(albumCoverMapper::toDTO)  
                .collect(Collectors.toSet()));  
        }  
  
        return dto;  
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