package com.jy.constant;

import lombok.Getter;

@Getter
public enum ContentType {
    // 图片
    IMAGE_JPEG("image/jpeg", ".jpg"),
    IMAGE_JPG("image/jpg", ".jpg"),
    IMAGE_PNG("image/png", ".png"),
    IMAGE_GIF("image/gif", ".gif"),
    IMAGE_WEBP("image/webp", ".webp"),
    IMAGE_BMP("image/bmp", ".bmp"),
    IMAGE_TIFF("image/tiff", ".tiff"),
    IMAGE_SVG("image/svg+xml", ".svg"),
    // 文档
    TEXT_PLAIN("text/plain", ".txt"),
    DOC_PDF("application/pdf", ".pdf"),
    DOC_DOC("application/msword", ".doc"),
    DOC_DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"),
    DOC_XLS("application/vnd.ms-excel", ".xls"),
    DOC_XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    DOC_PPT("application/vnd.ms-powerpoint", ".ppt"),
    DOC_PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", ".pptx"),

    // 音频
    AUDIO_MP3("audio/mpeg", ".mp3"),
    AUDIO_WAV("audio/wav", ".wav"),
    AUDIO_OGG("audio/ogg", ".ogg"),
    AUDIO_FLAC("audio/flac", ".flac"),

    // 视频
    VIDEO_MP4("video/mp4", ".mp4"),
    VIDEO_AVI("video/x-msvideo", ".avi"),
    VIDEO_MOV("video/quicktime", ".mov"),
    VIDEO_WEBM("video/webm", ".webm"),
    VIDEO_MKV("video/x-matroska", ".mkv"),
    VIDEO_FLV("video/x-flv", ".flv"),

    // 压缩包
    ZIP("application/zip", ".zip"),
    RAR("application/vnd.rar", ".rar"),
    RAR_OLD("application/x-rar-compressed", ".rar"),
    SEVEN_Z("application/x-7z-compressed", ".7z"),
    TAR("application/x-tar", ".tar"),
    GZIP("application/gzip", ".gz"),

    // 其他
    JSON("application/json", ".json"),
    XML("application/xml", ".xml");

    private final String contentType;
    private final String suffix;

    ContentType(String contentType, String suffix) {
        this.contentType = contentType;
        this.suffix = suffix;
    }
}