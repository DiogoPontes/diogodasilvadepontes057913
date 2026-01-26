package com.seplag.music.domain.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumCoverPresignedDTO {

    private Long coverId;
    private String presignedUrl;
    private String expiresIn; // "30 minutes"
}