package com.daroring.fromdarori.domain.story.service;

import com.daroring.fromdarori.domain.story.dto.StoryImagePostResponse;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoryImageStorage implements StoryImageReader {

    private static final String IMAGE_PATH = "/api/v1/admin/story-images/";
    private static final Pattern FILE_KEY = Pattern.compile(
            "[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.[a-zA-Z0-9]+");
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png",
            "gif", "image/gif", "webp", "image/webp", "avif", "image/avif", "bmp", "image/bmp");

    private final LocalStoryImageStorage local;
    private final Optional<S3StoryImageStorage> s3;

    public String url(String key) {
        return IMAGE_PATH + key;
    }

    public StoryImagePostResponse save(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String extension = filename == null ? "" : extensionOf(filename);
        String contentType = CONTENT_TYPES.get(extension);
        if (file.isEmpty() || contentType == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4001", "지원하는 형식의 사진을 선택해 주세요.");
        }

        String key = UUID.randomUUID() + "." + extension;
        try {
            if (s3.isPresent()) {
                s3.get().write(key, contentType, file);
            } else {
                local.write(key, file);
            }
        } catch (IOException exception) {
            log.error("Could not store story image {}", key, exception);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE5001", "사진을 저장하지 못했습니다.");
        }
        return new StoryImagePostResponse(key, url(key), filename, contentType, file.getSize());
    }

    @Override
    public Optional<Image> readImage(String key) {
        if (key == null || !FILE_KEY.matcher(key).matches()) {
            return Optional.empty();
        }
        String contentType = CONTENT_TYPES.get(extensionOf(key));
        if (contentType == null) {
            return Optional.empty();
        }
        return storedImage(key).map(resource -> new Image(resource, contentType));
    }

    public void requireUploaded(String key) {
        if (readImage(key).isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4002", "업로드한 사진을 찾을 수 없습니다.");
        }
    }

    private Optional<Resource> storedImage(String key) {
        return local.read(key).or(() -> s3.flatMap(storage -> storage.read(key)));
    }

    private static String extensionOf(String name) {
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
