package com.example.learningApp.service.translate;

import org.springframework.stereotype.Service;

@Service
public class RomajiService {

    public String toRomaji(String katakana) {
        return katakana
                .replace("ア", "a").replace("イ", "i").replace("ウ", "u")
                .replace("エ", "e").replace("オ", "o")
                .replace("カ", "ka").replace("キ", "ki").replace("ク", "ku")
                .replace("ケ", "ke").replace("コ", "ko")
                .replace("サ", "sa").replace("シ", "shi").replace("ス", "su")
                .replace("セ", "se").replace("ソ", "so")
                .replace("タ", "ta").replace("チ", "chi").replace("ツ", "tsu")
                .replace("テ", "te").replace("ト", "to")
                .replace("ナ", "na").replace("ニ", "ni").replace("ヌ", "nu")
                .replace("ネ", "ne").replace("ノ", "no")
                .replace("ハ", "ha").replace("ヒ", "hi").replace("フ", "fu")
                .replace("ヘ", "he").replace("ホ", "ho")
                .replace("マ", "ma").replace("ミ", "mi").replace("ム", "mu")
                .replace("メ", "me").replace("モ", "mo")
                .replace("ヤ", "ya").replace("ユ", "yu").replace("ヨ", "yo")
                .replace("ラ", "ra").replace("リ", "ri").replace("ル", "ru")
                .replace("レ", "re").replace("ロ", "ro")
                .replace("ワ", "wa").replace("ヲ", "wo").replace("ン", "n");
    }
}

