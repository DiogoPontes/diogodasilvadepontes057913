package com.seplag.music.domain.dto;

import com.seplag.music.domain.model.Regional;
import org.springframework.stereotype.Component;

@Component
public class RegionalMapper {

    public RegionalDTO toDTO(Regional regional) {
        if (regional == null) {
            return null;
        }
        return RegionalDTO.builder()
                .id(regional.getId())
                .nome(regional.getNome())
                .ativo(regional.getAtivo())
                .updatedAt(regional.getUpdatedAt())
                .build();
    }

    public Regional toEntity(RegionalDTO dto) {
        if (dto == null) {
            return null;
        }
        return Regional.builder()
                .id(dto.getId())
                .nome(dto.getNome())
                .ativo(dto.getAtivo())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}