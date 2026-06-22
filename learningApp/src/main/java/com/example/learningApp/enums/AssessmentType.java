package com.example.learningApp.enums;

import lombok.Getter;

@Getter
public enum AssessmentType {

    // ===== KANJI (Chữ Hán) =====
    KANJI_READING("Đọc chữ Hán", SkillCategory.KANJI),
    KANJI_MEMORY("Nhớ chữ Hán", SkillCategory.KANJI),
    KANJI_STROKE_ORDER("Thứ tự nét chữ Hán", SkillCategory.KANJI),
    KANJI_MULTIPLE_READING("Đa âm chữ Hán (N2/N3)", SkillCategory.KANJI),

    // ===== VOCABULARY (Từ vựng) =====
    // N4
    VOCAB_CONTEXT("Điền từ phù hợp vào ____★____", SkillCategory.VOCABULARY),
    VOCAB_SYNONYM("Tìm từ có cách diễn đạt giống", SkillCategory.VOCABULARY),
    VOCAB_CHOOSE_BEST("Chọn từ phù hợp với câu", SkillCategory.VOCABULARY),
    VOCAB_FIND_USAGE("Tìm cách dùng đúng của từ", SkillCategory.VOCABULARY),
    // N3
    VOCAB_ANTONYM("Tìm từ trái nghĩa (N3)", SkillCategory.VOCABULARY),
    VOCAB_EXACT_MATCH("Tìm từ có ý nghĩa chính xác (N3)", SkillCategory.VOCABULARY),
    VOCAB_CONTEXT_NUANCE("Tìm từ phù hợp nhất theo ngữ cảnh (N3)", SkillCategory.VOCABULARY),
    VOCAB_COLLOCATION("Tìm từ kết hợp phù hợp (N3)", SkillCategory.VOCABULARY),
    // N2
    VOCAB_FORMAL_CASUAL("Phân biệt từ trang trọng vs. thân mật (N2)", SkillCategory.VOCABULARY),
    VOCAB_REGISTER("Sử dụng từ theo bối cảnh (N2)", SkillCategory.VOCABULARY),
    VOCAB_NUANCE_DIFFERENCE("Hiểu sắc thái khác nhau của từ đồng nghĩa (N2)", SkillCategory.VOCABULARY),
    // N1
    VOCAB_IDIOMATIC("Biểu thức thành ngữ/tục ngữ (N1)", SkillCategory.VOCABULARY),
    VOCAB_SPECIALIZED("Từ vựng chuyên ngành (N1)", SkillCategory.VOCABULARY),

    // ===== GRAMMAR (Ngữ pháp) =====
    // N4/N3
    FILL_BLANK("Điền chỗ trống (　)", SkillCategory.GRAMMAR),
    SENTENCE_ORDER("Sắp xếp/điền số trong đoạn văn", SkillCategory.GRAMMAR),
    PARAPHRASE("Tìm câu có cách diễn đạt giống", SkillCategory.GRAMMAR),
    GRAMMAR_SELECT("Chọn ngữ pháp phù hợp với câu", SkillCategory.GRAMMAR),
    TEXT_COMPLETION("Hoàn thành đoạn văn", SkillCategory.GRAMMAR),
    // N3
    GRAMMAR_MEANING("Chọn ngữ pháp có cách diễn đạt tương tự (N3)", SkillCategory.GRAMMAR),
    GRAMMAR_NUANCE("Hiểu sắc thái khác nhau của ngữ pháp (N3)", SkillCategory.GRAMMAR),
    // N2
    GRAMMAR_SENTENCE_PATTERN("Mẫu câu phức tạp (N2)", SkillCategory.GRAMMAR),
    GRAMMAR_CAUSATIVE_PASSIVE("Thể nhân quả/bị động nâng cao (N2)", SkillCategory.GRAMMAR),
    GRAMMAR_CONDITIONAL("Các loại điều kiện (N2)", SkillCategory.GRAMMAR),
    // N1
    GRAMMAR_ADVANCED("Ngữ pháp nâng cao (N1)", SkillCategory.GRAMMAR),
    GRAMMAR_CLASSICAL("Ngữ pháp cổ/văn học (N1)", SkillCategory.GRAMMAR),

