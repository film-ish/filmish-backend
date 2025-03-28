package com.example.knockknock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    // 기존 메서드는 유지
    public void uploadFile(String bucketName, String key, byte[] content) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(),
            RequestBody.fromBytes(content)
        );
    }

    // Content-Type을 지정할 수 있는 새 메서드 추가
    public void uploadFile(String bucketName, String key, byte[] content, String contentType) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)  // Content-Type 설정
                .build(),
            RequestBody.fromBytes(content)
        );
    }

    public void deleteFile(String bucketName, String key) {
        s3Client.deleteObject(builder -> builder
            .bucket(bucketName)
            .key(key)
            .build());
    }
}