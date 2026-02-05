package com.seplag.music.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumNotificationDTO {
    private Long albumId;
    private String title;
    private String message;
}