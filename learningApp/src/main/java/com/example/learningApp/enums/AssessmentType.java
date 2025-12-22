package com.example.learningApp.enums;

import lombok.Getter;

@Getter
public enum AssessmentType {

    // ===== LANGUAGE KNOWLEDGE =====
    KANJI_READING("Đọc chữ Hán"),
    KANJI_MEMORY("Nhớ chữ Hán"),

    VOCAB_CONTEXT("Chọn từ phù hợp với câu"),
    PARAPHRASE("Tìm câu có cách diễn đạt giống"),

    GRAMMAR_SELECT("Chọn ngữ pháp phù hợp với câu"),
    SENTENCE_ORDER("Sắp xếp câu"),
    TEXT_COMPLETION("Hoàn thành đoạn văn"),

    READING_SHORT("Đoạn văn ngắn"),
    READING_MEDIUM("Trung văn"),
    READING_INFO("Tìm kiếm thông tin"),

    // ===== LISTENING =====
    LISTENING_TASK("Hiểu đề bài"),
    LISTENING_MAIN_IDEA("Hiểu điểm chính"),
    LISTENING_RESPONSE("Diễn đạt bằng lời nói"),
    LISTENING_INSTANT("Phản hồi tức thời");

    private final String displayName;

    AssessmentType(String displayName) {
        this.displayName = displayName;
    }
}
