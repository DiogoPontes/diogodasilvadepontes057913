package com.seplag.music.service;

import com.seplag.music.domain.dto.AlbumCoverPresignedDTO;
import com.seplag.music.domain.model.Album;
import com.seplag.music.domain.model.AlbumCover;
import com.seplag.music.repository.AlbumCoverRepository;
import com.seplag.music.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AlbumCoverService {

    private final AlbumCoverRepository albumCoverRepository;
    private final AlbumRepository albumRepository;
    private final StorageService storageService;

    public AlbumCoverPresignedDTO uploadCover(Long albumId, MultipartFile file) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado"));

        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo vazio");
        }

        // Faz o upload para o MinIO usando o StorageService
        String fileName = storageService.uploadFile(file);

        // Salva no banco usando os campos da sua entidade: objectName e createdAt
        AlbumCover albumCover = AlbumCover.builder()
                .album(album)
                .objectName(fileName)
                .createdAt(LocalDateTime.now())
                .build();

        AlbumCover savedCover = albumCoverRepository.save(albumCover);

        // Retorna o DTO usando seus campos: coverId, presignedUrl, expiresIn
        return AlbumCoverPresignedDTO.builder()
                .coverId(savedCover.getId())
                .presignedUrl(storageService.getPresignedUrl(fileName))
                .expiresIn("60 minutes")
                .build();
    }

    @Transactional(readOnly = true)
    public AlbumCoverPresignedDTO getCoverPresignedUrl(Long coverId) {
        AlbumCover cover = albumCoverRepository.findById(coverId)
                .orElseThrow(() -> new RuntimeException("Capa não encontrada"));

        return AlbumCoverPresignedDTO.builder()
                .coverId(cover.getId())
                .presignedUrl(storageService.getPresignedUrl(cover.getObjectName()))
                .expiresIn("60 minutes")
                .build();
    }

    @Transactional(readOnly = true)
    public List<AlbumCoverPresignedDTO> getCoversByAlbum(Long albumId) {
        return albumCoverRepository.findByAlbumId(albumId).stream()
                .map(cover -> AlbumCoverPresignedDTO.builder()
                        .coverId(cover.getId())
                        .presignedUrl(storageService.getPresignedUrl(cover.getObjectName()))
                        .expiresIn("60 minutes")
                        .build())
                .collect(Collectors.toList());
    }

    public void deleteCover(Long coverId) {
        AlbumCover cover = albumCoverRepository.findById(coverId)
                .orElseThrow(() -> new RuntimeException("Capa não encontrada"));
        albumCoverRepository.delete(cover);
    }
}