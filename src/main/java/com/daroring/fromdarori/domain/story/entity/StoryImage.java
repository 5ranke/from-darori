package com.daroring.fromdarori.domain.story.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "story_image", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"story_id", "display_order"})
})
public class StoryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "object_key", nullable = false, unique = true, columnDefinition = "text")
    private String objectKey;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public void changeDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public static StoryImage create(Story story, String objectKey, Integer displayOrder){
        StoryImage image = new StoryImage();
        image.story = story;
        image.objectKey = objectKey;
        image.displayOrder = displayOrder;
        return image;
    }
}
