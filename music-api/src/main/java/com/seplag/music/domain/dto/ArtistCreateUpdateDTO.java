package com.seplag.music.domain.dto;

import com.seplag.music.domain.model.ArtistType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistCreateUpdateDTO {

    @NotBlank(message = "Nome do artista é obrigatório")
    private String name;

    @NotNull(message = "Tipo de artista é obrigatório (SOLO ou BANDA)")
    private ArtistType type;
}