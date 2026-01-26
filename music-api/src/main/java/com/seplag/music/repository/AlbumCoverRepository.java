package com.seplag.music.repository;

import com.seplag.music.domain.model.AlbumCover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumCoverRepository extends JpaRepository<AlbumCover, Long> {

    List<AlbumCover> findByAlbumId(Long albumId);
}