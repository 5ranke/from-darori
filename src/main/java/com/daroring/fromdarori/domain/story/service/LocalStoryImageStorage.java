package com.daroring.fromdarori.domain.story.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
class LocalStoryImageStorage {

    private static final Path UPLOAD_DIR = Path.of("upload").toAbsolutePath();

    void write(String key, MultipartFile file) throws IOException {
        Files.createDirectories(UPLOAD_DIR);
        file.transferTo(UPLOAD_DIR.resolve(key));
    }

    Optional<Resource> read(String key) {
        Path path = UPLOAD_DIR.resolve(key);
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            return Optional.empty();
        }
        return Optional.of(new FileSystemResource(path));
    }
}
