package com.seplag.music.controller;

import com.seplag.music.domain.dto.ArtistCreateUpdateDTO;
import com.seplag.music.domain.dto.ArtistDTO;
import com.seplag.music.domain.model.ArtistType;
import com.seplag.music.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
@Tag(name = "Artists", description = "Endpoints para gerenciar artistas")
public class ArtistController {

    private final ArtistService artistService;

    @PostMapping
    @Operation(summary = "Criar novo artista")
    public ResponseEntity<ArtistDTO> create(@Valid @RequestBody ArtistCreateUpdateDTO dto) {
        ArtistDTO created = artistService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar artista")
    public ResponseEntity<ArtistDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ArtistCreateUpdateDTO dto) {
        ArtistDTO updated = artistService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar artista")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar artista por ID")
    public ResponseEntity<ArtistDTO> findById(@PathVariable Long id) {
        ArtistDTO artist = artistService.findById(id);
        return ResponseEntity.ok(artist);
    }

    @GetMapping
    @Operation(summary = "Listar artistas com filtros")
    public ResponseEntity<Page<ArtistDTO>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ArtistType type,
            @RequestParam(defaultValue = "asc") String order,
            Pageable pageable) {
        
        Page<ArtistDTO> page;
        
        if (name != null && !name.isEmpty()) {
            page = artistService.findByName(name, order, pageable);
        } else if (type != null) {
            page = artistService.findByType(type, pageable);
        } else {
            page = artistService.findAll(pageable);
        }
        
        return ResponseEntity.ok(page);
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Você é um admin!");
    }
}