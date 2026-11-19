package com.daroring.fromdarori.domain.story.service;

import com.daroring.fromdarori.domain.story.dto.StoryImageRequest;
import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.entity.StoryImage;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.story.repository.AdminStoryImageRepository;
import com.daroring.fromdarori.domain.story.repository.AdminStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminStoryImageService {
    private final AdminStoryImageRepository adminStoryImageRepository;
    private final AdminStoryRepository adminStoryRepository;

    @Transactional
    public List<StoryImage> addStoryImages(UUID storyId, List<String> savedFilenames) {
        Story story = adminStoryRepository.findById(storyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "STORY4041", "소식을 찾을 수 없습니다."));
        List<StoryImage> images = new java.util.ArrayList<>(savedFilenames.size());
        for (int index = 0; index < savedFilenames.size(); index++) {
            // request.images의 순서를 DB display_order(1부터 시작)로 사용합니다.
            images.add(StoryImage.create(story, savedFilenames.get(index), index + 1));
        }

        adminStoryImageRepository.saveAll(images);
        return images;
    }

}
