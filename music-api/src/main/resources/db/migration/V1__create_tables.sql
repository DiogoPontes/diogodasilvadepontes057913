-- ============================================
-- V1 - CREATE TABLES
-- ============================================

-- ARTIST (cantores / bandas)
CREATE TABLE artist (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL, -- SOLO ou BANDA
    created_at TIMESTAMP DEFAULT NOW()
);

-- ALBUM
CREATE TABLE album (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    release_year INTEGER,
    created_at TIMESTAMP DEFAULT NOW()
);

-- RELACIONAMENTO N:N ENTRE ARTIST E ALBUM
CREATE TABLE artist_album (
    artist_id BIGINT NOT NULL REFERENCES artist(id),
    album_id BIGINT NOT NULL REFERENCES album(id),
    PRIMARY KEY (artist_id, album_id)
);

-- CAPAS DE ÁLBUM (MinIO)
CREATE TABLE album_cover (
    id BIGSERIAL PRIMARY KEY,
    album_id BIGINT NOT NULL REFERENCES album(id),
    object_name VARCHAR(255) NOT NULL, -- caminho do arquivo no MinIO
    created_at TIMESTAMP DEFAULT NOW()
);

-- REGIONAIS IMPORTADAS DA API EXTERNA
CREATE TABLE regional (
    id INTEGER PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT NOW()
);