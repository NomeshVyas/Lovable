package com.nomesh.projects.lovable_clone.config;

import com.nomesh.projects.lovable_clone.exception.StorageException;
import com.nomesh.projects.lovable_clone.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MinioBucketInitializer implements ApplicationRunner {

    MinioClient minioClient;
    MinioProperties minioProperties;

    @Override
    public void run(ApplicationArguments args) {
        String bucket = minioProperties.projectBucket();
        boolean bucketExists = isMinioBucketExist(bucket);

        if (bucketExists) {
            log.info("MinIO bucket '{}' already exists", bucket);
            return;
        }

        makeMinioBucket(bucket);
    }

    private boolean isMinioBucketExist(String bucket) {
        try {
            return minioClient.bucketExists(
                    BucketExistsArgs
                            .builder()
                            .bucket(bucket)
                            .build()
            );
        } catch (MinioException exception) {
            throw new StorageException("Failed to check MinIO bucket's existence: " + bucket, exception);
        }
    }

    private void makeMinioBucket(String bucket) {
        try {
            minioClient.makeBucket(MakeBucketArgs
                    .builder()
                    .bucket(bucket)
                    .build()
            );
            log.info("Created MinIO bucket '{}'", bucket);
        } catch (MinioException exception) {
            throw new StorageException("Failed to make MinIO bucket: " + bucket, exception);
        }
    }
}
