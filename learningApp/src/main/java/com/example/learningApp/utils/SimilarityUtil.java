package com.example.learningApp.utils;


public class SimilarityUtil {

    /**
     * Tính độ giống nhau (%) dựa trên Levenshtein Distance
     * Phù hợp cho luyện phát âm / speech-to-text
     */
    public static double similarityPercent(String expected, String actual) {
        if (expected == null || actual == null) return 0.0;

        expected = normalize(expected);
        actual = normalize(actual);

        int maxLen = Math.max(expected.length(), actual.length());
        if (maxLen == 0) return 100.0;

        int distance = levenshteinDistance(expected, actual);

        double score = (1.0 - (double) distance / maxLen) * 100.0;

        return Math.max(0, Math.min(100, score));
    }


    /**
     * Chuẩn hóa text:
     * - lowercase
     * - bỏ dấu câu
     * - bỏ khoảng trắng thừa
     */
    private static String normalize(String s) {
        return s
                .toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Levenshtein Distance
     */
    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }
}

