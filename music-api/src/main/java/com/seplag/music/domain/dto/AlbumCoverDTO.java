package com.seplag.music.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumCoverDTO {

    private Long id;
    private String objectName;
    private LocalDateTime createdAt;
}