    // ===== READING (Đọc hiểu) =====
    // N4
    READING_SHORT("Đọc hiểu đoạn văn ngắn", SkillCategory.READING),
    READING_DIALOGUE("Đọc hiểu hội thoại ngắn", SkillCategory.READING),
    READING_LETTER("Đọc thư/tin nhắn", SkillCategory.READING),
    // N3
    READING_PERSONAL("Đọc hiểu mô tả gia đình/cá nhân (N3)", SkillCategory.READING),
    READING_PLACE("Đọc hiểu mô tả môi trường/địa điểm (N3)", SkillCategory.READING),
    READING_INFO("Đọc hiểu thông báo/lịch trình (N3)", SkillCategory.READING),
    READING_MEDIUM("Đọc hiểu trung văn (N3)", SkillCategory.READING),
    READING_COMPARISON("Bài so sánh (N3)", SkillCategory.READING),
    READING_INFO_SEARCH("Tìm kiếm thông tin (N3)", SkillCategory.READING),
    // N2
    READING_NUANCE("Trường văn (hiểu luận điểm) (N2)", SkillCategory.READING),
    READING_LOGICAL_FLOW("Hiểu mối liên hệ logic trong văn bản (N2)", SkillCategory.READING),
    READING_OPINION_ANALYSIS("Phân tích quan điểm/ý kiến (N2)", SkillCategory.READING),
    // N1
    READING_CRITICAL("Phân tích/đánh giá nội dung (N1)", SkillCategory.READING),
    READING_IMPLICATION("Hiểu ý ẩn của tác giả (N1)", SkillCategory.READING),
    READING_LONG_ESSAY("Đọc bài văn dài (N1)", SkillCategory.READING),
    READING_ACADEMIC("Đọc bài học thuật (N1)", SkillCategory.READING),

    // ===== LISTENING (Nghe hiểu) =====
    // N4
    LISTENING_TASK("Hiểu yêu cầu (課題理解)", SkillCategory.LISTENING),
    LISTENING_MAIN_POINT("Hiểu điểm chính (ポイント理解)", SkillCategory.LISTENING),
    LISTENING_MAIN_IDEA("Hiểu ý chính (概要理解)", SkillCategory.LISTENING),
    LISTENING_CORRECT_RESPONSE("Phản hồi tức thời (応答理解)", SkillCategory.LISTENING),
    // N3
    LISTENING_UNDERSTAND_KEY("Hiểu bao quát (N3)", SkillCategory.LISTENING),
    LISTENING_COMPREHENSIVE("Nghe hiểu tổng hợp (N3)", SkillCategory.LISTENING),
    LISTENING_DETAIL("Nghe hiểu chi tiết (N3)", SkillCategory.LISTENING),
    // N2
    LISTENING_RELATIONSHIP("Hiểu mối quan hệ giữa người nói (N2)", SkillCategory.LISTENING),
    LISTENING_IMPLICIT("Hiểu ý ẩn/ý định của người nói (N2)", SkillCategory.LISTENING),
    LISTENING_NUANCE_TONE("Hiểu thái độ/nhu cầu (N2)", SkillCategory.LISTENING),
    // N1
    LISTENING_INFERENCE("Suy luận từ nội dung nghe (N1)", SkillCategory.LISTENING),
    LISTENING_LONG_LECTURE("Nghe bài giảng dài (N1)", SkillCategory.LISTENING),
    LISTENING_COMPLEX("Nghe hiểu phức tạp - đa chủ đề (N1)", SkillCategory.LISTENING),
    LISTENING_SPECIALIZED("Nghe bài chuyên đề phức tạp (N1)", SkillCategory.LISTENING);

    private final String displayName;
    private final SkillCategory category;

    AssessmentType(String displayName, SkillCategory category) {
        this.displayName = displayName;
        this.category = category;
    }
}
