package com.seplag.music.service;

import com.seplag.music.domain.dto.AlbumCreateUpdateDTO;
import com.seplag.music.domain.dto.AlbumDTO;
import com.seplag.music.domain.dto.AlbumMapper;
import com.seplag.music.domain.dto.AlbumCoverDTO;
import com.seplag.music.domain.dto.AlbumNotificationDTO;
import com.seplag.music.domain.model.Album;
import com.seplag.music.domain.model.Artist;
import com.seplag.music.repository.AlbumRepository;
import com.seplag.music.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final AlbumMapper albumMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final StorageService storageService; // injetei o StorageService aqui

    public AlbumDTO create(AlbumCreateUpdateDTO dto) {
        Album album = albumMapper.toEntity(dto);
        Album saved = albumRepository.save(album);
        AlbumDTO result = albumMapper.toDTO(saved);

        // Envia notificação via WebSocket APÓS o commit da transação
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    AlbumNotificationDTO notification = new AlbumNotificationDTO(
                            saved.getId(),
                            saved.getTitle(),
                            "Novo álbum criado: " + saved.getTitle()
                    );
                    messagingTemplate.convertAndSend("/topic/albums", notification);
                }
            });
        } else {
            // fallback: envia imediatamente caso não haja transação ativa
            AlbumNotificationDTO notification = new AlbumNotificationDTO(
                    saved.getId(),
                    saved.getTitle(),
                    "Novo álbum criado: " + saved.getTitle()
            );
            messagingTemplate.convertAndSend("/topic/albums", notification);
        }

        return result;
    }

    public AlbumDTO update(Long id, AlbumCreateUpdateDTO dto) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + id));
        albumMapper.updateEntity(dto, album);
        Album updated = albumRepository.save(album);
        return albumMapper.toDTO(updated);
    }

    public void delete(Long id) {
        if (!albumRepository.existsById(id)) {
            throw new RuntimeException("Álbum não encontrado com ID: " + id);
        }
        albumRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public AlbumDTO findById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + id));

        // Força inicialização das collections LAZY dentro da transação
        if (album.getCovers() != null) {
            album.getCovers().size();
        }
        if (album.getArtists() != null) {
            album.getArtists().size();
        }

        AlbumDTO dto = albumMapper.toDTO(album);

        // Preenche URLs nas covers (presigned ou pública como fallback)
        if (dto.getCovers() != null && !dto.getCovers().isEmpty()) {
            dto.getCovers().forEach(c -> {
                String objectName = c.getObjectName() != null ? c.getObjectName() : c.getFileName();
                try {
                    c.setUrl(storageService.getPresignedUrl(objectName));
                } catch (Exception e) {
                    try {
                        c.setUrl(storageService.getPublicUrl(objectName));
                    } catch (Exception ex) {
                        // deixa null caso ambos falhem
                    }
                }
            });
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> findAll(Pageable pageable) {
        return albumRepository.findAll(pageable)
                .map(album -> {
                    // Força inicialização das collections LAZY dentro da transação
                    if (album.getCovers() != null) {
                        album.getCovers().size();
                    }
                    if (album.getArtists() != null) {
                        album.getArtists().size();
                    }

                    AlbumDTO dto = albumMapper.toDTO(album);

                    if (dto.getCovers() != null && !dto.getCovers().isEmpty()) {
                        Set<AlbumCoverDTO> coversWithUrl = dto.getCovers().stream().map(c -> {
                            String objectName = c.getObjectName() != null ? c.getObjectName() : c.getFileName();
                            String url = null;
                            try {
                                url = storageService.getPresignedUrl(objectName);
                            } catch (Exception e) {
                                try {
                                    url = storageService.getPublicUrl(objectName);
                                } catch (Exception ex) {
                                    // url permanece null
                                }
                            }
                            c.setUrl(url);
                            return c;
                        }).collect(Collectors.toSet());
                        dto.setCovers(coversWithUrl);
                    }

                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> findByTitle(String title, String order, Pageable pageable) {
        Page<Album> page;

        if ("desc".equalsIgnoreCase(order)) {
            page = albumRepository.findByTitleOrderDesc(title, pageable);
        } else {
            page = albumRepository.findByTitleOrderAsc(title, pageable);
        }

        return page.map(album -> {
            // inicializa covers/artists
            if (album.getCovers() != null) album.getCovers().size();
            if (album.getArtists() != null) album.getArtists().size();

            AlbumDTO dto = albumMapper.toDTO(album);

            if (dto.getCovers() != null && !dto.getCovers().isEmpty()) {
                Set<AlbumCoverDTO> coversWithUrl = dto.getCovers().stream().map(c -> {
                    String objectName = c.getObjectName() != null ? c.getObjectName() : c.getFileName();
                    String url = null;
                    try {
                        url = storageService.getPresignedUrl(objectName);
                    } catch (Exception e) {
                        try {
                            url = storageService.getPublicUrl(objectName);
                        } catch (Exception ex) {
                            // url permanece null
                        }
                    }
                    c.setUrl(url);
                    return c;
                }).collect(Collectors.toSet());
                dto.setCovers(coversWithUrl);
            }

            return dto;
        });
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> findByArtistId(Long artistId, Pageable pageable) {
        if (!artistRepository.existsById(artistId)) {
            throw new RuntimeException("Artista não encontrado com ID: " + artistId);
        }

        return albumRepository.findByArtistId(artistId, pageable)
                .map(album -> {
                    // inicializa covers/artists
                    if (album.getCovers() != null) album.getCovers().size();
                    if (album.getArtists() != null) album.getArtists().size();

                    AlbumDTO dto = albumMapper.toDTO(album);

                    if (dto.getCovers() != null && !dto.getCovers().isEmpty()) {
                        Set<AlbumCoverDTO> coversWithUrl = dto.getCovers().stream().map(c -> {
                            String objectName = c.getObjectName() != null ? c.getObjectName() : c.getFileName();
                            String url = null;
                            try {
                                url = storageService.getPresignedUrl(objectName);
                            } catch (Exception e) {
                                try {
                                    url = storageService.getPublicUrl(objectName);
                                } catch (Exception ex) {
                                    // url permanece null
                                }
                            }
                            c.setUrl(url);
                            return c;
                        }).collect(Collectors.toSet());
                        dto.setCovers(coversWithUrl);
                    }

                    return dto;
                });
    }

    public void addArtistToAlbum(Long albumId, Long artistId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + albumId));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artista não encontrado com ID: " + artistId));

        album.getArtists().add(artist);
        albumRepository.save(album);
    }

    public void removeArtistFromAlbum(Long albumId, Long artistId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new RuntimeException("Álbum não encontrado com ID: " + albumId));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artista não encontrado com ID: " + artistId));

        album.getArtists().remove(artist);
        albumRepository.save(album);
    }
}