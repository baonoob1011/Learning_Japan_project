package com.example.learningApp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_flashcards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFlashcard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Của ai?

    @ManyToOne
    @JoinColumn(name = "vocab_id", nullable = false)
    private Vocabulary vocabulary; // Học từ nào? (Lấy nghĩa, kanji từ đây)

    @ManyToOne
    @JoinColumn(name = "deck_id")
    private Deck deck; // Nằm trong bộ thẻ nào?

    @ManyToOne
    @JoinColumn(name = "video_id")
    private YoutubeVideo video; // Học từ video nào? (để link ngược lại)

    // --- DỮ LIỆU HỌC TẬP ---

    @Column(name = "source_sentence", columnDefinition = "TEXT")
    private String sourceSentence; // Câu ví dụ lấy từ script video lúc lưu

    // Các chỉ số cho thuật toán lặp lại (Spaced Repetition)
    private int level = 0; // Cấp độ nhớ (0: New, 1: Hard, 5: Master...)

    private int streak = 0; // Số lần trả lời đúng liên tiếp

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt; // Thời gian ôn tập tiếp theo

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt; // Lần ôn tập cuối
}
