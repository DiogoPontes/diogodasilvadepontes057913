package com.seplag.music.controller;

import com.seplag.music.domain.dto.AlbumCreateUpdateDTO;
import com.seplag.music.domain.dto.AlbumDTO;
import com.seplag.music.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Tag(name = "Albums", description = "Endpoints para gerenciar álbuns")
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping
    @Operation(summary = "Criar novo álbum")
    public ResponseEntity<AlbumDTO> create(@Valid @RequestBody AlbumCreateUpdateDTO dto) {
        AlbumDTO created = albumService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar álbum")
    public ResponseEntity<AlbumDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AlbumCreateUpdateDTO dto) {
        AlbumDTO updated = albumService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar álbum")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar álbum por ID")
    public ResponseEntity<AlbumDTO> findById(@PathVariable Long id) {
        AlbumDTO album = albumService.findById(id);
        return ResponseEntity.ok(album);
    }

    @GetMapping
    @Operation(summary = "Listar álbuns com paginação e filtros")
    public ResponseEntity<Page<AlbumDTO>> findAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long artistId,
            @RequestParam(defaultValue = "asc") String order,
            Pageable pageable) {
        
        Page<AlbumDTO> page;
        
        if (title != null && !title.isEmpty()) {
            page = albumService.findByTitle(title, order, pageable);
        } else if (artistId != null) {
            page = albumService.findByArtistId(artistId, pageable);
        } else {
            page = albumService.findAll(pageable);
        }
        
        return ResponseEntity.ok(page);
    }

    @PostMapping("/{albumId}/artists/{artistId}")
    @Operation(summary = "Adicionar artista a um álbum")
    public ResponseEntity<Void> addArtist(
            @PathVariable Long albumId,
            @PathVariable Long artistId) {
        albumService.addArtistToAlbum(albumId, artistId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{albumId}/artists/{artistId}")
    @Operation(summary = "Remover artista de um álbum")
    public ResponseEntity<Void> removeArtist(
            @PathVariable Long albumId,
            @PathVariable Long artistId) {
        albumService.removeArtistFromAlbum(albumId, artistId);
        return ResponseEntity.noContent().build();
    }
}