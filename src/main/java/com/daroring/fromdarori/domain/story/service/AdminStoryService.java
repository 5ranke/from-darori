package com.daroring.fromdarori.domain.story.service;

import com.daroring.fromdarori.domain.admin.repository.AdminUserRepository;
import com.daroring.fromdarori.domain.category.repository.CategoryRepository;
import com.daroring.fromdarori.domain.story.dto.*;
import com.daroring.fromdarori.domain.newsletter.entity.NewsletterStatus;
import com.daroring.fromdarori.domain.story.entity.Story;
import com.daroring.fromdarori.domain.story.entity.StoryImage;
import com.daroring.fromdarori.domain.story.entity.StoryPublicationStatus;
import com.daroring.fromdarori.domain.story.entity.StorySourceType;
import com.daroring.fromdarori.domain.story.exception.ApiException;
import com.daroring.fromdarori.domain.story.repository.AdminStoryImageRepository;
import com.daroring.fromdarori.domain.story.repository.AdminStoryRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final LocalStoryImageStorage imageStorage;

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
        } else if (publicationStatus == StoryPublicationStatus.UNPUBLISHED
                || publicationStatus == StoryPublicationStatus.AVAILABLE) {
            var excludedStatuses = publicationStatus == StoryPublicationStatus.AVAILABLE
                    ? NewsletterStatus.HOLDING_STORIES : Set.of(NewsletterStatus.PUBLISHED);
            storyPage = adminStoryRepository.findAdminStoriesExcludingNewsletterStatuses(
                    categoryId, sourceType, excludedStatuses, pageable);
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
                        imageStorage.url(image.getObjectKey()),
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
                : saveNewImages(story, request.images());
        return StoryCreateResponse.from(story, imageResponses(savedImages));
    }

    @Transactional
    public StoryUpdateResponse update(UUID storyId, StoryUpdateRequest request) {
        if (request.getCategoryId() == null
                && request.getTitle() == null
                && request.getBody() == null
                && !request.isActionUrlPresent()
                && !request.isApplicationDeadlinePresent()
                && request.getImages() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "STORY4002", "수정할 내용이 없습니다.");
        }

        Story story = adminStoryRepository.findByIdForUpdate(storyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "STORY4041", "소식을 찾을 수 없습니다."));

        var category = request.getCategoryId() == null ? null
                : categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                "CATEGORY4041", "카테고리를 찾을 수 없습니다."));

        story.update(category, request.getTitle(), request.getBody(), request.getActionUrl(),
                request.isActionUrlPresent(), request.getApplicationDeadline(), request.isApplicationDeadlinePresent());

        var existingImages = adminStoryImageRepository.findByStory_IdOrderByDisplayOrderAsc(storyId);
        var images = request.getImages() == null ? existingImages : updateImages(story, existingImages, request.getImages());
        adminStoryRepository.flush();
        return StoryUpdateResponse.from(story, imageResponses(images));
    }

    @Transactional
    public StoryDeleteResponse delete(UUID storyId) {
        Story story = adminStoryRepository.findById(storyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "STORY4041", "소식을 찾을 수 없습니다."));
        adminStoryRepository.delete(story);
        return new StoryDeleteResponse(story.getId());
    }

    private List<StoryImage> saveNewImages(Story story, List<StoryImageRequest> requests) {
        Set<Integer> orders = new HashSet<>();
        Set<String> keys = new HashSet<>();
        var images = new ArrayList<StoryImage>(requests.size());
        for (var request : requests) {
            if (!orders.add(request.displayOrder()) || !keys.add(request.objectKey())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4003", "사진이나 표시 순서가 중복됐습니다.");
            }
            imageStorage.requireUploaded(request.objectKey());
            images.add(StoryImage.create(story, request.objectKey(), request.displayOrder()));
        }
        return adminStoryImageRepository.saveAll(images);
    }

    private List<StoryImage> updateImages(Story story, List<StoryImage> existingImages,
                                         List<StoryImageUpdateRequest> requests) {
        var omitted = existingImages.stream().collect(Collectors.toMap(StoryImage::getId, Function.identity()));
        Set<Integer> orders = new HashSet<>();
        Set<String> keys = new HashSet<>();
        var retained = new ArrayList<StoryImage>();
        var newImages = new ArrayList<StoryImageRequest>();

        for (var request : requests) {
            if (!orders.add(request.displayOrder())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4003", "사진 표시 순서가 중복됐습니다.");
            }
            if (request.imageId() == null) {
                newImages.add(new StoryImageRequest(request.objectKey(), request.displayOrder()));
                continue;
            }
            StoryImage image = omitted.remove(request.imageId());
            if (image == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4004", "이 소식의 사진 ID를 한 번만 전달해 주세요.");
            }
            retained.add(image);
            keys.add(image.getObjectKey());
        }

        for (var image : newImages) {
            if (!keys.add(image.objectKey())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE4003", "사진이 중복됐습니다.");
            }
        }

        adminStoryImageRepository.deleteAll(omitted.values());
        adminStoryImageRepository.flush();

        // Move retained images to free positions before applying the requested order.
        Set<Integer> occupied = new HashSet<>(orders);
        retained.forEach(image -> occupied.add(image.getDisplayOrder()));
        int temporaryOrder = 1;
        for (var image : retained) {
            while (!occupied.add(temporaryOrder)) {
                temporaryOrder++;
            }
            image.changeDisplayOrder(temporaryOrder);
        }
        adminStoryImageRepository.flush();

        int index = 0;
        for (var request : requests) {
            if (request.imageId() != null) {
                retained.get(index++).changeDisplayOrder(request.displayOrder());
            }
        }
        adminStoryImageRepository.flush();
        retained.addAll(saveNewImages(story, newImages));
        return retained;
    }

    private List<StoryImageResponse> imageResponses(List<StoryImage> images) {
        return images.stream()
                .sorted(Comparator.comparing(StoryImage::getDisplayOrder))
                .map(image -> new StoryImageResponse(image.getId(), image.getDisplayOrder()))
                .toList();
    }
}
