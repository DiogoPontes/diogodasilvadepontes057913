package com.seplag.music.domain.dto;

import lombok.Data;  
import lombok.NoArgsConstructor;  
import lombok.AllArgsConstructor;  
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumCreateUpdateDTO {

    @NotBlank(message = "Título do álbum é obrigatório")
    private String title;

    @NotNull
    private Integer releaseYear;
}