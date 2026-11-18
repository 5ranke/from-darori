package com.daroring.fromdarori.domain.story.service;

import com.daroring.fromdarori.domain.admin.repository.AdminUserRepository;
import com.daroring.fromdarori.domain.category.repository.CategoryRepository;
import com.daroring.fromdarori.domain.story.dto.*;
import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.entity.StoryImage;
import com.daroring.fromdarori.domain.story.entity.StoryPublicationStatus;
import com.daroring.fromdarori.domain.story.entity.StorySourceType;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.story.repository.AdminStoryImageRepository;
import com.daroring.fromdarori.domain.story.repository.AdminStoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStoryService {
    private final AdminStoryRepository adminStoryRepository;
    private final AdminStoryImageRepository adminStoryImageRepository;
    private final CategoryRepository categoryRepository;
    private final AdminUserRepository adminUserRepository;

    @Transactional(readOnly = true)
    public StoryInfoResponse getStories(
            int page,
            int size,
            UUID categoryId,
            StorySourceType sourceType,
            StoryPublicationStatus publicationStatus
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Story> storyPage;
        if (publicationStatus == StoryPublicationStatus.PUBLISHED) {
            storyPage = adminStoryRepository.findPublishedAdminStories(categoryId, sourceType, pageable);
        } else if (publicationStatus == StoryPublicationStatus.UNPUBLISHED) {
            storyPage = adminStoryRepository.findUnpublishedAdminStories(categoryId, sourceType, pageable);
        } else {
            storyPage = adminStoryRepository.findAdminStories(categoryId, sourceType, pageable);
        }

        Set<UUID> publishedStoryIds = storyPage.isEmpty()
                ? Set.of()
                : adminStoryRepository.findPublishedStoryIds(
                        storyPage.getContent().stream()
                                .map(Story::getId)
                                .toList()
                );

        return new StoryInfoResponse(
                Math.toIntExact(storyPage.getTotalElements()),
                storyPage.getNumber(),
                storyPage.getSize(),
                storyPage.getTotalPages(),
                storyPage.hasNext(),
                storyPage.getContent().stream()
                        .map(story -> StoryItemResponse.from(story, publishedStoryIds.contains(story.getId())))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public StoryIdInfoResponse getStory(UUID storyId) {
        Story story = adminStoryRepository.findById(storyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "STORY4041", "소식을 찾을 수 없습니다.") );

        List<StoryIdImageResponse> images = adminStoryImageRepository
                .findByStory_IdOrderByDisplayOrderAsc(storyId)
                .stream()
                .map(image -> new StoryIdImageResponse(
                        image.getId(),
                        image.getObjectKey(),
                        image.getDisplayOrder()))
                .toList();
        return StoryIdInfoResponse.from(story,images);
    }

    @Transactional
    public StoryCreateResponse create(StoryCreateRequest request, UUID adminId) {
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,"CATEGORY4041","카테고리를 찾을 수 없습니다."));
        var admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ADMIN4041","운영자를 찾을 수 없습니다."));

        Story story = adminStoryRepository.save(Story.create(
                category, admin, request.title(), request.body(),
                request.actionUrl(), request.applicationDeadline()));

        List<StoryImage> savedImages = request.images() == null
                ? List.of()
                : adminStoryImageRepository.saveAll(request.images().stream()
                    .map(image -> StoryImage.create(
                            story,
                            image.objectKey(),
                            image.displayOrder()
                    ))
                    .toList()
                );
        List<StoryImageResponse> imageResponses = savedImages.stream()
                .map(image -> new StoryImageResponse(
                        image.getId(),
                        image.getDisplayOrder()
                ))
                .toList();
        return StoryCreateResponse.from(story, imageResponses);
    }
}
