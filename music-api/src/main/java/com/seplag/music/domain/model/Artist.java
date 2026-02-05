package com.seplag.music.domain.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "artist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"albums"})
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArtistType type;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonIgnore
    @ManyToMany(mappedBy = "artists")
    private Set<Album> albums = new HashSet<>();
}