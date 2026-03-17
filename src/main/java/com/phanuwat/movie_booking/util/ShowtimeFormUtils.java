package com.phanuwat.movie_booking.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ShowtimeFormUtils {
    private ShowtimeFormUtils() {
    }

    public static List<String> parseTokens(String showtimesText) {
        if (showtimesText == null) {
            return List.of();
        }
        List<String> tokens = Arrays.stream(showtimesText.split(","))
                .map(String::trim)
                .map(ShowtimeUtils::normalizeToken)
                .filter(token -> token != null && !token.isBlank())
                .collect(Collectors.toList());
        tokens.sort(ShowtimeUtils::compareTokens);
        return tokens;
    }

    public static String validateFormat(String showtimesText) {
        if (showtimesText == null || showtimesText.trim().isEmpty()) {
            return "กรุณากรอกรอบฉายให้ถูกต้อง (เช่น 10:00@1, 15:00@2)";
        }
        String[] tokens = showtimesText.split(",");
        for (String rawToken : tokens) {
            String token = rawToken == null ? "" : rawToken.trim();
            if (token.isEmpty()) {
                continue;
            }
            String timePart = ShowtimeUtils.extractTime(token);
            String theaterPart = ShowtimeUtils.extractTheater(token);
            if (!ShowtimeUtils.isValidTime(timePart)) {
                return "รูปแบบเวลาไม่ถูกต้อง: " + token;
            }
            if (token.contains("@") || token.contains("|")) {
                if (!ShowtimeUtils.isValidTheater(theaterPart)) {
                    return "รูปแบบโรงไม่ถูกต้อง: " + token;
                }
            }
        }
        return null;
    }
}
