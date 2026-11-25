package com.daroring.fromdarori.domain.newsletter.mail;

import com.daroring.fromdarori.domain.category.repository.CategoryRepository;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterMailContent.InlineImage;
import com.daroring.fromdarori.domain.newsletter.mail.NewsletterMailDraft.Story;
import com.daroring.fromdarori.domain.newsletter.service.NewsletterCategoryOrder;
import com.daroring.fromdarori.domain.story.service.LocalStoryImageStorage;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class NewsletterEmailRenderer {

    private static final Pattern TOKEN = Pattern.compile("\\{\\{([A-Z_]+)}}");
    private static final String LINK_STYLE = "color:#90402d;text-decoration:underline;";
    private static final String TABLE = "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"";
    private final LocalStoryImageStorage storage;
    private final CategoryRepository categoryRepository;
    private final URI site;
    private final String template;
    private final byte[] logo;

    public NewsletterEmailRenderer(LocalStoryImageStorage storage, CategoryRepository categoryRepository,
                                   @Value("${darori.newsletter.site-url:https://from-darori.com/}") String siteUrl)
            throws IOException {
        this.storage = storage;
        this.categoryRepository = categoryRepository;
        this.site = Objects.requireNonNull(webUri(siteUrl), "Invalid newsletter site URL");
        template = new ClassPathResource("templates/email/newsletter.html").getContentAsString(StandardCharsets.UTF_8);
        logo = new ClassPathResource("templates/email/darori-envelope.png").getContentAsByteArray();
    }

    public String renderPreview(NewsletterMailDraft draft) {
        return render(draft, DeliveryLinks.none(), true).htmlBody();
    }

    public NewsletterMailContent render(NewsletterMailDraft draft, DeliveryLinks links) {
        return render(draft, links, false);
    }

    private NewsletterMailContent render(NewsletterMailDraft draft, DeliveryLinks links, boolean preview) {
        Map<String, InlineImage> inlineImages = new LinkedHashMap<>();
        String logoSrc = inlineSource(new InlineImage("darori-logo", "image/png", logo.clone()), preview, inlineImages);
        var text = new StringBuilder();
        appendText(text, "From. 다로리", draft.issue(), draft.title(), draft.intro(), draft.summary());

        Map<String, List<Story>> sections = new LinkedHashMap<>();
        if (draft.stories() != null && !draft.stories().isEmpty()) {
            var categoryOrder = NewsletterCategoryOrder.byCategory(categoryRepository.findAllByOrderByDisplayOrderAsc(),
                    Story::categoryId, Story::categoryName);
            draft.stories().stream().filter(Objects::nonNull)
                    .sorted(categoryOrder.thenComparingInt(Story::displayOrder))
                    .forEach(story -> sections.computeIfAbsent(value(story.categoryName()), key -> new ArrayList<>()).add(story));
        }
        var stories = new StringBuilder();
        var contents = new ArrayList<String>();
        for (var section : sections.entrySet()) {
            CategoryLabels labels = categoryLabels(section.getKey());
            if (present(labels.heading())) {
                stories.append("<h2 style=\"margin:30px 0 0;padding-bottom:10px;border-bottom:1px solid #ededed;font-size:17px;\">")
                        .append(escape(labels.heading())).append("</h2>");
                appendText(text, labels.heading());
            }
            for (Story story : section.getValue()) {
                if (present(story.title())) {
                    contents.add(present(labels.contents()) ? labels.contents() + " · " + story.title() : story.title());
                }
                stories.append(storyHtml(story, preview, inlineImages, text));
            }
        }

        String quiz = quizHtml(draft.quiz(), links, text);
        appendText(text, draft.closing(), "다로리 마을에서 지역으로, 다같이 다로리",
                "마을의 일상과 함께하는 사람들의 이야기를 전합니다.");
        String teamLinks = renderLinks(text,
                new Link("마을 소개", site.resolve("/visitor/village.html").toString()),
                new Link("인스타그램", "https://www.instagram.com/darori_in/"),
                new Link("문의하기", "mailto:daroriin@naver.com"));
        String footerLinks = renderLinks(text,
                new Link("지난 편지 보기", site.resolve("/visitor/letters.html").toString()),
                new Link("구독 설정", webUrl(links.preferencesUrl())),
                new Link("구독 해지", webUrl(links.unsubscribeUrl(), site.resolve("/visitor/unsubscribe.html").toString())));

        String summary = contentsHtml(contents, draft.summary());
        Map<String, String> tokens = Map.ofEntries(
                Map.entry("TITLE", escape(draft.title())), Map.entry("HEADING", withBreaks(draft.title())),
                Map.entry("ISSUE", escape(present(draft.issue()) ? draft.issue() : preview ? "발행호 미정" : "")),
                Map.entry("LOGO", logoSrc), Map.entry("INTRO", paragraph(draft.intro(), "margin:10px 0;")),
                Map.entry("SUMMARY", summary), Map.entry("STORIES", stories.toString()), Map.entry("QUIZ", quiz),
                Map.entry("CLOSING", paragraph(draft.closing(), "margin:28px 0 0;")),
                Map.entry("TEAM_LINKS", teamLinks), Map.entry("FOOTER_LINKS", footerLinks));
        String html = TOKEN.matcher(template).replaceAll(match ->
                java.util.regex.Matcher.quoteReplacement(tokens.get(match.group(1))));
        return new NewsletterMailContent(value(draft.title()), text.toString().strip(), html,
                List.copyOf(inlineImages.values()));
    }

    private static CategoryLabels categoryLabels(String name) {
        return switch (name) {
            case "다로리의 요즘" -> new CategoryLabels("🏡 마을 소식", "다로리의 요즘");
            case "프로젝트 후속" -> new CategoryLabels("🌱 프로젝트 후속", "🌱 그 프로젝트, 지금은?");
            case "구인/참여" -> new CategoryLabels("🙋 모집", "🙋 다로리에서 찾습니다");
            case "제철 정보" -> new CategoryLabels("🍊 특산물", "🍊 다로리의 제철");
            default -> new CategoryLabels(name, name);
        };
    }

    private static String contentsHtml(List<String> items, String fallback) {
        if (items.isEmpty() && !present(fallback)) return "";
        var html = new StringBuilder(TABLE).append(" style=\"margin:24px 0;\"><tr><td style=\"padding:18px 20px;"
                + "background:#f5f5f5;border-radius:12px;\"><strong>이번 편지에 담았어요</strong>");
        if (items.isEmpty()) {
            html.append(paragraph(fallback, "margin:8px 0 0;font-size:13px;"));
        } else {
            for (String item : items) html.append(paragraph(item, "margin:4px 0 0;font-size:13px;"));
        }
        return html.append("</td></tr></table>").toString();
    }

    private record CategoryLabels(String contents, String heading) {
    }

    private String storyHtml(Story story, boolean preview, Map<String, InlineImage> inlineImages, StringBuilder text) {
        appendText(text, story.title(), story.body());
        StringBuilder html = new StringBuilder("<h3 style=\"margin:16px 0 10px;font-size:16px;\">")
                .append(escape(story.title())).append("</h3>")
                .append(paragraph(story.body(), "margin:0 0 12px;font-size:14px;"))
                .append(gallery(story.images(), preview, inlineImages));
        if (present(story.sourceName()) || webUri(story.sourceUrl()) != null) {
            String source = present(story.sourceName()) ? story.sourceName() : "원문 보기";
            String url = webUrl(story.sourceUrl());
            appendText(text, "출처 · " + source + (url == null ? "" : " (" + url + ")"));
            html.append("<p style=\"margin:6px 0;font-size:13px;color:#62626c;\">출처 · ")
                    .append(url == null ? escape(source) : anchor(source, url)).append("</p>");
        }
        if (story.applicationDeadline() != null) {
            String deadline = "모집 마감 · " + story.applicationDeadline();
            appendText(text, deadline);
            html.append(paragraph(deadline, "margin:6px 0;font-size:13px;color:#62626c;"));
        }
        if (present(story.purchaseInfo())) {
            appendText(text, "구매 안내 · " + story.purchaseInfo());
            html.append(paragraph("구매 안내 · " + story.purchaseInfo(), "margin:6px 0;font-size:13px;color:#62626c;"));
        }
        String action = webUrl(story.actionUrl());
        if (action != null) {
            String label = switch (value(story.categoryName())) {
                case "프로젝트 후속" -> "프로젝트 이야기 보기";
                case "구인/참여" -> "신청·문의 안내";
                case "제철 정보" -> "구매 안내";
                default -> "관련 링크 보기";
            };
            appendText(text, label + " · " + action);
            html.append("<p style=\"margin:6px 0;font-size:13px;\">").append(anchor(label, action)).append("</p>");
        }
        return html.toString();
    }

    private String gallery(List<NewsletterMailDraft.Image> images, boolean preview, Map<String, InlineImage> inlineImages) {
        if (images == null || images.isEmpty()) return "";
        var tags = new ArrayList<String>();
        for (var image : images.stream().filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(NewsletterMailDraft.Image::displayOrder)).toList()) {
            String src = imageSource(image, preview, inlineImages);
            if (src != null) {
                tags.add("<img src=\"" + escape(src) + "\" alt=\"" + escape(image.altText())
                        + "\" width=\"100%\" style=\"display:block;width:100%;max-width:100%;height:auto;border:0;border-radius:8px;\">");
            }
        }
        if (tags.isEmpty()) return "";
        StringBuilder html = new StringBuilder(TABLE).append(" style=\"margin:8px 0 16px;table-layout:fixed;\">");
        int columns = Math.min(3, tags.size());
        for (int offset = 0; offset < tags.size(); offset += columns) {
            html.append("<tr>");
            for (int col = 0; col < columns; col++) {
                int index = offset + col;
                html.append("<td width=\"").append(100 / columns).append("%\" valign=\"top\" style=\"padding:4px;\">");
                if (index < tags.size()) html.append(tags.get(index));
                html.append("</td>");
            }
            html.append("</tr>");
        }
        return html.append("</table>").toString();
    }

    private String imageSource(NewsletterMailDraft.Image image, boolean preview, Map<String, InlineImage> inlineImages) {
        var path = storage.find(image.objectKey());
        if (path != null) {
            try {
                String cid = "darori-photo-" + image.objectKey();
                InlineImage asset = inlineImages.get(cid);
                if (asset == null) asset = new InlineImage(cid, storage.contentType(image.objectKey()), Files.readAllBytes(path));
                return inlineSource(asset, preview, inlineImages);
            } catch (IOException ignored) {
                return webUrl(image.imageUrl());
            }
        }
        return webUrl(image.imageUrl());
    }

    private static String inlineSource(InlineImage asset, boolean preview, Map<String, InlineImage> images) {
        images.putIfAbsent(asset.contentId(), asset);
        return preview ? "data:" + asset.contentType() + ";base64," + Base64.getEncoder().encodeToString(asset.bytes())
                : "cid:" + asset.contentId();
    }

    private static String quizHtml(NewsletterMailDraft.Quiz quiz, DeliveryLinks links, StringBuilder text) {
        if (quiz == null || !present(quiz.question()) || quiz.options() == null
                || quiz.options().size() != 2 || !quiz.options().stream().allMatch(NewsletterEmailRenderer::present)) return "";
        appendText(text, "다로리 퀴즈", quiz.question());
        StringBuilder html = new StringBuilder(TABLE).append(" style=\"margin-top:28px;\"><tr><td style=\"padding:24px;"
                + "background:#f7f7f8;border-radius:12px;\"><h2 style=\"margin:0;font-size:20px;\">🎁 다로리 퀴즈</h2>")
                .append(paragraph(quiz.question(), "margin:10px 0;"));
        List<String> urls = links.quizOptionUrls();
        for (int index = 0; index < 2; index++) {
            String label = (index + 1) + ". " + quiz.options().get(index);
            String url = urls.size() == 2 ? webUrl(urls.get(index)) : null;
            appendText(text, label + (url == null ? "" : " · " + url));
            html.append(TABLE).append(" style=\"margin-top:8px;\"><tr><td style=\"padding:10px 14px;background:#ffffff;"
                    + "border:1px solid #dedee2;\">")
                    .append(url == null ? escape(label) : anchor(label, url)).append("</td></tr></table>");
        }
        return html.append("</td></tr></table>").toString();
    }

    private static String renderLinks(StringBuilder text, Link... links) {
        var anchors = new ArrayList<String>();
        for (Link link : links) {
            if (link.url() == null) continue;
            appendText(text, link.label() + " · " + link.url());
            anchors.add(anchor(link.label(), link.url()));
        }
        return String.join(" &middot; ", anchors);
    }

    private static String anchor(String label, String url) {
        return "<a target=\"_blank\" rel=\"noopener noreferrer\" style=\"" + LINK_STYLE + "\" href=\""
                + escape(url) + "\">" + escape(label) + "</a>";
    }

    private static String paragraph(String content, String style) {
        return present(content) ? "<p style=\"" + style + "\">" + withBreaks(content) + "</p>" : "";
    }

    private static String withBreaks(String content) {
        return escape(content).replace("\r\n", "\n").replace('\r', '\n').replace("\n", "<br>");
    }

    private static String escape(String text) {
        return HtmlUtils.htmlEscape(value(text), StandardCharsets.UTF_8.name());
    }

    private static String value(String text) { return Objects.requireNonNullElse(text, ""); }

    private static boolean present(String text) { return text != null && !text.isBlank(); }

    private static void appendText(StringBuilder text, String... lines) {
        for (String line : lines) {
            if (present(line)) text.append(line).append("\n\n");
        }
    }

    private static String webUrl(String raw) {
        URI result = webUri(raw);
        return result == null ? null : result.toASCIIString();
    }

    private static String webUrl(String raw, String fallback) {
        return Objects.requireNonNullElse(webUrl(raw), fallback);
    }

    private static URI webUri(String raw) {
        if (!present(raw)) return null;
        try {
            URI uri = URI.create(raw.strip());
            String scheme = uri.getScheme();
            if (("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
                    && uri.getHost() != null && uri.getUserInfo() == null) return uri;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        return null;
    }

    private record Link(String label, String url) {
    }

    // Quiz URLs open a confirmation page. Recording an answer belongs to its separate explicit submission action.
    public record DeliveryLinks(String preferencesUrl, String unsubscribeUrl, List<String> quizOptionUrls) {
        public DeliveryLinks {
            quizOptionUrls = quizOptionUrls == null ? List.of() : List.copyOf(quizOptionUrls);
        }

        public static DeliveryLinks none() {
            return new DeliveryLinks(null, null, List.of());
        }
    }
}
