package com.seplag.music.service;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
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
     * Faz upload de um arquivo para o MinIO
     */
    public String uploadFile(MultipartFile file, String objectName) {
        try {
            // Garante que o bucket existe
            ensureBucketExists();

            // Faz upload do arquivo
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("Arquivo enviado com sucesso: {} para bucket: {}", objectName, bucketName);
            return objectName;

        } catch (MinioException e) {
            log.error("Erro ao fazer upload do arquivo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao fazer upload do arquivo: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao fazer upload: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao fazer upload: " + e.getMessage());
        }
    }

    /**
     * Gera uma URL assinada (presigned) para download do arquivo
     * Válida por 7 dias por padrão
     */
    public String getPresignedUrl(String objectName) {
        return getPresignedUrl(objectName, 7, TimeUnit.DAYS);
    }

    /**
     * Gera uma URL assinada com tempo customizável
     */
    public String getPresignedUrl(String objectName, int duration, TimeUnit timeUnit) {
        try {
            // Convertendo a duração para segundos, que é o que o método expiry() geralmente espera
            int expiryInSeconds = (int) timeUnit.toSeconds(duration);

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expiryInSeconds) // Alterado de expiration() para expiry()
                            .build()
            );

            log.info("URL presigned gerada para: {}", objectName);
            return url;

        } catch (MinioException e) {
            log.error("Erro ao gerar presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar URL presigned: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao gerar presigned URL: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar URL: " + e.getMessage());
        }
    }
    /**
     * Obtém a URL pública do arquivo (sem assinatura)
     * Útil se o bucket estiver configurado como público
     */
    public String getPublicUrl(String objectName) {
        return String.format("%s/%s/%s", minioUrl, bucketName, objectName);
    }

    /**
     * Deleta um arquivo do MinIO
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
            log.error("Erro ao deletar arquivo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao deletar arquivo: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao deletar arquivo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao deletar arquivo: " + e.getMessage());
        }
    }

    /**
     * Verifica se um arquivo existe no MinIO
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

        } catch (MinioException e) {
            if (e.getMessage().contains("404")) {
                return false;
            }
            log.error("Erro ao verificar existência do arquivo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar arquivo: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao verificar arquivo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao verificar arquivo: " + e.getMessage());
        }
    }

    /**
     * Garante que o bucket existe, cria se não existir
     */
    private void ensureBucketExists() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket criado: {}", bucketName);
            }
        } catch (MinioException e) {
            log.error("Erro ao verificar/criar bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerenciar bucket: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro inesperado ao gerenciar bucket: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerenciar bucket: " + e.getMessage());
        }
    }
}