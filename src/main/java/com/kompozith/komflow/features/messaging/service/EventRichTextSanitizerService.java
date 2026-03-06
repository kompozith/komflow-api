package com.kompozith.komflow.features.messaging.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class EventRichTextSanitizerService {

    private static final Safelist EVENT_SAFE_LIST = Safelist.none()
            .addTags("div", "p", "br", "strong", "b", "em", "i", "u", "s", "ul", "ol", "li", "blockquote", "a", "span")
            .addAttributes("a", "href", "target", "rel")
            .addProtocols("a", "href", "http", "https", "mailto");

    public String sanitizeHtml(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        Document.OutputSettings outputSettings = new Document.OutputSettings();
        outputSettings.prettyPrint(false);
        String cleaned = Jsoup.clean(content, "", EVENT_SAFE_LIST, outputSettings);
        return cleaned.trim();
    }
}
