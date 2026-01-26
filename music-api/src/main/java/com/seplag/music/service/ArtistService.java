package com.seplag.music.service;

import com.seplag.music.domain.dto.ArtistCreateUpdateDTO;
import com.seplag.music.domain.dto.ArtistDTO;
import com.seplag.music.domain.dto.ArtistMapper;
import com.seplag.music.domain.model.Artist;
import com.seplag.music.domain.model.ArtistType;
import com.seplag.music.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;

    public ArtistDTO create(ArtistCreateUpdateDTO dto) {
        Artist artist = artistMapper.toEntity(dto);
        Artist saved = artistRepository.save(artist);
        return artistMapper.toDTO(saved);
    }

    public ArtistDTO update(Long id, ArtistCreateUpdateDTO dto) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artista não encontrado com ID: " + id));
        artistMapper.updateEntity(dto, artist);
        Artist updated = artistRepository.save(artist);
        return artistMapper.toDTO(updated);
    }

    public void delete(Long id) {
        if (!artistRepository.existsById(id)) {
            throw new RuntimeException("Artista não encontrado com ID: " + id);
        }
        artistRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ArtistDTO findById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artista não encontrado com ID: " + id));
        return artistMapper.toDTO(artist);
    }

    @Transactional(readOnly = true)
    public Page<ArtistDTO> findByName(String name, String order, Pageable pageable) {
        Page<Artist> page;
        
        if ("desc".equalsIgnoreCase(order)) {
            page = artistRepository.findByNameOrderDesc(name, pageable);
        } else {
            page = artistRepository.findByNameOrderAsc(name, pageable);
        }
        
        return page.map(artistMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ArtistDTO> findByType(ArtistType type, Pageable pageable) {
        return artistRepository.findByType(type, pageable)
                .map(artistMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ArtistDTO> findAll(Pageable pageable) {
        return artistRepository.findAll(pageable)
                .map(artistMapper::toDTO);
    }
}