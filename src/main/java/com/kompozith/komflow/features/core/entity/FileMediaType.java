package com.kompozith.komflow.features.core.entity;

import java.util.Locale;
import java.util.Set;

public enum FileMediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    ARCHIVE,
    OTHER;

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "avi", "mkv", "webm", "m4v");
    private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "wav", "ogg", "aac", "flac", "m4a");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv");
    private static final Set<String> ARCHIVE_EXTENSIONS = Set.of("zip", "rar", "7z", "tar", "gz");

    public static FileMediaType fromFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return OTHER;
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return OTHER;
        }

        String extension = fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);

        if (IMAGE_EXTENSIONS.contains(extension)) {
            return IMAGE;
        }
        if (VIDEO_EXTENSIONS.contains(extension)) {
            return VIDEO;
        }
        if (AUDIO_EXTENSIONS.contains(extension)) {
            return AUDIO;
        }
        if (DOCUMENT_EXTENSIONS.contains(extension)) {
            return DOCUMENT;
        }
        if (ARCHIVE_EXTENSIONS.contains(extension)) {
            return ARCHIVE;
        }
        return OTHER;
    }
}
