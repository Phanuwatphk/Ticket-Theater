package com.phanuwat.movie_booking.store;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.phanuwat.movie_booking.model.Seat;

public class BookingStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path STORE_PATH = Paths.get("data", "bookings.json");
    private static final Path LEGACY_STORE_PATH = Paths.get(
            System.getProperty("user.home"),
            ".movie-booking",
            "bookings.json"
    );

    private static final Map<String, Set<String>> BOOKED_SEATS = new HashMap<>();
    private static final List<BookingRecord> BOOKINGS = new java.util.ArrayList<>();
    private static int ticketCounter = 1;

    static {
        load();
    }

    private BookingStore() {
    }

    public static synchronized void addBooking(String movieTitle, String showtime, List<Seat> seats) {
        String key = buildKey(movieTitle, showtime);
        Set<String> booked = BOOKED_SEATS.computeIfAbsent(key, k -> new HashSet<>());
        for (Seat seat : seats) {
            booked.add(seat.getRow() + seat.getColumn());
        }
        save();
    }

    public static synchronized void addBookingWithRecord(
            String movieTitle,
            String showtime,
            List<Seat> seats,
            BookingRecord record
    ) {
        String key = buildKey(movieTitle, showtime);
        Set<String> booked = BOOKED_SEATS.computeIfAbsent(key, k -> new HashSet<>());
        for (Seat seat : seats) {
            booked.add(seat.getRow() + seat.getColumn());
        }
        if (record != null) {
            BOOKINGS.add(record);
        }
        save();
    }

    public static synchronized Set<String> getBookedSeats(String movieTitle, String showtime) {
        String key = buildKey(movieTitle, showtime);
        Set<String> booked = BOOKED_SEATS.get(key);
        if (booked == null) {
            return new HashSet<>();
        }
        return new HashSet<>(booked);
    }

    public static synchronized List<BookingRecord> getBookings() {
        return new java.util.ArrayList<>(BOOKINGS);
    }

    public static synchronized void updateBooking(String ticketId, BookingRecord updated) {
        if (ticketId == null || updated == null) {
            return;
        }
        for (int i = 0; i < BOOKINGS.size(); i++) {
            if (ticketId.equals(BOOKINGS.get(i).getTicketId())) {
                BOOKINGS.set(i, updated);
                save();
                return;
            }
        }
    }

    public static synchronized void deleteBooking(String ticketId) {
        if (ticketId == null) {
            return;
        }
        BookingRecord target = null;
        for (BookingRecord record : BOOKINGS) {
            if (ticketId.equals(record.getTicketId())) {
                target = record;
                break;
            }
        }
        if (target != null) {
            removeBookedSeats(target);
            BOOKINGS.remove(target);
            save();
        }
    }

    public static synchronized String allocateTicketRange(int count) {
        if (count <= 0) {
            return "";
        }
        int start = ticketCounter;
        int end = ticketCounter + count - 1;
        ticketCounter += count;
        save();
        String startId = formatTicketId(start);
        String endId = formatTicketId(end);
        if (start == end) {
            return startId;
        }
        return startId + "-" + endId;
    }

    private static void load() {
        migrateLegacyStoreIfNeeded();
        if (!Files.exists(STORE_PATH)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(STORE_PATH)) {
            StoreData data = GSON.fromJson(reader, StoreData.class);
            if (data == null) {
                return;
            }
            BOOKED_SEATS.clear();
            BOOKINGS.clear();
            if (data.bookings != null) {
                BOOKINGS.addAll(data.bookings);
            }
            if (data.ticketCounter > 0) {
                ticketCounter = data.ticketCounter;
            }
            rebuildBookedSeatsFromBookings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        try {
            Files.createDirectories(STORE_PATH.getParent());
            StoreData data = new StoreData();
            rebuildBookedSeatsFromBookings();
            data.bookedSeats = BOOKED_SEATS;
            data.bookings = BOOKINGS;
            data.ticketCounter = ticketCounter;
            try (Writer writer = Files.newBufferedWriter(STORE_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void migrateLegacyStoreIfNeeded() {
        try {
            if (Files.exists(STORE_PATH)) {
                return;
            }
            if (!Files.exists(LEGACY_STORE_PATH)) {
                return;
            }
            Files.createDirectories(STORE_PATH.getParent());
            Files.copy(LEGACY_STORE_PATH, STORE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String buildKey(String movieTitle, String showtime) {
        String safeTitle = movieTitle == null ? "" : movieTitle.trim();
        String safeTime = showtime == null ? "" : showtime.trim();
        return safeTitle + "@" + safeTime;
    }

    private static String formatTicketId(int number) {
        return String.format("T%04d", number);
    }

    private static void removeBookedSeats(BookingRecord record) {
        String key = buildKey(record.getMovieTitle(), record.getShowtime());
        Set<String> booked = BOOKED_SEATS.get(key);
        if (booked == null) {
            return;
        }
        String seatsText = record.getSeats();
        if (seatsText != null && !seatsText.isBlank()) {
            String[] seats = seatsText.split(",");
            for (String seat : seats) {
                booked.remove(seat.trim());
            }
        }
        if (booked.isEmpty()) {
            BOOKED_SEATS.remove(key);
        }
    }

    private static void rebuildBookedSeatsFromBookings() {
        BOOKED_SEATS.clear();
        for (BookingRecord record : BOOKINGS) {
            if (record == null) {
                continue;
            }
            String key = buildKey(record.getMovieTitle(), record.getShowtime());
            Set<String> booked = BOOKED_SEATS.computeIfAbsent(key, k -> new HashSet<>());
            String seatsText = record.getSeats();
            if (seatsText == null || seatsText.isBlank()) {
                continue;
            }
            String[] seats = seatsText.split(",");
            for (String seat : seats) {
                String seatCode = seat.trim();
                if (!seatCode.isEmpty()) {
                    booked.add(seatCode);
                }
            }
        }
    }

    private static class StoreData {
        Map<String, Set<String>> bookedSeats = new HashMap<>();
        List<BookingRecord> bookings = new java.util.ArrayList<>();
        int ticketCounter = 1;
    }

    public static class BookingRecord {
        private final String ticketId;
        private final String movieTitle;
        private final String showtime;
        private final String seats;
        private final String name;
        private final String phone;
        private final String totalPrice;
        private final String timestamp;

        public BookingRecord(
                String ticketId,
                String movieTitle,
                String showtime,
                String seats,
                String name,
                String phone,
                int totalPrice,
                String timestamp
        ) {
            this.ticketId = ticketId;
            this.movieTitle = movieTitle;
            this.showtime = showtime;
            this.seats = seats;
            this.name = name;
            this.phone = phone;
            this.totalPrice = totalPrice + " บาท";
            this.timestamp = timestamp;
        }

        public String getTicketId() {
            return ticketId;
        }

        public String getMovieTitle() {
            return movieTitle;
        }

        public String getShowtime() {
            return showtime;
        }

        public String getSeats() {
            return seats;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getTotalPrice() {
            return totalPrice;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}
