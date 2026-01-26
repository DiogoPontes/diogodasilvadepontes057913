package com.seplag.music.service;

import com.seplag.music.domain.dto.AlbumCreateUpdateDTO;
import com.seplag.music.domain.dto.AlbumDTO;
import com.seplag.music.domain.dto.AlbumMapper;
import com.seplag.music.domain.model.Album;
import com.seplag.music.domain.model.Artist;
import com.seplag.music.repository.AlbumRepository;
import com.seplag.music.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final AlbumMapper albumMapper;

    public AlbumDTO create(AlbumCreateUpdateDTO dto) {
        Album album = albumMapper.toEntity(dto);
        Album saved = albumRepository.save(album);
        return albumMapper.toDTO(saved);
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
        return albumMapper.toDTO(album);
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> findAll(Pageable pageable) {
        return albumRepository.findAll(pageable)
                .map(albumMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> findByTitle(String title, String order, Pageable pageable) {
        Page<Album> page;
        
        if ("desc".equalsIgnoreCase(order)) {
            page = albumRepository.findByTitleOrderDesc(title, pageable);
        } else {
            page = albumRepository.findByTitleOrderAsc(title, pageable);
        }
        
        return page.map(albumMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AlbumDTO> findByArtistId(Long artistId, Pageable pageable) {
        if (!artistRepository.existsById(artistId)) {
            throw new RuntimeException("Artista não encontrado com ID: " + artistId);
        }
        return albumRepository.findByArtistId(artistId, pageable)
                .map(albumMapper::toDTO);
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