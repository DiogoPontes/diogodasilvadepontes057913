package com.seplag.music.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name:albums}")
    private String bucketName;

    @Value("${minio.url:http://localhost:9000}")
    private String minioUrl;

    public StorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Faz upload de um arquivo para o MinIO.
     * Retorna o objectName (nome usado no bucket).
     */
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            ensureBucketExists();

            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            log.info("Arquivo enviado com sucesso: {} para bucket: {}", objectName, bucketName);
            return objectName;

        } catch (MinioException e) {
            log.error("Erro ao fazer upload do arquivo (MinioException): {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao fazer upload do arquivo: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao fazer upload: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao fazer upload: " + e.getMessage(), e);
        }
    }

    /**
     * Gera um objectName único para armazenar o arquivo no bucket.
     * Exemplo: "d290f1ee-6c54-4b01-90e6-XXXXXXXX_originalName.jpg"
     */
    public String generateObjectName(MultipartFile file) {
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        // sanitize filename: remove espaços e caracteres potencialmente problemáticos
        String safe = URLEncoder.encode(original, StandardCharsets.UTF_8).replaceAll("\\+", "_");
        return UUID.randomUUID().toString() + "_" + safe;
    }

    /**
     * Gera uma URL assinada (presigned) para download do arquivo.
     * Válida por 7 dias por padrão.
     */
    public String getPresignedUrl(String objectName) {
        return getPresignedUrl(objectName, 7, TimeUnit.DAYS);
    }

    /**
     * Gera uma URL assinada com tempo customizável.
     * @param objectName nome do objeto no bucket
     * @param duration duração (ex: 1)
     * @param timeUnit unidade de tempo (ex: TimeUnit.HOURS)
     * @return URL pré-assinada
     */
    public String getPresignedUrl(String objectName, int duration, TimeUnit timeUnit) {
        try {
            // Converter duração para segundos (algumas versões do SDK aceitam segundos)
            int expiryInSeconds = (int) timeUnit.toSeconds(duration);

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryInSeconds)
                            .build()
            );

            log.debug("URL presigned gerada para: {} (expiry {} {})", objectName, duration, timeUnit);
            return url;

        } catch (ErrorResponseException e) {
            // Erro específico de resposta do servidor — log detalhado
            log.error("Erro de resposta ao gerar presigned URL para {}: code={}, message={}",
                    objectName, e.errorResponse().code(), e.errorResponse().message(), e);
            throw new RuntimeException("Erro ao gerar presigned URL: " + e.errorResponse().message(), e);
        } catch (MinioException e) {
            log.error("Erro ao gerar presigned URL (MinioException) para {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar presigned URL: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao gerar presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar presigned URL: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna a URL pública (não-assinada) do arquivo, útil se o bucket for público.
     * Formato: {minioUrl}/{bucketName}/{objectName}
     */
    public String getPublicUrl(String objectName) {
        return String.format("%s/%s/%s", minioUrl.replaceAll("/+$", ""), bucketName, objectName);
    }

    /**
     * Deleta um arquivo do MinIO.
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            log.info("Arquivo deletado com sucesso: {}", objectName);

        } catch (MinioException e) {
            log.error("Erro ao deletar arquivo (MinioException): {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao deletar arquivo: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao deletar arquivo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao deletar arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se um arquivo existe no MinIO.
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;

        } catch (ErrorResponseException e) {
            // Se o servidor retornar 404, ErrorResponseException pode indicar isso
            if (e.errorResponse() != null && "NoSuchKey".equalsIgnoreCase(e.errorResponse().code())) {
                return false;
            }
            log.error("Erro de resposta ao verificar arquivo {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar arquivo: " + e.getMessage(), e);
        } catch (MinioException e) {
            // Algumas versões retornam MinioException para 404 — checar mensagem
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("404") || msg.toLowerCase().contains("not found")) {
                return false;
            }
            log.error("Erro ao verificar existência do arquivo (MinioException): {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar arquivo: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao verificar arquivo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Garante que o bucket exista; cria caso não exista.
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket criado: {}", bucketName);
            }
        } catch (MinioException e) {
            log.error("Erro ao verificar/criar bucket (MinioException): {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerenciar bucket: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao gerenciar bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerenciar bucket: " + e.getMessage(), e);
        }
    }
}