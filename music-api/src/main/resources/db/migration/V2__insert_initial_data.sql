-- ============================================
-- V2 - INSERT INITIAL DATA
-- ============================================

-- ARTISTS
INSERT INTO artist (name, type) VALUES ('Serj Tankian', 'SOLO');
INSERT INTO artist (name, type) VALUES ('Mike Shinoda', 'SOLO');
INSERT INTO artist (name, type) VALUES ('Michel Teló', 'SOLO');
INSERT INTO artist (name, type) VALUES ('Guns N’ Roses', 'BANDA');

-- ALBUMS
INSERT INTO album (title, release_year) VALUES ('Harakiri', 2012);
INSERT INTO album (title, release_year) VALUES ('Black Blooms', 2018);
INSERT INTO album (title, release_year) VALUES ('The Rough Dog', 2020);

INSERT INTO album (title, release_year) VALUES ('The Rising Tied', 2005);
INSERT INTO album (title, release_year) VALUES ('Post Traumatic', 2018);
INSERT INTO album (title, release_year) VALUES ('Post Traumatic EP', 2018);
INSERT INTO album (title, release_year) VALUES ('Where’d You Go', 2006);

INSERT INTO album (title, release_year) VALUES ('Bem Sertanejo', 2014);
INSERT INTO album (title, release_year) VALUES ('Bem Sertanejo - O Show (Ao Vivo)', 2017);
INSERT INTO album (title, release_year) VALUES ('Bem Sertanejo - (1ª Temporada) - EP', 2014);

INSERT INTO album (title, release_year) VALUES ('Use Your Illusion I', 1991);
INSERT INTO album (title, release_year) VALUES ('Use Your Illusion II', 1991);
INSERT INTO album (title, release_year) VALUES ('Greatest Hits', 2004);

-- ARTIST-ALBUM MAPPING

-- Serj Tankian (id = 1)
INSERT INTO artist_album (artist_id, album_id) VALUES (1, 1);
INSERT INTO artist_album (artist_id, album_id) VALUES (1, 2);
INSERT INTO artist_album (artist_id, album_id) VALUES (1, 3);

-- Mike Shinoda (id = 2)
INSERT INTO artist_album (artist_id, album_id) VALUES (2, 4);
INSERT INTO artist_album (artist_id, album_id) VALUES (2, 5);
INSERT INTO artist_album (artist_id, album_id) VALUES (2, 6);
INSERT INTO artist_album (artist_id, album_id) VALUES (2, 7);

-- Michel Teló (id = 3)
INSERT INTO artist_album (artist_id, album_id) VALUES (3, 8);
INSERT INTO artist_album (artist_id, album_id) VALUES (3, 9);
INSERT INTO artist_album (artist_id, album_id) VALUES (3, 10);

-- Guns N’ Roses (id = 4)
INSERT INTO artist_album (artist_id, album_id) VALUES (4, 11);
INSERT INTO artist_album (artist_id, album_id) VALUES (4, 12);
INSERT INTO artist_album (artist_id, album_id) VALUES (4, 13);