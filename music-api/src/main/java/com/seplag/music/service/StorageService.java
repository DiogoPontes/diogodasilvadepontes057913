package com.seplag.music.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        try {
            // Verifica se o bucket existe, se não, cria
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // Gera um nome único para o arquivo para evitar sobrescrita
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // Faz o upload para o MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("Arquivo enviado com sucesso para o MinIO: {}", fileName);
            return fileName;
        } catch (Exception e) {
            log.error("Erro ao fazer upload para o MinIO", e);
            throw new RuntimeException("Erro ao salvar arquivo no servidor de armazenamento");
        }
    }

    public String getPresignedUrl(String fileName) {
        try {
            // Gera uma URL temporária (1 hora) para visualizar a imagem
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erro ao gerar URL temporária do MinIO", e);
            return null;
        }
    }
}