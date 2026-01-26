package com.seplag.music.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionalDTO {

    private Integer id;
    private String nome;
    private Boolean ativo;
    private LocalDateTime updatedAt;
}