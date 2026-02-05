package com.seplag.music.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "album_cover")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumCover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Evita incluir album em toString/equals para não forçar fetch eager
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Album album;

    @Column(name = "filename")
    private String fileName;

    @Column(name = "object_name", nullable = false)
    private String objectName; // Nome do arquivo no MinIO / caminho no bucket

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "is_primary")
    private Boolean isPrimary; // Define se é a capa principal

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}