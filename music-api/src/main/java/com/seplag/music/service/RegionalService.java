package com.seplag.music.service;

import com.seplag.music.domain.dto.RegionalDTO;
import com.seplag.music.domain.dto.RegionalMapper;
import com.seplag.music.domain.model.Regional;
import com.seplag.music.repository.RegionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RegionalService {

    private final RegionalRepository regionalRepository;
    private final RegionalMapper regionalMapper;

    @Transactional(readOnly = true)
    public RegionalDTO findById(Integer id) {
        Regional regional = regionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regional não encontrada com ID: " + id));
        return regionalMapper.toDTO(regional);
    }

    @Transactional(readOnly = true)
    public Page<RegionalDTO> findAllAtivos(Pageable pageable) {
        return regionalRepository.findByAtivoTrue(pageable)
                .map(regionalMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<RegionalDTO> findAllAtivos() {
        return regionalRepository.findByAtivoTrue()
                .stream()
                .map(regionalMapper::toDTO)
                .toList();
    }

    public RegionalDTO create(RegionalDTO dto) {
        Regional regional = regionalMapper.toEntity(dto);
        regional.setUpdatedAt(LocalDateTime.now());
        Regional saved = regionalRepository.save(regional);
        return regionalMapper.toDTO(saved);
    }

    public RegionalDTO update(Integer id, RegionalDTO dto) {
        Regional regional = regionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regional não encontrada com ID: " + id));
        regional.setNome(dto.getNome());
        regional.setAtivo(dto.getAtivo());
        regional.setUpdatedAt(LocalDateTime.now());
        Regional updated = regionalRepository.save(regional);
        return regionalMapper.toDTO(updated);
    }

    public void inativar(Integer id) {
        Regional regional = regionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regional não encontrada com ID: " + id));
        regional.setAtivo(false);
        regional.setUpdatedAt(LocalDateTime.now());
        regionalRepository.save(regional);
    }
}