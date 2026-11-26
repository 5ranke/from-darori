package com.daroring.fromdarori.domain.story.service;

import com.daroring.fromdarori.domain.story.exception.ApiException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@Profile("story-images-s3")
@Slf4j
class S3StoryImageStorage {

    private static final String PREFIX = "story-images/";

    private final S3Client client;
    private final String bucket;

    S3StoryImageStorage(S3Client client, @Value("${darori.story-images.s3.bucket}") String bucket) {
        Assert.hasText(bucket, "S3 story images require a bucket name");
        this.client = client;
        this.bucket = bucket.strip();
    }

    void write(String key, String contentType, MultipartFile file) throws IOException {
        var request = PutObjectRequest.builder().bucket(bucket).key(PREFIX + key).contentType(contentType).build();
        try {
            client.putObject(request, RequestBody.fromContentProvider(() -> {
                try {
                    return file.getInputStream();
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            }, file.getSize(), contentType));
        } catch (SdkException | UncheckedIOException exception) {
            throw new IOException("S3 upload failed", exception);
        }
    }

    Optional<Resource> read(String key) {
        String objectKey = PREFIX + key;
        try {
            client.headObject(HeadObjectRequest.builder().bucket(bucket).key(objectKey).build());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return Optional.empty();
            }
            throw readFailure(key, exception);
        } catch (SdkException exception) {
            throw readFailure(key, exception);
        }
        var request = GetObjectRequest.builder().bucket(bucket).key(objectKey).build();
        return Optional.of(new InputStreamResource(() -> {
            try {
                return client.getObject(request);
            } catch (SdkException exception) {
                log.error("Could not stream story image {} from S3", key, exception);
                throw new IOException("S3 download failed", exception);
            }
        }));
    }

    private ApiException readFailure(String key, SdkException exception) {
        log.error("Could not inspect story image {} in S3", key, exception);
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "IMAGE5031", "사진 저장소에 연결할 수 없습니다.");
    }
}
