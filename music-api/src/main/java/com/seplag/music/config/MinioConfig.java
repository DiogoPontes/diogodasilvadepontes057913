package com.seplag.music.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.url}") // Mapeia para minio.url
    private String url;

    @Value("${minio.accessKey}") // Mapeia para minio.accessKey
    private String accessKey;

    @Value("${minio.secretKey}") // Mapeia para minio.secretKey
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }
}