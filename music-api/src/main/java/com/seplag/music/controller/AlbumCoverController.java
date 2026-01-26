package com.seplag.music.controller;

import com.seplag.music.domain.dto.AlbumCoverPresignedDTO;
import com.seplag.music.service.AlbumCoverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/album-covers")
@RequiredArgsConstructor
@Tag(name = "Album Covers", description = "Endpoints para gerenciar capas de álbuns")
public class AlbumCoverController {

    private final AlbumCoverService albumCoverService;

    @PostMapping("/upload/{albumId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Upload de capa", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseEntity<AlbumCoverPresignedDTO> uploadCover(
            @PathVariable Long albumId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(albumCoverService.uploadCover(albumId, file));
    }

    @GetMapping("/{coverId}/presigned-url")
    public ResponseEntity<AlbumCoverPresignedDTO> getPresignedUrl(@PathVariable Long coverId) {
        return ResponseEntity.ok(albumCoverService.getCoverPresignedUrl(coverId));
    }

    @GetMapping("/album/{albumId}")
    public ResponseEntity<List<AlbumCoverPresignedDTO>> getCoversByAlbum(@PathVariable Long albumId) {
        return ResponseEntity.ok(albumCoverService.getCoversByAlbum(albumId));
    }

    @DeleteMapping("/{coverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteCover(@PathVariable Long coverId) {
        albumCoverService.deleteCover(coverId);
        return ResponseEntity.noContent().build();
    }
}