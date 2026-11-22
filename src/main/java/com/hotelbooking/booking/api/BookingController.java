package com.hotelbooking.booking.api;

import com.hotelbooking.booking.api.dto.BookingResponse;
import com.hotelbooking.booking.api.dto.CreateBookingRequest;
import com.hotelbooking.booking.application.BookingService;
import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings")
class BookingController {

    private final BookingService bookings;

    BookingController(BookingService bookings) {
        this.bookings = bookings;
    }

    @PostMapping
    @Operation(summary = "Book a room type for a date range",
            description = "Holds the rooms and returns a PENDING_PAYMENT booking. Pay before holdExpiresAt.")
    ResponseEntity<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request) {
        Booking booking = bookings.create(request.toCommand());
        return ResponseEntity.created(URI.create("/api/v1/bookings/" + booking.id()))
                .body(BookingResponse.from(booking));
    }

    @GetMapping("/{bookingId}")
    @Operation(summary = "Get a booking")
    BookingResponse get(@PathVariable UUID bookingId) {
        return BookingResponse.from(bookings.get(new BookingId(bookingId)));
    }
}
