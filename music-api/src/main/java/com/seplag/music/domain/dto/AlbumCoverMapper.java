package com.seplag.music.domain.dto;

import com.seplag.music.domain.model.AlbumCover;
import org.springframework.stereotype.Component;

@Component
public class AlbumCoverMapper {

    public AlbumCoverDTO toDTO(AlbumCover entity) {
        if (entity == null) return null;

        return AlbumCoverDTO.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .objectName(entity.getObjectName())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .isPrimary(entity.getIsPrimary())
                // A URL será preenchida pelo Service chamando o MinIO
                .build();
    }
}