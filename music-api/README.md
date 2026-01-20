# music-api  
API REST para gestão de artistas, álbuns e regionais — Projeto do Concurso SEPLAG  

Repositório oficial conforme exigido:  
https://github.com/DiogoPontes/diogodasilvadepontes057913.git

## Tecnologias
- Java 21  
- Spring Boot 3  
- Spring Web, JPA, Security, WebSockets  
- JWT (expiração 5 minutos + refresh)  
- MinIO (S3)  
- PostgreSQL  
- Flyway  
- Docker + docker-compose  
- OpenAPI / Swagger  
- Bucket4j (Rate Limit)  
- Testes (JUnit + Mockito)

## Rodando com Docker
```bash
docker-compose up -d