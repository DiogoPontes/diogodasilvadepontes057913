package com.seplag.music.repository;

import com.seplag.music.domain.model.Regional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionalRepository extends JpaRepository<Regional, Integer> {

    List<Regional> findByAtivoTrue();

    Page<Regional> findByAtivoTrue(Pageable pageable);
}