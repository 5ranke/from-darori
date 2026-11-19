package com.daroring.fromdarori.domain.story.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoryUpdateRequest {
    private UUID categoryId;

    @Size(max = 80)
    private String title;

    @Size(max = 3000)
    private String body;

    private String actionUrl;
    private LocalDate applicationDeadline;
    private List<@NotNull @Valid StoryImageUpdateRequest> images;

    @Setter(lombok.AccessLevel.NONE)
    private boolean actionUrlPresent;

    @Setter(lombok.AccessLevel.NONE)
    private boolean applicationDeadlinePresent;

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
        actionUrlPresent = true;
    }

    public void setApplicationDeadline(LocalDate applicationDeadline) {
        this.applicationDeadline = applicationDeadline;
        applicationDeadlinePresent = true;
    }
}
