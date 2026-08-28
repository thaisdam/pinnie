package com.pinnie.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3ImageStorageServiceImpl implements ImageStorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3ImageStorageServiceImpl.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3ImageStorageServiceImpl(
            @Value("${app.storage.s3.bucket}") String bucketName,
            @Value("${app.storage.s3.region}") String region,
            @Value("${app.storage.s3.access-key}") String accessKey,
            @Value("${app.storage.s3.secret-key}") String secretKey) {
        
        this.bucketName = bucketName;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Override
    public String store(byte[] content, String extension) throws IOException {
        String filename = UUID.randomUUID().toString() + extension;
        
        String contentType = "application/octet-stream";
        if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg")) contentType = "image/jpeg";
        else if (extension.equalsIgnoreCase(".png")) contentType = "image/png";
        else if (extension.equalsIgnoreCase(".webp")) contentType = "image/webp";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
            return filename;
        } catch (Exception e) {
            throw new IOException("Failed to store file in S3", e);
        }
    }

    @Override
    public void delete(String storedFilename) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storedFilename)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            logger.error("Failed to delete orphaned file from S3: {}", storedFilename, e);
        }
    }
}
