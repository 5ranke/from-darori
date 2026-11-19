package com.daroring.fromdarori.domain.story.repository;

import com.daroring.fromdarori.domain.story.entity.StoryImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminStoryImageRepository extends JpaRepository<StoryImage, UUID> {
    List<StoryImage> findByStory_IdOrderByDisplayOrderAsc(UUID storyId);
    void deleteByStory_Id(UUID storyId);
}
