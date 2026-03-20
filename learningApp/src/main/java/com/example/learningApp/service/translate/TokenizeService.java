package com.example.learningApp.service.translate;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import org.springframework.stereotype.Service;

/**
 * TokenizeService
 * ----------------
 * Service dùng để tách từ tiếng Nhật bằng thư viện Kuromoji.
 * Chịu trách nhiệm phân tích hình thái và trả về token đầu tiên của câu.
 */
@Service
public class TokenizeService {

    // Kuromoji tokenizer dùng để phân tích tiếng Nhật
    private final Tokenizer tokenizer = new Tokenizer();

    /**
     * Tách câu tiếng Nhật và trả về token đầu tiên.
     *
     * @param text câu tiếng Nhật cần tokenize
     * @return Token đầu tiên trong câu
     * @throws IllegalArgumentException nếu text rỗng hoặc không tokenize được
     */
    public Token firstToken(String text) {
        var tokens = tokenizer.tokenize(text);

        // Kiểm tra trường hợp không có token
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Text is empty or cannot be tokenized");
        }

        return tokens.get(0);
    }
}

