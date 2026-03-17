package com.phanuwat.movie_booking.util;

public final class TextUtils {
    private TextUtils() {
    }

    public static int parsePrice(String priceText) {
        if (priceText == null) {
            return 0;
        }
        String digits = priceText.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
