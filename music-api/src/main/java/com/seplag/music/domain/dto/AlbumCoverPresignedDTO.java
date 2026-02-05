package com.seplag.music.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlbumCoverPresignedDTO {

    private Long id;
    private Long albumId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private Boolean isPrimary;
    private String presignedUrl;
    private LocalDateTime createdAt;
}