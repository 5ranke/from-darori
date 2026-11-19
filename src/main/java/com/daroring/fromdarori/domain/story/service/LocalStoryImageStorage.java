package com.daroring.fromdarori.domain.story.service;

import com.daroring.fromdarori.domain.story.dto.StoryImagePostResponse;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalStoryImageStorage {
    private static final String IMAGE_PATH = "/api/v1/admin/story-images/";
    private static final Path UPLOAD_DIR = Path.of("upload").toAbsolutePath();
    private static final Pattern FILE_KEY = Pattern.compile(
            "[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.[a-zA-Z0-9]+");
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png",
            "gif", "image/gif", "webp", "image/webp", "avif", "image/avif", "bmp", "image/bmp");

    public String url(String key) {
        return IMAGE_PATH + key;
    }

    public StoryImagePostResponse save(MultipartFile file) {
        String name = file.getOriginalFilename();
        String extension = name == null ? "" : name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String contentType = CONTENT_TYPES.get(extension);
        if (file.isEmpty() || contentType == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4001", "지원하는 형식의 사진을 선택해 주세요.");
        }

        String key = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(UPLOAD_DIR);
            file.transferTo(UPLOAD_DIR.resolve(key));
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE5001", "사진을 저장하지 못했습니다.");
        }
        return new StoryImagePostResponse(key, url(key), name, contentType, file.getSize());
    }

    public Path find(String key) {
        if (key == null || !FILE_KEY.matcher(key).matches() || contentType(key) == null) {
            return null;
        }
        Path path = UPLOAD_DIR.resolve(key);
        return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) ? path : null;
    }

    public String contentType(String key) {
        return CONTENT_TYPES.get(key.substring(key.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT));
    }

    public void requireUploaded(String key) {
        if (find(key) == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4002", "업로드한 사진을 찾을 수 없습니다.");
        }
    }
}
