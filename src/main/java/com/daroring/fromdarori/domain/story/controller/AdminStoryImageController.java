package com.daroring.fromdarori.domain.story.controller;

import com.daroring.fromdarori.domain.story.dto.StoryImagePostResponse;
import com.daroring.fromdarori.domain.story.service.LocalStoryImageStorage;
import com.daroring.fromdarori.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/story-images")
public class AdminStoryImageController {
    private final LocalStoryImageStorage storage;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StoryImagePostResponse>> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("사진이 업로드되었습니다.", storage.save(file)));
    }

    @GetMapping("/{objectKey}")
    public ResponseEntity<Resource> read(@PathVariable String objectKey) {
        var path = storage.find(objectKey);
        if (path == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(storage.contentType(objectKey)))
                .body(new FileSystemResource(path));
    }
}
