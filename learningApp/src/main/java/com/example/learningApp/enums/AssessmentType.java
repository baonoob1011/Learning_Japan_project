package com.example.learningApp.enums;

import lombok.Getter;

@Getter
public enum AssessmentType {

    // ===== LANGUAGE KNOWLEDGE =====
    //N5
    FILL_BLANK("もんだい1: Điền chỗ trống (　)"),
    VOCAB_CONTEXT("もんだい2: Điền từ phù hợp vào ____★____"),
    SENTENCE_ORDER("もんだい3: Sắp xếp/điền số trong đoạn văn"),
    READING_SHORT("もんだい4: Đọc hiểu đoạn văn ngắn"),
    READING_DIALOGUE("もんだい5: Đọc hiểu hội thoại ngắn"),
    READING_LETTER("もんだい6: Đọc thư/tin nhắn"),
    READING_PERSONAL("もんだい7: Đọc hiểu mô tả gia đình/cá nhân"),
    READING_PLACE("もんだい8: Đọc hiểu mô tả môi trường/địa điểm"),
    READING_INFO("もんだい9: Đọc hiểu thông báo/lịch trình"),
    KANJI_READING("Đọc chữ Hán"),
    KANJI_MEMORY("Nhớ chữ Hán"),
    PARAPHRASE("Tìm câu có cách diễn đạt giống"),
    GRAMMAR_SELECT("Chọn ngữ pháp phù hợp với câu"),
    TEXT_COMPLETION("Hoàn thành đoạn văn"),
    READING_MEDIUM("Trung văn"),

    // ===== LISTENING =====
    LISTENING_TASK("もんだい1: Hiểu yêu cầu (課題理解)"),
    LISTENING_CHOICE_PREVIEW("もんだい2: Hiểu điểm quan trọng (ポイント理解)"),
    LISTENING_MAIN_IDEA("もんだい3: Hiểu ý chính (概要理解)"),
    LISTENING_RESPONSE("もんだい4: Chọn phản hồi phù hợp (応答理解)"),
    LISTENING_INSTANT("もんだいX: Nghe và trả lời ngay (即時理解)"),
    LISTENING_LONG("もんだい5: Nghe hiểu tổng hợp (総合理解)");

    private final String displayName;

    AssessmentType(String displayName) {
        this.displayName = displayName;
    }
}
