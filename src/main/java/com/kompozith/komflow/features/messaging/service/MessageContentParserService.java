package com.kompozith.komflow.features.messaging.service;

import com.kompozith.komflow.features.messaging.entity.MessageChannel;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class MessageContentParserService {

    private static final Safelist EMAIL_SAFE_LIST = Safelist.none()
            .addTags("div", "p", "br", "strong", "b", "em", "i", "u", "s", "ul", "ol", "li", "blockquote", "a", "span")
            .addAttributes("a", "href", "target", "rel")
            .addProtocols("a", "href", "http", "https", "mailto");

    public String normalizeForStorage(String content, MessageChannel channel) {
        if (content == null) {
            return "";
        }
        if (channel == null) {
            return content.trim();
        }
        if (channel == MessageChannel.EMAIL) {
            return sanitizeEmailHtml(content);
        }
        return toPlainText(content);
    }

    public String renderForChannel(String content, MessageChannel channel) {
        if (content == null) {
            return "";
        }
        if (channel == MessageChannel.EMAIL) {
            return sanitizeEmailHtml(content);
        }
        return toPlainText(content);
    }

    public String toPlainText(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String withLineBreakHints = content
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)</div>", "\n")
                .replaceAll("(?i)</li>", "\n");

        String text = Jsoup.parse(withLineBreakHints).body().wholeText();
        return text.replace('\u00A0', ' ')
                .replaceAll("[\\t\\x0B\\f\\r]+", " ")
                .replaceAll(" +\n", "\n")
                .replaceAll("\n +", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private String sanitizeEmailHtml(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        Document.OutputSettings outputSettings = new Document.OutputSettings();
        outputSettings.prettyPrint(false);
        String cleaned = Jsoup.clean(content, "", EMAIL_SAFE_LIST, outputSettings);
        return cleaned.trim();
    }
}
