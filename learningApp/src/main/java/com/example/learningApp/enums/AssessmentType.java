package com.example.learningApp.enums;

import lombok.Getter;

@Getter
public enum AssessmentType {

    // ===== VOCABULARY =====
    KANJI_READING("Đọc chữ Hán", SkillCategory.KANJI),
    KANJI_MEMORY("Nhớ chữ Hán", SkillCategory.KANJI),
    VOCAB_CONTEXT("Điền từ phù hợp vào ____★____", SkillCategory.VOCABULARY),

    // ===== GRAMMAR =====
    FILL_BLANK("Điền chỗ trống (　)", SkillCategory.GRAMMAR),
    SENTENCE_ORDER("Sắp xếp/điền số trong đoạn văn", SkillCategory.GRAMMAR),
    PARAPHRASE("Tìm câu có cách diễn đạt giống", SkillCategory.GRAMMAR),
    GRAMMAR_SELECT("Chọn ngữ pháp phù hợp với câu", SkillCategory.GRAMMAR),
    TEXT_COMPLETION("Hoàn thành đoạn văn", SkillCategory.GRAMMAR),

    // ===== READING =====
    READING_SHORT("Đọc hiểu đoạn văn ngắn", SkillCategory.READING),
    READING_DIALOGUE("Đọc hiểu hội thoại ngắn", SkillCategory.READING),
    READING_LETTER("Đọc thư/tin nhắn", SkillCategory.READING),
    READING_PERSONAL("Đọc hiểu mô tả gia đình/cá nhân", SkillCategory.READING),
    READING_PLACE("Đọc hiểu mô tả môi trường/địa điểm", SkillCategory.READING),
    READING_INFO("Đọc hiểu thông báo/lịch trình", SkillCategory.READING),
    READING_MEDIUM("Trung văn", SkillCategory.READING),

    // ===== LISTENING =====
    LISTENING_TASK("Hiểu yêu cầu (課題理解)", SkillCategory.LISTENING),
    LISTENING_CHOICE_PREVIEW("Hiểu điểm quan trọng (ポイント理解)", SkillCategory.LISTENING),
    LISTENING_MAIN_IDEA("Hiểu ý chính (概要理解)", SkillCategory.LISTENING),
    LISTENING_RESPONSE("Chọn phản hồi phù hợp (応答理解)", SkillCategory.LISTENING),
    LISTENING_INSTANT("Nghe và trả lời ngay (即時理解)", SkillCategory.LISTENING),
    LISTENING_LONG("Nghe hiểu tổng hợp (総合理解)", SkillCategory.LISTENING);

    private final String displayName;
    private final SkillCategory category;

    AssessmentType(String displayName, SkillCategory category) {
        this.displayName = displayName;
        this.category = category;
    }
}