package com.example.learningApp.utils;


import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import java.util.List;
import java.util.stream.Collectors;

public class JapaneseAnalyzerUtil {

    private static final Tokenizer tokenizer = new Tokenizer();

    public static String getKatakanaReading(String text) {
        List<Token> tokens = tokenizer.tokenize(text);

        return tokens.stream()
                .map(t -> t.getReading() != null ? t.getReading() : t.getSurface())
                .collect(Collectors.joining());
    }
}
