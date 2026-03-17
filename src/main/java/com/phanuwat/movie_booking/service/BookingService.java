package com.phanuwat.movie_booking.service;

import java.util.List;
import java.util.Set;

import com.phanuwat.movie_booking.model.Seat;
import com.phanuwat.movie_booking.store.BookingStore;

public final class BookingService {
    private BookingService() {
    }

    public static List<BookingStore.BookingRecord> getBookings() {
        return BookingStore.getBookings();
    }

    public static void updateBooking(String ticketId, BookingStore.BookingRecord updated) {
        BookingStore.updateBooking(ticketId, updated);
    }

    public static void deleteBooking(String ticketId) {
        BookingStore.deleteBooking(ticketId);
    }

    public static String allocateTicketRange(int count) {
        return BookingStore.allocateTicketRange(count);
    }

    public static void addBookingWithRecord(
            String movieTitle,
            String showtime,
            List<Seat> seats,
            BookingStore.BookingRecord record
    ) {
        BookingStore.addBookingWithRecord(movieTitle, showtime, seats, record);
    }

    public static Set<String> getBookedSeats(String movieTitle, String showtime) {
        return BookingStore.getBookedSeats(movieTitle, showtime);
    }
}
