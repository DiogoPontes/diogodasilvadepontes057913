package com.seplag.music.repository;

import com.seplag.music.domain.model.AlbumCover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumCoverRepository extends JpaRepository<AlbumCover, Long> {

    List<AlbumCover> findByAlbumId(Long albumId);

    List<AlbumCover> findByAlbumIdOrderByCreatedAtDesc(Long albumId);

    Optional<AlbumCover> findByAlbumIdAndIsPrimaryTrue(Long albumId);

    long countByAlbumId(Long albumId);
}