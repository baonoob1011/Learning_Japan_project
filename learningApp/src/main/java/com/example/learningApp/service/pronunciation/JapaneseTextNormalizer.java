package com.example.learningApp.service.pronunciation;

import org.springframework.stereotype.Component;

@Component
public class JapaneseTextNormalizer {

    public String normalizeTextForJapanese(String text) {
        return text.replace("0", "零")
                .replace("1", "一")
                .replace("2", "二")
                .replace("3", "三")
                .replace("4", "四")
                .replace("5", "五")
                .replace("6", "六")
                .replace("7", "七")
                .replace("8", "八")
                .replace("9", "九");
    }

    public String normalizeForCompare(String text) {

        if (text == null) return "";

        text = text.replaceAll("[、。！？,.?!]", "")
                .replaceAll("\\s+", "");

        text = text.replace("に","二")
                .replace("2","二")
                .replace("さん","三")
                .replace("3","三")
                .replace("し","四")
                .replace("4","四")
                .replace("ご","五")
                .replace("5","五")
                .replace("ろく","六")
                .replace("6","六")
                .replace("なな","七")
                .replace("7","七")
                .replace("はち","八")
                .replace("8","八")
                .replace("きゅう","九")
                .replace("9","九")
                .replace("0","零")
                .replace("ぜろ","零");

        return text;
    }
}
