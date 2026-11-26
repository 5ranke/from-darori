package com.daroring.fromdarori.domain.newsletter.mail;

import com.daroring.fromdarori.domain.newsletter.entity.Newsletter;
import com.daroring.fromdarori.domain.story.dto.PublishedStoryContent;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class NewsletterMailDraftFactory {

    private final ObjectMapper mapper;

    public NewsletterMailDraft fromSnapshot(Newsletter newsletter) {
        var snapshot = mapper.readTree(newsletter.getContentSnapshot());
        var stories = PublishedStoryContent.inDisplayOrder(snapshot.path("sections"))
                .flatMap(section -> PublishedStoryContent.inDisplayOrder(section.path("stories"))
                        .map(story -> toStory(story, section.path("title").asString(null))))
                .toList();
        var quiz = mapper.readTree(newsletter.getQuizContent());
        var options = quiz.path("options").valueStream().map(JsonNode::asString).toList();
        return new NewsletterMailDraft(null, newsletter.getTitle(), newsletter.getSummary(),
                snapshot.path("intro").asString(null), stories, snapshot.path("closing").asString(null),
                new NewsletterMailDraft.Quiz(quiz.path("question").asString(null), options));
    }

    private static NewsletterMailDraft.Story toStory(JsonNode story, String sectionTitle) {
        String categoryId = story.path("categoryId").asString(null);
        String deadline = story.path("applicationDeadline").asString(null);
        String title = story.path("title").asString("");
        var images = PublishedStoryContent.inDisplayOrder(story.path("images"))
                .map(image -> new NewsletterMailDraft.Image(image.path("objectKey").asString(null),
                        image.path("imageUrl").asString(null), image.path("altText").asString(title),
                        image.path("displayOrder").asInt(0)))
                .toList();
        return new NewsletterMailDraft.Story(story.path("displayOrder").asInt(0),
                categoryId == null ? null : UUID.fromString(categoryId),
                story.path("categoryName").asString(sectionTitle), title, story.path("body").asString(""),
                story.path("actionUrl").asString(null), deadline == null ? null : LocalDate.parse(deadline),
                story.path("sourceName").asString(null), story.path("sourceUrl").asString(null),
                story.path("purchaseInfo").asString(null), images);
    }
}
