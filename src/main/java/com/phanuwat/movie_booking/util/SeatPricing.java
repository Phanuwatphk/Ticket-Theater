package com.phanuwat.movie_booking.util;

import com.phanuwat.movie_booking.model.Seat;

public final class SeatPricing {
    private SeatPricing() {
    }

    public static int getSeatPrice(Seat seat) {
        if (seat == null) {
            return 0;
        }
        return getSeatPrice(seat.getRow());
    }

    public static int getSeatPrice(String row) {
        if (row == null || row.isEmpty()) {
            return 0;
        }
        char rowChar = Character.toUpperCase(row.charAt(0));
        if (rowChar >= 'A' && rowChar <= 'C') {
            return 120;
        }
        if (rowChar >= 'D' && rowChar <= 'G') {
            return 160;
        }
        if (rowChar >= 'H' && rowChar <= 'J') {
            return 200;
        }
        return 0;
    }
}
