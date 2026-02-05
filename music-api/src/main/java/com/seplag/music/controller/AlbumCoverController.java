package com.seplag.music.controller;

import com.seplag.music.domain.dto.AlbumCoverDTO;
import com.seplag.music.domain.dto.AlbumCoverPresignedDTO;
import com.seplag.music.service.AlbumCoverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/albums/{albumId}/covers")
@Tag(name = "Album Covers", description = "Gerenciamento de capas de álbuns")
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
public class AlbumCoverController {

    private final AlbumCoverService albumCoverService;

    public AlbumCoverController(AlbumCoverService albumCoverService) {
        this.albumCoverService = albumCoverService;
    }

    /**
     * Faz upload de uma nova capa para um álbum
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload de capa do álbum",
            description = "Faz upload de uma imagem como capa do álbum. A primeira capa será marcada como primária.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Capa enviada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Arquivo inválido ou álbum não encontrado"),
                    @ApiResponse(responseCode = "401", description = "Não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Álbum não encontrado")
            }
    )
    public ResponseEntity<AlbumCoverPresignedDTO> uploadCover(
            @Parameter(description = "ID do álbum", required = true)
            @PathVariable Long albumId,
            @Parameter(description = "Arquivo de imagem (JPG, PNG, WebP, etc.)", required = true)
            @RequestParam("file") MultipartFile file) {

        log.info("Recebido upload de capa para álbum ID: {}", albumId);
        AlbumCoverPresignedDTO result = albumCoverService.uploadCover(albumId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Lista todas as capas de um álbum
     */
    @GetMapping
    @Operation(
            summary = "Listar capas do álbum",
            description = "Retorna todas as capas associadas a um álbum com URLs presigned para download",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de capas retornada com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Álbum não encontrado")
            }
    )
    public ResponseEntity<List<AlbumCoverDTO>> listCovers(
            @Parameter(description = "ID do álbum", required = true)
            @PathVariable Long albumId) {

        log.info("Listando capas do álbum ID: {}", albumId);
        List<AlbumCoverDTO> covers = albumCoverService.listCoversByAlbum(albumId);
        return ResponseEntity.ok(covers);
    }

    /**
     * Obtém a capa primária de um álbum
     */
    @GetMapping("/primary")
    @Operation(
            summary = "Obter capa primária",
            description = "Retorna a capa primária (principal) de um álbum",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Capa primária retornada com sucesso"),
                    @ApiResponse(responseCode = "401", description = "Não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Álbum ou capa primária não encontrada")
            }
    )
    public ResponseEntity<AlbumCoverDTO> getPrimaryCover(
            @Parameter(description = "ID do álbum", required = true)
            @PathVariable Long albumId) {

        log.info("Buscando capa primária do álbum ID: {}", albumId);
        AlbumCoverDTO cover = albumCoverService.getPrimaryCover(albumId);
        return ResponseEntity.ok(cover);
    }

    /**
     * Define uma capa como primária
     */
    @PutMapping("/{coverId}/set-primary")
    @Operation(
            summary = "Definir capa como primária",
            description = "Define uma capa específica como a capa primária do álbum",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Capa definida como primária com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Capa não pertence ao álbum"),
                    @ApiResponse(responseCode = "401", description = "Não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Capa ou álbum não encontrado")
            }
    )
    public ResponseEntity<AlbumCoverDTO> setPrimaryCover(
            @Parameter(description = "ID do álbum", required = true)
            @PathVariable Long albumId,
            @Parameter(description = "ID da capa", required = true)
            @PathVariable Long coverId) {

        log.info("Definindo capa ID: {} como primária para álbum ID: {}", coverId, albumId);
        AlbumCoverDTO cover = albumCoverService.setPrimaryCover(albumId, coverId);
        return ResponseEntity.ok(cover);
    }

    /**
     * Deleta uma capa específica
     */
    @DeleteMapping("/{coverId}")
    @Operation(
            summary = "Deletar capa",
            description = "Remove uma capa específica do álbum. Se for a primária, a próxima será marcada como primária.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Capa deletada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Capa não pertence ao álbum"),
                    @ApiResponse(responseCode = "401", description = "Não autenticado"),
                    @ApiResponse(responseCode = "404", description = "Capa ou álbum não encontrado")
            }
    )
    public ResponseEntity<Void> deleteCover(
            @Parameter(description = "ID do álbum", required = true)
            @PathVariable Long albumId,
            @Parameter(description = "ID da capa", required = true)
            @PathVariable Long coverId) {

        log.info("Deletando capa ID: {} do álbum ID: {}", coverId, albumId);
        albumCoverService.deleteCover(albumId, coverId);
        return ResponseEntity.noContent().build();
    }
}