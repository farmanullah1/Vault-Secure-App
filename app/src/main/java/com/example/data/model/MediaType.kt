package com.example.data.model

enum class MediaType {
    PHOTO,
    VIDEO,
    AUDIO,
    DOCUMENT,
    NOTE,
    OTHER;

    companion object {
        fun fromMimeType(mime: String?): MediaType {
            if (mime == null) return OTHER
            val lower = mime.lowercase()
            return when {
                lower.startsWith("image/") -> PHOTO
                lower.startsWith("video/") -> VIDEO
                lower.startsWith("audio/") -> AUDIO
                lower.startsWith("text/") || lower.contains("pdf") || lower.contains("document") || lower.contains("word") || lower.contains("sheet") || lower.contains("presentation") -> DOCUMENT
                else -> OTHER
            }
        }
    }
}
