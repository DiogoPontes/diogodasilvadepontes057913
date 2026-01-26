package com.seplag.music.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumCreateUpdateDTO {

    @NotBlank(message = "Título do álbum é obrigatório")
    private String title;

    private Integer releaseYear;
}