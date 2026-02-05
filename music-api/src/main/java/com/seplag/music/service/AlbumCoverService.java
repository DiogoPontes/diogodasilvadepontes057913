package com.seplag.music.service;

import com.seplag.music.domain.dto.AlbumCoverDTO;
import com.seplag.music.domain.dto.AlbumCoverPresignedDTO;
import com.seplag.music.domain.model.Album;
import com.seplag.music.domain.model.AlbumCover;
import com.seplag.music.exception.BusinessException;
import com.seplag.music.repository.AlbumCoverRepository;
import com.seplag.music.repository.AlbumRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AlbumCoverService {

    private final AlbumCoverRepository albumCoverRepository;
    private final AlbumRepository albumRepository;
    private final StorageService storageService;

    public AlbumCoverService(AlbumCoverRepository albumCoverRepository,
                            AlbumRepository albumRepository,
                            StorageService storageService) {
        this.albumCoverRepository = albumCoverRepository;
        this.albumRepository = albumRepository;
        this.storageService = storageService;
    }

    /**
     * Faz upload de uma capa para um álbum
     */
    @Transactional
    public AlbumCoverPresignedDTO uploadCover(Long albumId, MultipartFile file) {
        log.info("Iniciando upload de capa para álbum ID: {}", albumId);

        // Valida se o álbum existe
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> BusinessException.notFound("Álbum não encontrado com ID: " + albumId));

        // Valida o arquivo
        if (file.isEmpty()) {
            throw BusinessException.badRequest("Arquivo não pode estar vazio");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw BusinessException.badRequest("Arquivo não pode exceder 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw BusinessException.badRequest("Apenas arquivos de imagem são permitidos");
        }

        try {
            // Gera nome único para o arquivo
            String objectName = generateObjectName(albumId, file.getOriginalFilename());

            // Faz upload para MinIO
            storageService.uploadFile(file, objectName);

            // Se é a primeira capa, marca como primária
            boolean isPrimary = albumCoverRepository.countByAlbumId(albumId) == 0;

            // Salva registro no banco
            AlbumCover cover = AlbumCover.builder()
                    .album(album)
                    .fileName(file.getOriginalFilename())
                    .objectName(objectName)
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .isPrimary(isPrimary)
                    .build();

            AlbumCover saved = albumCoverRepository.save(cover);

            // Gera presigned URL
            String presignedUrl = storageService.getPresignedUrl(objectName);

            log.info("Capa enviada com sucesso para álbum ID: {}", albumId);

            return AlbumCoverPresignedDTO.builder()
                    .id(saved.getId())
                    .albumId(saved.getAlbum().getId())
                    .fileName(saved.getFileName())
                    .contentType(saved.getContentType())
                    .fileSize(saved.getFileSize())
                    .isPrimary(saved.getIsPrimary())
                    .presignedUrl(presignedUrl)
                    .createdAt(saved.getCreatedAt())
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao fazer upload de capa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao fazer upload de capa: " + e.getMessage());
        }
    }

    /**
     * Lista todas as capas de um álbum
     */
    @Transactional(readOnly = true)
    public List<AlbumCoverDTO> listCoversByAlbum(Long albumId) {
        log.info("Listando capas do álbum ID: {}", albumId);

        // Valida se o álbum existe
        if (!albumRepository.existsById(albumId)) {
            throw BusinessException.notFound("Álbum não encontrado com ID: " + albumId);
        }

        List<AlbumCover> covers = albumCoverRepository.findByAlbumIdOrderByCreatedAtDesc(albumId);

        return covers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtém a capa primária de um álbum
     */
    @Transactional(readOnly = true)
    public AlbumCoverDTO getPrimaryCover(Long albumId) {
        log.info("Buscando capa primária do álbum ID: {}", albumId);

        AlbumCover cover = albumCoverRepository.findByAlbumIdAndIsPrimaryTrue(albumId)
                .orElseThrow(() -> BusinessException.notFound("Capa primária não encontrada para o álbum ID: " + albumId));

        return convertToDTO(cover);
    }

    /**
     * Deleta uma capa específica
     */
    @Transactional
    public void deleteCover(Long albumId, Long coverId) {
        log.info("Deletando capa ID: {} do álbum ID: {}", coverId, albumId);

        AlbumCover cover = albumCoverRepository.findById(coverId)
                .orElseThrow(() -> BusinessException.notFound("Capa não encontrada com ID: " + coverId));

        // Valida se a capa pertence ao álbum
        if (!cover.getAlbum().getId().equals(albumId)) {
            throw BusinessException.badRequest("Capa não pertence ao álbum especificado");
        }

        try {
            // Deleta do MinIO
            storageService.deleteFile(cover.getObjectName());

            // Se era a capa primária, marca a próxima como primária
            if (cover.getIsPrimary()) {
                albumCoverRepository.findByAlbumIdOrderByCreatedAtDesc(albumId)
                        .stream()
                        .filter(c -> !c.getId().equals(coverId))
                        .findFirst()
                        .ifPresent(nextCover -> {
                            nextCover.setIsPrimary(true);
                            albumCoverRepository.save(nextCover);
                        });
            }

            // Deleta do banco
            albumCoverRepository.delete(cover);

            log.info("Capa deletada com sucesso: {}", coverId);

        } catch (Exception e) {
            log.error("Erro ao deletar capa: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao deletar capa: " + e.getMessage());
        }
    }

    /**
     * Deleta todas as capas de um álbum
     */
    @Transactional
    public void deleteAllCoversFromAlbum(Long albumId) {
        log.info("Deletando todas as capas do álbum ID: {}", albumId);

        List<AlbumCover> covers = albumCoverRepository.findByAlbumId(albumId);

        covers.forEach(cover -> {
            try {
                storageService.deleteFile(cover.getObjectName());
                albumCoverRepository.delete(cover);
            } catch (Exception e) {
                log.error("Erro ao deletar capa ID: {}: {}", cover.getId(), e.getMessage());
            }
        });

        log.info("Todas as capas do álbum ID: {} foram deletadas", albumId);
    }

    /**
     * Define uma capa como primária
     */
    @Transactional
    public AlbumCoverDTO setPrimaryCover(Long albumId, Long coverId) {
        log.info("Definindo capa ID: {} como primária para álbum ID: {}", coverId, albumId);

        AlbumCover cover = albumCoverRepository.findById(coverId)
                .orElseThrow(() -> BusinessException.notFound("Capa não encontrada com ID: " + coverId));

        if (!cover.getAlbum().getId().equals(albumId)) {
            throw BusinessException.badRequest("Capa não pertence ao álbum especificado");
        }

        // Remove primária anterior
        albumCoverRepository.findByAlbumIdAndIsPrimaryTrue(albumId)
                .ifPresent(oldPrimary -> {
                    oldPrimary.setIsPrimary(false);
                    albumCoverRepository.save(oldPrimary);
                });

        // Define nova primária
        cover.setIsPrimary(true);
        AlbumCover updated = albumCoverRepository.save(cover);

        return convertToDTO(updated);
    }

    /**
     * Gera um nome único para o arquivo no MinIO
     */
    private String generateObjectName(Long albumId, String originalFilename) {
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

        return String.format("albums/%d/%s%s", albumId, UUID.randomUUID(), extension);
    }

    /**
     * Converte AlbumCover para DTO com presigned URL
     */
    private AlbumCoverDTO convertToDTO(AlbumCover cover) {
        String presignedUrl = storageService.getPresignedUrl(cover.getObjectName());

        return AlbumCoverDTO.builder()
                .id(cover.getId())
                .albumId(cover.getAlbum().getId())
                .fileName(cover.getFileName())
                .contentType(cover.getContentType())
                .fileSize(cover.getFileSize())
                .isPrimary(cover.getIsPrimary())
                .presignedUrl(presignedUrl)
                .createdAt(cover.getCreatedAt())
                .build();
    }
}