package com.iwms.iwms.infrastructure.storage.r2;

import java.net.URL;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.iwms.iwms.application.ports.AttachmentStoragePort;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class R2StorageAdapter implements AttachmentStoragePort {

    private final S3Client s3Client;
    private final String bucket;
    private final String endpoint;
    private final String region;

    public R2StorageAdapter(S3Client s3Client,
                            @Value("${app.storage.r2.bucket}") String bucket,
                            @Value("${app.storage.r2.endpoint}") String endpoint,
                            @Value("${app.storage.r2.region:auto}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.endpoint = endpoint;
        this.region = region;
    }

    @Override
    public URL createPresignedUploadUrl(String objectKey, String contentType, long ttlSeconds) {
        S3Presigner presigner = S3Presigner.builder()
            .region(Region.of(region))
            .endpointOverride(java.net.URI.create(endpoint))
            .build();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            .contentType(contentType)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(ttlSeconds))
            .putObjectRequest(putObjectRequest)
            .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        return presignedRequest.url();
    }
}


