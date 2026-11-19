package com.daroring.fromdarori.domain.story.controller;

import com.daroring.fromdarori.domain.story.dto.StoryImagePostResponse;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.story.service.AdminStoryImageService;
import com.daroring.fromdarori.global.response.ApiResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/story-images")
public class AdminStoryImageController {
    // 소식 사진 업로드
    // POST /api/v1/admin/story-images
    @PostMapping
    public ResponseEntity<ApiResponse<List<StoryImagePostResponse>>> upload(
            @RequestParam("filename") List<MultipartFile> files) {
        List<StoryImagePostResponse> results = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4001", "이미지가 없습니다.");
                }

                String originalFilename = file.getOriginalFilename();
                String contentType = file.getContentType();
                long fileSize = file.getSize();

                Path uploadPath = Paths.get("upload").toAbsolutePath().normalize();
                Files.createDirectories(uploadPath);

                String extension = "";
                if (originalFilename != null) {
                    int dotIndex = originalFilename.lastIndexOf('.');
                    if (dotIndex >= 0) {
                        extension = originalFilename.substring(dotIndex);
                    }
                }

                String savedFilename = UUID.randomUUID() + extension;
                file.transferTo(uploadPath.resolve(savedFilename));

                results.add(new StoryImagePostResponse(
                        savedFilename,
                        "/upload/" + savedFilename,
                        originalFilename,
                        contentType,
                        fileSize
                ));
            }
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4002", exception.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.success("사진이 업로드되었습니다.", results));
    }
}
