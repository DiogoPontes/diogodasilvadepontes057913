-- Adiciona novos campos à tabela album_cover
ALTER TABLE album_cover ADD COLUMN IF NOT EXISTS object_name VARCHAR(255) NOT NULL DEFAULT 'unknown';
ALTER TABLE album_cover ADD COLUMN IF NOT EXISTS content_type VARCHAR(100) NOT NULL DEFAULT 'image/jpeg';
ALTER TABLE album_cover ADD COLUMN IF NOT EXISTS file_size BIGINT NOT NULL DEFAULT 0;
ALTER TABLE album_cover ADD COLUMN IF NOT EXISTS is_primary BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE album_cover ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE album_cover ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Cria índice para melhorar performance
CREATE INDEX IF NOT EXISTS idx_album_cover_album_id ON album_cover(album_id);
CREATE INDEX IF NOT EXISTS idx_album_cover_is_primary ON album_cover(is_primary);