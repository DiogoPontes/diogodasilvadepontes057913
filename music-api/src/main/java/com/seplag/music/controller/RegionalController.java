package com.seplag.music.controller;

import com.seplag.music.domain.dto.RegionalDTO;
import com.seplag.music.service.RegionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/regionais")
@RequiredArgsConstructor
@Tag(name = "Regionais", description = "Endpoints para gerenciar regionais")
public class RegionalController {

    private final RegionalService regionalService;

    @GetMapping("/{id}")
    @Operation(summary = "Buscar regional por ID")
    public ResponseEntity<RegionalDTO> findById(@PathVariable Integer id) {
        RegionalDTO regional = regionalService.findById(id);
        return ResponseEntity.ok(regional);
    }

    @GetMapping
    @Operation(summary = "Listar regionais ativas com paginação")
    public ResponseEntity<Page<RegionalDTO>> findAll(Pageable pageable) {
        Page<RegionalDTO> page = regionalService.findAllAtivos(pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping
    @Operation(summary = "Criar nova regional")
    public ResponseEntity<RegionalDTO> create(@RequestBody RegionalDTO dto) {
        RegionalDTO created = regionalService.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar regional")
    public ResponseEntity<RegionalDTO> update(
            @PathVariable Integer id,
            @RequestBody RegionalDTO dto) {
        RegionalDTO updated = regionalService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/inativar")
    @Operation(summary = "Inativar regional")
    public ResponseEntity<Void> inativar(@PathVariable Integer id) {
        regionalService.inativar(id);
        return ResponseEntity.noContent().build();
    }
}