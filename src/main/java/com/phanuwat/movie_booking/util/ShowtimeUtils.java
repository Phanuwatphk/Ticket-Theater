package com.phanuwat.movie_booking.util;

public final class ShowtimeUtils {
    private ShowtimeUtils() {
    }

    public static String normalizeToken(String token) {
        if (token == null) {
            return null;
        }
        String trimmed = token.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String time = trimmed;
        String theater = "1";
        if (trimmed.contains("@")) {
            String[] parts = trimmed.split("@", 2);
            time = parts[0].trim();
            theater = parts.length > 1 ? parts[1].trim() : "1";
        } else if (trimmed.contains("|")) {
            String[] parts = trimmed.split("\\|", 2);
            time = parts[0].trim();
            theater = parts.length > 1 ? parts[1].trim() : "1";
        }
        if (time.isEmpty()) {
            return null;
        }
        if (theater == null || theater.isBlank()) {
            theater = "1";
        }
        return time + "@" + theater;
    }

    public static String extractTime(String token) {
        if (token == null) {
            return "";
        }
        String trimmed = token.trim();
        if (trimmed.contains("@")) {
            return trimmed.split("@", 2)[0].trim();
        }
        if (trimmed.contains("|")) {
            return trimmed.split("\\|", 2)[0].trim();
        }
        return trimmed;
    }

    public static String extractTheater(String token) {
        if (token == null) {
            return "";
        }
        String trimmed = token.trim();
        if (trimmed.contains("@")) {
            String[] parts = trimmed.split("@", 2);
            return parts.length > 1 ? parts[1].trim() : "";
        }
        if (trimmed.contains("|")) {
            String[] parts = trimmed.split("\\|", 2);
            return parts.length > 1 ? parts[1].trim() : "";
        }
        return "";
    }

    public static int compareTokens(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        String leftTime = extractTime(left);
        String rightTime = extractTime(right);
        int leftMinutes = parseTimeToMinutes(leftTime);
        int rightMinutes = parseTimeToMinutes(rightTime);
        if (leftMinutes != rightMinutes) {
            return Integer.compare(leftMinutes, rightMinutes);
        }
        int leftTheater = parseTheaterNumber(extractTheater(left));
        int rightTheater = parseTheaterNumber(extractTheater(right));
        if (leftTheater != rightTheater) {
            return Integer.compare(leftTheater, rightTheater);
        }
        return left.compareToIgnoreCase(right);
    }

    public static boolean isValidTime(String timeText) {
        if (timeText == null) {
            return false;
        }
        String trimmed = timeText.trim();
        if (!trimmed.matches("\\d{1,2}:\\d{2}")) {
            return false;
        }
        String[] parts = trimmed.split(":");
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidTheater(String theaterText) {
        if (theaterText == null) {
            return false;
        }
        String trimmed = theaterText.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        return trimmed.matches("\\d+");
    }

    public static int parseTimeToMinutes(String timeText) {
        if (timeText == null) {
            return Integer.MAX_VALUE;
        }
        String trimmed = timeText.trim();
        if (trimmed.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        String[] parts = trimmed.split(":");
        if (parts.length != 2) {
            return Integer.MAX_VALUE;
        }
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
                return Integer.MAX_VALUE;
            }
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private static int parseTheaterNumber(String theater) {
        if (theater == null || theater.isBlank()) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(theater.trim());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}
